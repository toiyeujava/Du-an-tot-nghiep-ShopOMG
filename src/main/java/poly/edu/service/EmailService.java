package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Gửi email reset password
     */
    public void sendPasswordResetEmail(String toEmail, String fullName, String token) {
        String subject = "Đặt lại mật khẩu - ShopOMG";
        String resetUrl = baseUrl + "/reset-password?token=" + token;

        String htmlContent = buildPasswordResetEmailTemplate(fullName, resetUrl);

        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    /**
     * Gửi email xác thực tài khoản
     */
    public void sendVerificationEmail(String toEmail, String fullName, String token) {
        String subject = "Xác thực tài khoản - ShopOMG";
        String verifyUrl = baseUrl + "/verify-email?token=" + token;

        String htmlContent = buildVerificationEmailTemplate(fullName, verifyUrl);

        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    /**
     * Template email đẹp cho reset password
     */
    private String buildPasswordResetEmailTemplate(String fullName, String resetUrl) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }
                        .container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
                        .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; }
                        .header h1 { margin: 0; font-size: 28px; }
                        .content { padding: 40px 30px; color: #333; }
                        .content h2 { color: #667eea; margin-top: 0; }
                        .button { display: inline-block; padding: 14px 32px; background-color: #667eea; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; margin: 20px 0; }
                        .button:hover { background-color: #764ba2; }
                        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 12px; }
                        .warning { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 12px; margin: 20px 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🔐 ShopOMG</h1>
                        </div>
                        <div class="content">
                            <h2>Xin chào %s!</h2>
                            <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>
                            <p>Vui lòng click vào nút bên dưới để tạo mật khẩu mới:</p>
                            <div style="text-align: center;">
                                <a href="%s" class="button">ĐẶT LẠI MẬT KHẨU</a>
                            </div>
                            <p>Hoặc copy link sau vào trình duyệt:</p>
                            <p style="background-color: #f8f9fa; padding: 10px; border-radius: 4px; word-break: break-all; font-size: 12px;">%s</p>
                            <div class="warning">
                                <strong>⚠️ Lưu ý:</strong> Link này chỉ có hiệu lực trong <strong>1 giờ</strong> và chỉ sử dụng được <strong>1 lần</strong>.
                            </div>
                            <p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này. Tài khoản của bạn vẫn an toàn.</p>
                        </div>
                        <div class="footer">
                            <p>© 2026 ShopOMG - Website Thời Trang Hàng Đầu Việt Nam</p>
                            <p>Email này được gửi tự động, vui lòng không trả lời.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(fullName, resetUrl, resetUrl);
    }

    /**
     * Template email đẹp cho verification
     */
    private String buildVerificationEmailTemplate(String fullName, String verifyUrl) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }
                        .container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
                        .header { background: linear-gradient(135deg, #28a745 0%%, #20c997 100%%); color: white; padding: 30px; text-align: center; }
                        .header h1 { margin: 0; font-size: 28px; }
                        .content { padding: 40px 30px; color: #333; }
                        .content h2 { color: #28a745; margin-top: 0; }
                        .button { display: inline-block; padding: 14px 32px; background-color: #28a745; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; margin: 20px 0; }
                        .button:hover { background-color: #20c997; }
                        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 12px; }
                        .info { background-color: #d1ecf1; border-left: 4px solid #0c5460; padding: 12px; margin: 20px 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>✉️ ShopOMG</h1>
                        </div>
                        <div class="content">
                            <h2>Chào mừng %s đến với ShopOMG!</h2>
                            <p>Cảm ơn bạn đã đăng ký tài khoản tại ShopOMG - Website thời trang hàng đầu Việt Nam.</p>
                            <p>Để hoàn tất quá trình đăng ký và bắt đầu mua sắm, vui lòng xác thực địa chỉ email của bạn:</p>
                            <div style="text-align: center;">
                                <a href="%s" class="button">XÁC THỰC EMAIL</a>
                            </div>
                            <p>Hoặc copy link sau vào trình duyệt:</p>
                            <p style="background-color: #f8f9fa; padding: 10px; border-radius: 4px; word-break: break-all; font-size: 12px;">%s</p>
                            <div class="info">
                                <strong>ℹ️ Lưu ý:</strong> Link xác thực có hiệu lực trong <strong>24 giờ</strong>. Sau khi xác thực, bạn có thể đăng nhập và bắt đầu mua sắm ngay!
                            </div>
                            <p>Nếu bạn không đăng ký tài khoản này, vui lòng bỏ qua email này.</p>
                        </div>
                        <div class="footer">
                            <p>© 2026 ShopOMG - Website Thời Trang Hàng Đầu Việt Nam</p>
                            <p>Email này được gửi tự động, vui lòng không trả lời.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(fullName, verifyUrl, verifyUrl);
    }

    /**
     * Gửi email HTML
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML
            helper.setFrom("noreply@shopomg.com");

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email: " + e.getMessage(), e);
        }
    }
}
