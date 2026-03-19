# Bug Report: `/home` tự động redirect sang `/login`

## Tóm tắt người gây lỗi

| # | Vấn đề | Người gây lỗi | GitHub | Commit | Ngày | Commit message |
|---|---|---|---|---|---|---|
| 1 | `getGlobalCategories()` không có try-catch | **Keiwa081** | anminecraft134@gmail.com | `c93750e` | 2026-03-05 | "register/login minor update" |
| 2 | `httpsEnforcerFilter` re-enable `DefaultLoginPageGeneratingFilter` | **Keiwa081** | anminecraft134@gmail.com | `6f63225` | 2026-03-17 | "Deploy lên server và fix login gg và fb do https" |
| phụ | `@ModelAttribute("currentUser")` khai báo trùng | **lamndhts01060** | lamndhts01060@fpt.edu.vn | `dab5cd0` | 2026-01-02 | "Hoan thien chuc nang: Dang ky, Dang nhap, Cap nhat Profile va Fix loi Controller" |

> **Kết luận:** Cả 2 vấn đề cốt lõi tạo ra bug đều được commit bởi cùng một người — **Keiwa081**.

---

## Mô tả triệu chứng

- Người dùng truy cập `http://localhost:8080/home` (hoặc `/`)
- Browser tự redirect sang `http://localhost:8080/login`
- Trang `/login` hiển thị **trang mặc định của Spring Security** ("Please sign in") thay vì template tùy chỉnh `user/login.html` ("Đăng nhập - ShopOMG")

---

## Môi trường

| Thành phần | Giá trị |
|---|---|
| Framework | Spring Boot 3.x / Spring Security 6.x |
| Template engine | Thymeleaf |
| Database | SQL Server (JDBC) |
| OAuth2 Provider | Google, Facebook |

---

## Phân tích nguyên nhân gốc rễ

Có **2 vấn đề độc lập** cùng xảy ra và kết hợp tạo ra triệu chứng trên.

---

## Vấn đề 1 — Tại sao `/home` redirect sang `/login`

### Git blame

| | |
|---|---|
| **Author** | Keiwa081 (`anminecraft134@gmail.com`) |
| **Commit** | `c93750e` |
| **Ngày** | 2026-03-05 07:10 +0700 |
| **Message** | "register/login minor update" |

Keiwa081 thêm `getGlobalCategories()` vào `GlobalModelAttributes.java` mà không bọc try-catch. Comment trong code thậm chí ghi rõ _"Sửa lại thành hàm lấy danh mục tương ứng của bạn"_ — cho thấy đây là code copy-paste chưa kiểm tra kỹ.

### Thủ phạm

**File:** `src/main/java/poly/edu/config/GlobalModelAttributes.java` — dòng 69–72

```java
@ModelAttribute("categories")
public List<Category> getGlobalCategories() {
    return categoryService.findAll(); // Chạy cho MỌI request, kể cả khi chưa đăng nhập
}
```

### Tại sao đây là vấn đề

`@ControllerAdvice` với `@ModelAttribute` không có điều kiện sẽ **chạy trước mọi controller handler**, bao gồm cả `/home` của user chưa đăng nhập.

Nếu `categoryService.findAll()` ném exception (ví dụ: DB chưa sẵn sàng, connection timeout, bảng `Categories` chưa tồn tại), exception này leo lên qua Spring Security's `ExceptionTranslationFilter`.

`ExceptionTranslationFilter` phân tích chuỗi nguyên nhân (cause chain) của exception:
- Nếu tìm thấy `AuthenticationException` → redirect sang `/login`
- Nếu tìm thấy `AccessDeniedException` → redirect sang `/403`
- Nếu không phải → ném tiếp lên

Một số JDBC/JPA exception có thể được wrap dưới dạng `AuthenticationException` trong một số cấu hình, gây ra redirect không mong muốn sang `/login`.

### Vấn đề phụ: `@ModelAttribute("currentUser")` bị khai báo trùng (git blame: lamndhts01060, commit `dab5cd0`, 2026-01-02)

| File | Dòng | Method |
|---|---|---|
| `CurrentUserAdvice.java` | 19 | `getCurrentUser(Authentication auth)` |
| `GlobalModelAttributes.java` | 33 | `currentUser(Principal principal)` |

