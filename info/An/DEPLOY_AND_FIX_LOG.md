# Hướng dẫn Deploy Spring Boot lên VPS Ubuntu & Fix Lỗi OAuth2 (Google/Facebook)

Tài liệu này tổng hợp ngắn gọn các bước từ lúc bắt đầu mua VPS Ubuntu, deploy ứng dụng Spring Boot, cài đặt Nginx, SSL, và quan trọng nhất là chi tiết cách xử lý triệt để lỗi cấu hình `redirect_uri` của Facebook/Google OAuth2 đằng sau thư mục Reserve Proxy.

---

## Phần 1: Deploy ứng dụng lên VPS (Tóm tắt)

1. **Chuẩn bị VPS & Môi trường:**
   - Thuê VPS Ubuntu (khuyến nghị 2GB RAM, 1 CPU). Truy cập qua SSH (`ssh root@ip_cua_vps`).
   - Cài đặt Java JDK (VD: OpenJDK 17).
   - Cài đặt Nginx làm Reverse Proxy.
   - Cài đặt Database (SQL Server/MySQL) hoặc kết nối DB bên ngoài.

2. **Cấu hình Nginx & SSL (HTTPS):**
   - Trỏ domain (VD: `shopomg.id.vn`) về IP của VPS trên trang quản lý DNS.
   - Viết file cấu hình Nginx trong `/etc/nginx/sites-available/` để forward thư mục port `80` sang port `8080` của Spring Boot.
   - Cài đặt chứng chỉ SSL miễn phí bằng Certbot (Let's Encrypt):
     `sudo certbot --nginx -d shopomg.id.vn -d www.shopomg.id.vn`
   - Nginx phải được cấu hình truyền các **Headers quan trọng** cho Spring Boot biết yêu cầu ban đầu là HTTPS:
     ```nginx
     location / {
         proxy_pass http://localhost:8080;
         proxy_set_header Host $host;
         proxy_set_header X-Real-IP $remote_addr;
         proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
         proxy_set_header X-Forwarded-Proto $scheme; # Rất quan trọng cho OAuth2
     }
     ```

3. **Deploy & Chạy Spring Boot như một Service (systemd):**
   - Đóng gói ứng dụng thành file `.jar` (`mvn clean package -DskipTests`).
   - Upload file `shopomg.jar` lên VPS (vào `/root/shopomg/`).
   - Tạo file service cho Systemd (VD: `/etc/systemd/system/shopomg.service`) để app tự khởi động lại khi sập hoặc khi VPS resest.
   - Khởi động app: 
     ```bash
     sudo systemctl enable shopomg.service
     sudo systemctl start shopomg.service
     ```

---

## Phần 2: Cơn ác mộng OAuth2 Facebook "http://" & Cách giải quyết

### 1. Mô tả sự cố
Sau khi có SSL, web chạy trên `https://shopomg.id.vn`, nhưng khi click **Đăng nhập Facebook**, url chuyển hướng trả về cho trình duyệt một tham số `redirect_uri=http://...` (giao thức HTTP không bảo mật). 
- Facebook chặn ngay lập tức vì không cấp quyền đăng nhập cho kết nối `http://` trần trụi.
- Google OAuth2 thỉnh thoảng cũng bị tương tự nếu không nhận diện đúng môi trường.

**Nguyên nhân gốc rễ:** 
Mặc dù Nginx phục vụ client bằng HTTPS, nhưng khi Nginx âm thầm chuyển tiếp (proxy_pass) request sang port 8080 của Tomcat/Spring. Tomcat lại thấy nó được gọi bằng nội bộ mạng `HTTP/1.1`. Do đó, lúc module OAuth2 động tay sinh link đăng nhập, nó cứ ngây thơ lấy giao thức `http://` gắn vào.

---

### 2. Các phương án thất bại (Đã thử nghiệm và nguyên nhân rút ra)

Trong quá trình phá án, chúng ta đã kinh qua nhiều phương pháp được hướng dẫn trên mạng nhưng đều bị Spring Boot "chặn đứng":

1. **Thêm thuộc tính cấu hình Web (Thất bại)**
   - Cố gắng báo cho Tomcat biết nó đang đứng sau Proxy:
     ```properties
     server.forward-headers-strategy=nativel # Hoặc framework
     server.tomcat.remoteip.remote-ip-header=x-forwarded-for
     server.tomcat.remoteip.protocol-header=x-forwarded-proto
     ```
   - **Lý do tạch:** Một số phiên bản Spring Boot + Tomcat không tương thích, hoặc do Nginx gửi chuẩn Header hơi khác cấu hình mặc định nên Tomcat bỏ qua.

2. **Chọc thẳng URL vào `application.properties` (Thất bại)**
   - Cố gắng hardcode luôn cái URI:
     `spring.security.oauth2.client.registration.facebook.redirect-uri=https://shopomg.id.vn/...`
   - **Lý do tạch:** Tính năng tự động sinh URL động (Dynamic URL Builder) của Spring Security mặc định mạnh hơn cái `application.properties`. Chữ `https` bị nó tự động override (ghi đè) lại thành `http` dựa trên Context.

3. **Custom Provider chặn đè (Thất bại)**
   - Tạo một Provider riêng gọi là `facebook-manual` không dùng chuẩn của Spring.
   - **Lý do tạch:** Quá phức tạp, làm hỏng hệ thống tự động mapping JSON của Spring. Gây nhầm lẫn Provider ID.

4. **Kế thừa và viết lại `OAuth2AuthorizationRequestResolver` (Thất bại)**
   - Chặn quá trình Spring chuẩn bị lệnh gửi sang Facebook thông qua class này, kiểm tra chuỗi `redirect_uri`, nếu thấy `http://` thì `replace` thành `https://`.
   - **Lý do tạch:** Tính năng Này hoạt động RẤT TỐT khi test ở localhost vì nó đánh trúng cái ngọn. Nhưng khi deploy lên VPS, không hiểu sao Spring Security lại Bypass (nhảy cóc) qua cái Bean này.

---

### 3. Phương pháp dứt điểm (The Ultimate Fix)

Sau khi đánh vào "ngọn" (Spring Security) không xong, chúng ta chuyển hướng đấm thẳng vào "gốc" (Tầng Servlet của Tomcat). 

Nếu Spring Boot bị ảo giác là đang chạy `http://`, ta sẽ tiêm một liều thuốc mê cho nó. Cài đặt một **Global Filter** tóm gọn tất cả các Request vừa chớm vào Tomcat, sau đó "Gói (Wrap)" request đó lại và nói dối Spring Boot rằng: *"Này, giao thức là HTTPS, cổng là 443"*.

**Triển khai vào file `SecurityConfig.java`**:

```java
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

// (Bên trong class SecurityConfig)

    @Bean
    public FilterRegistrationBean<jakarta.servlet.Filter> httpsEnforcerFilter() {
        FilterRegistrationBean<jakarta.servlet.Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter((request, response, chain) -> {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(req) {
                
                // Ép buộc Spring hiểu Scheme là HTTPS
                @Override
                public String getScheme() {
                    return "https";
                }
                
                // Khẳng định đây là kết nối bảo mật
                @Override
                public boolean isSecure() {
                    return true;
                }
                
                // Cổng mặc định của HTTPS là 443
                @Override
                public int getServerPort() {
                    return 443;
                }
                
                // Sinh lại đường truyền với prefix https://
                @Override
                public StringBuffer getRequestURL() {
                    StringBuffer url = new StringBuffer();
                    url.append("https://").append(getServerName()).append(super.getRequestURI());
                    return url;
                }
            };
            // Đẩy request "giả vờ làm HTTPS" này đi tiếp các bước còn lại của Spring
            chain.doFilter(wrapper, response);
        });
        
        // Cực kỳ quan trọng: Order phải là HIGHEST_PRECEDENCE để nó chạy TRƯỚC CẢ Spring Security
        registrationBean.setOrder(org.springframework.core.Ordered.HIGHEST_PRECEDENCE);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
```

**Kết quả:** 
Khi OAuth2 cần sinh link `redirect_uri`, nó gọi hàm `request.getScheme()` và `request.isSecure()`. Thay vì nhận kết quả thật là `http/false`, nó được "Filter" của chúng ta nhét vào mồm đáp án `https/true`. Kết quả là link sinh ra đẹp như mơ: `https://.../login/oauth2/code/facebook`.

Và Facebook đã cho phép đăng nhập thành công. Lỗi kết thúc.

---
*Tài liệu được sinh tự động bởi Antigravity.*
