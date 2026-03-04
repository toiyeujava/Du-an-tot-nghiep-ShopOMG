package poly.edu;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ChatAutomationTest {
    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            // BƯỚC 1: ĐĂNG NHẬP VÀ MỞ TAB ADMIN
            driver.get("http://localhost:8080/login"); 
            String adminTab = driver.getWindowHandle();
            
            // Theo form login: id của ô email là "email", password là "password", nút submit là "ĐĂNG NHẬP"
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys("admin"); 
            driver.findElement(By.id("password")).sendKeys("123456"); 
            driver.findElement(By.xpath("//button[text()='ĐĂNG NHẬP']")).click(); 
            
            wait.until(ExpectedConditions.urlContains("/admin"));
            driver.get("http://localhost:8080/admin/chat");


            // MỞ TAB MỚI, ĐĂNG NHẬP VÀ MỞ TRANG USER
            driver.switchTo().newWindow(WindowType.TAB);
            String userTab = driver.getWindowHandle();

            driver.get("http://localhost:8080/login"); 
            
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys("lehoanganhkha2007@gmail.com");
            driver.findElement(By.id("password")).sendKeys("Kha927$$$"); 
            driver.findElement(By.xpath("//button[text()='ĐĂNG NHẬP']")).click();

            wait.until(ExpectedConditions.urlContains("/home"));
            driver.get("http://localhost:8080/home");
            
            // Đợi 2 giây để Socket kết nối xong
            Thread.sleep(2000); 


            // USER BẮT ĐẦU NHẮN TIN "hello"
            // Nút mở chat có class là .chat-btn-toggle
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".chat-btn-toggle"))).click();
            
            // Ô nhập tin nhắn user có id là chatInput
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("chatInput"))).sendKeys("hello");
                
            // Nút gửi có sự kiện onclick='sendMessage()'
            driver.findElement(By.xpath("//button[@onclick='sendMessage()']")).click();
            System.out.println("User đã gửi tin nhắn: hello");


            // QUAY LẠI TAB ADMIN KIỂM TRA
            driver.switchTo().window(adminTab);
            
            // Code JS của bạn sinh ra id dòng user là: user-row-{email}
            wait.until(ExpectedConditions.elementToBeClickable(By.id("user-row-lehoanganhkha2007@gmail.com"))).click();
            
            // Đợi xem chữ "hello" xuất hiện
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), 'hello')]")));
            System.out.println("✅ Admin ĐÃ NHẬN ĐƯỢC tin nhắn: hello");
            
            // Admin phản hồi lại qua id adminInput và sendBtn
            wait.until(ExpectedConditions.elementToBeClickable(By.id("adminInput")))
                .sendKeys("Hi bạn, Admin đã nhận được tin nhắn!");
            driver.findElement(By.id("sendBtn")).click();
            System.out.println("Admin đã gửi phản hồi.");


           //CHECK LẠI Ở TAB USER XEM CÓ NHẬN ĐƯỢC PHẢN HỒI KHÔNG
            driver.switchTo().window(userTab);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), 'Hi bạn, Admin đã nhận được tin nhắn!')]")));
            System.out.println("✅ HOÀN TẤT: E2E Test thành công! Chat real-time hoạt động tốt 2 chiều.");

            // Dừng 5 giây để bạn xem kết quả trên màn hình
            Thread.sleep(5000); 

        } catch (Exception e) {
            System.out.println("❌ CÓ LỖI XẢY RA TRONG QUÁ TRÌNH TEST:");
            e.printStackTrace();
        } finally {
            driver.quit(); 
        }
    }
    
    
}