Cả hai đều export model attribute tên `"currentUser"`. Spring MVC xử lý theo thứ tự không đảm bảo → giá trị cuối cùng trong model phụ thuộc vào thứ tự load bean, không nhất quán.

---

## Vấn đề 2 — Tại sao `/login` hiển thị trang mặc định thay vì template tùy chỉnh

### Git blame

| | |
|---|---|
| **Author** | Keiwa081 (`anminecraft134@gmail.com`) |
| **Commit** | `6f63225` |
| **Ngày** | 2026-03-17 05:56 +0700 |
| **Message** | "Deploy lên server và fix login gg và fb do https" |

Keiwa081 thêm toàn bộ `httpsEnforcerFilter` bean vào `SecurityConfig.java` khi deploy lên server để fix OAuth2 redirect URI. Filter này giải quyết được vấn đề HTTPS cho Google/Facebook nhưng vô tình kích hoạt lại `DefaultLoginPageGeneratingFilter`.

### Thủ phạm

**File:** `src/main/java/poly/edu/config/SecurityConfig.java` — dòng 157–186

```java
@Bean
public FilterRegistrationBean<Filter> httpsEnforcerFilter() {
    FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter((request, response, chain) -> {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(req) {
            @Override public String getScheme()       { return "https"; }
            @Override public boolean isSecure()       { return true; }
            @Override public int getServerPort()      { return 443; }
            @Override public StringBuffer getRequestURL() {
                return new StringBuffer("https://").append(getServerName()).append(super.getRequestURI());
            }
        };
        chain.doFilter(wrapper, response);
    });
    registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE); // Integer.MIN_VALUE
    registrationBean.addUrlPatterns("/*");
    return registrationBean;
}
```

### Tại sao đây là vấn đề

Filter này được thêm vào với mục đích giải quyết yêu cầu HTTPS của Google/Facebook OAuth2 redirect URI. Tuy nhiên nó có **tác dụng phụ nghiêm trọng**:

**Thứ tự filter chain:**
```
Browser Request
  │
  ▼
[HIGHEST_PRECEDENCE] httpsEnforcerFilter
  │  → wrap request: isSecure()=true, scheme="https", port=443
  │  → getRequestURL() = "https://localhost/..." (không có port 8080)
  │
  ▼
[Order -100] DelegatingFilterProxy → FilterChainProxy (Spring Security)
  │  → nhận request đã bị wrap → nghĩ đây là HTTPS request thật
  │
  ▼
Spring Security Filter Chain
  ├── SecurityContextHolderFilter
  ├── CsrfFilter
  ├── [Custom] CSRF Eager Loading Filter
  ├── OAuth2AuthorizationRequestRedirectFilter
  ├── OAuth2LoginAuthenticationFilter
  ├── DefaultLoginPageGeneratingFilter  ← VẤN ĐỀ NẰM Ở ĐÂY
  ├── UsernamePasswordAuthenticationFilter
  ├── AnonymousAuthenticationFilter
  ├── ExceptionTranslationFilter
  └── AuthorizationFilter
```

### `DefaultLoginPageGeneratingFilter` tại sao vẫn còn active?

Theo thiết kế của Spring Security:
- `formLogin.loginPage("/login")` → **nên** disable `DefaultLoginPageGeneratingFilter`
- Nhưng khi `oauth2Login` **cũng** được cấu hình → `OAuth2LoginConfigurer` có thể **re-enable** filter này để hiển thị link đăng nhập OAuth2

Kết quả: Filter vẫn active và **chặn GET `/login` trước khi `DispatcherServlet` route đến `AuthController.showLoginForm()`**.

```
GET /login
  ├── [Spring Security] DefaultLoginPageGeneratingFilter.isLoginUrlRequest("/login") → true
  │     → generate HTML mặc định "Login with OAuth 2.0"
  │     → response.getWriter().write(loginPageHtml)
  │     → RETURN  ← DispatcherServlet không bao giờ được gọi
  │
  └── [Không đến được] AuthController.showLoginForm() → "user/login" template
```

### Bằng chứng

| Dấu hiệu | Giải thích |
|---|---|
| Tab browser: **"Please sign in"** | Đây là title hardcode trong `DefaultLoginPageGeneratingFilter` |
| Tab browser sẽ là: **"Đăng nhập - ShopOMG"** | Nếu `user/login.html` được render (`<title>` ở dòng 5 của template) |
| Giao diện: chỉ có 2 link "facebook" và "Google" | Giao diện mặc định của Spring Security OAuth2 |

---

## Flow đầy đủ khi xảy ra bug

```
1. User gõ: http://localhost:8080/home
   │
   ▼
2. httpsEnforcerFilter (HIGHEST_PRECEDENCE)
   └── Wrap request: scheme=https, port=443, isSecure=true
   │
   ▼
3. Spring Security FilterChainProxy
   └── anyRequest().permitAll() → ALLOW  ✓
   │
   ▼
4. DispatcherServlet → HomeController.index()
   │
   ▼
5. Spring MVC gọi tất cả @ModelAttribute trong @ControllerAdvice
   └── GlobalModelAttributes.getGlobalCategories()
         └── categoryService.findAll()
               └── [NẾU LỖI] Ném exception (DB timeout, etc.)
   │
   ▼  (nếu có exception)
6. Exception leo lên qua Spring Security's ExceptionTranslationFilter
   └── Phân tích cause chain → tìm thấy AuthenticationException/AccessDeniedException
   └── Gọi LoginUrlAuthenticationEntryPoint.commence()
   │
   ▼
7. HTTP 302 Redirect → http://localhost:8080/login
   │
   ▼
8. Browser GET /login
   │
   ▼
9. Spring Security FilterChainProxy
   └── DefaultLoginPageGeneratingFilter.isLoginUrlRequest("/login") → true
   └── Generate HTML mặc định → response trả về
   └── DispatcherServlet KHÔNG được gọi
   │
   ▼
10. Browser hiển thị trang "Please sign in" (mặc định Spring Security)
    THAY VÌ template user/login.html ("Đăng nhập - ShopOMG")
```

---

## Các file liên quan

| File | Vị trí vấn đề | Vai trò |
|---|---|---|
| `SecurityConfig.java` | Dòng 157–186 | `httpsEnforcerFilter` — kích hoạt lại `DefaultLoginPageGeneratingFilter` |
| `GlobalModelAttributes.java` | Dòng 69–72 | `getGlobalCategories()` — chạy mọi request, có thể throw exception |
| `GlobalModelAttributes.java` | Dòng 33 | `currentUser` trùng tên với `CurrentUserAdvice` |
| `AuthController.java` | Dòng 24 | `GET /login` — bị bypass bởi `DefaultLoginPageGeneratingFilter` |
| `application.properties` | Dòng 61 | `server.forward-headers-strategy=native` — tương tác với `httpsEnforcerFilter` |

---

## Hướng fix (chỉ để tham khảo, chưa áp dụng)

### Fix vấn đề 1 — `GlobalModelAttributes`

Bảo vệ `getGlobalCategories()` bằng try-catch để tránh exception leo lên Spring Security:

```java
@ModelAttribute("categories")
public List<Category> getGlobalCategories() {
    try {
        return categoryService.findAll();
    } catch (Exception e) {
        return Collections.emptyList();
    }
}
```

Xóa 1 trong 2 `@ModelAttribute("currentUser")` bị trùng (giữ lại `CurrentUserAdvice` vì xử lý cả OAuth2).

### Fix vấn đề 2 — `DefaultLoginPageGeneratingFilter`

Thêm `.disable()` tường minh vào cấu hình:

```java
// Trong SecurityConfig.filterChain():
http.formLogin(login -> login
    .loginPage("/login")
    ...
);

// Sau khi build xong, thêm:
DefaultLoginPageGeneratingFilter filter = http.getSharedObject(DefaultLoginPageGeneratingFilter.class);
if (filter != null) {
    filter.setEnabled(false);
}
```

Hoặc dùng `server.forward-headers-strategy=framework` thay vì `native` để Spring Boot tự handle headers một cách an toàn hơn, bỏ `httpsEnforcerFilter` đi và dùng cơ chế proxy header chuẩn.

---

*Tài liệu này được tạo để phục vụ debugging — chưa có thay đổi code nào được áp dụng.*
