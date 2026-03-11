package poly.edu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import poly.edu.entity.Order;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

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
    
 
    //Gửi email thông báo trạng thái đơn hàng
    /**
     * Gửi email khi nhân viên CẬP NHẬT TRẠNG THÁI đơn hàng (4.7)
     * @Async → gửi bất đồng bộ, không làm chậm response trả về client
     */
    @Async
    public void sendOrderStatusEmail(Order order) {
        String toEmail = order.getAccount() != null ? order.getAccount().getEmail() : null;
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("[4.9] Đơn #{} không có email khách hàng, bỏ qua.", order.getId());
            return;
        }
        try {
            String subject = buildOrderSubject(order.getStatus(), order.getId());
            String html    = buildOrderStatusHtml(order);
            sendHtmlEmail(toEmail, subject, html);
            log.info("[4.9] Đã gửi email trạng thái '{}' cho đơn #{} → {}", order.getStatus(), order.getId(), toEmail);
        } catch (Exception e) {
            log.error("[4.9] Lỗi gửi email đơn #{}: {}", order.getId(), e.getMessage());
        }
    }

    /**
     * Gửi email khi nhân viên HỦY ĐƠN HÀNG kèm lý do (4.8)
     * @Async → gửi bất đồng bộ, không làm chậm response trả về client
     */
    @Async
    public void sendOrderCancelledEmail(Order order, String reason) {
        String toEmail = order.getAccount() != null ? order.getAccount().getEmail() : null;
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("[4.9] Đơn #{} không có email khách hàng, bỏ qua.", order.getId());
            return;
        }
        try {
            String subject = "❌ Đơn hàng #" + order.getId() + " đã bị hủy - ShopOMG";
            String html    = buildOrderCancelledHtml(order, reason);
            sendHtmlEmail(toEmail, subject, html);
            log.info("[4.9] Đã gửi email hủy đơn #{} → {}", order.getId(), toEmail);
        } catch (Exception e) {
            log.error("[4.9] Lỗi gửi email hủy đơn #{}: {}", order.getId(), e.getMessage());
        }
    }

    // ----------- Template builders  -----------

    private String buildOrderSubject(String status, Integer orderId) {
        return switch (status) {
            case "CONFIRMED" -> "✅ Đơn hàng #" + orderId + " đã được xác nhận - ShopOMG";
            case "SHIPPING"  -> "🚚 Đơn hàng #" + orderId + " đang được giao - ShopOMG";
            case "COMPLETED" -> "🎉 Đơn hàng #" + orderId + " đã giao thành công - ShopOMG";
            default          -> "📦 Cập nhật đơn hàng #" + orderId + " - ShopOMG";
        };
    }

    private String buildOrderStatusHtml(Order order) {
        String name      = order.getReceiverName() != null ? order.getReceiverName() : "Quý khách";
        String phone     = order.getReceiverPhone() != null ? order.getReceiverPhone() : "N/A";
        String address   = order.getShippingAddress() != null ? order.getShippingAddress() : "N/A";
        String amount    = formatCurrency(order.getFinalAmount());
        String color     = statusColor(order.getStatus());
        String icon      = statusIcon(order.getStatus());
        String label     = statusLabel(order.getStatus());
        String bodyMsg   = statusBodyMessage(order.getStatus(), name);

        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8">
            <style>
              body{font-family:'Segoe UI',Arial,sans-serif;background:#f4f4f4;margin:0;padding:0}
              .wrap{max-width:600px;margin:40px auto;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1)}
              .hd{background:linear-gradient(135deg,#1a1a2e,#16213e);padding:30px;text-align:center;color:#fff}
              .hd h1{margin:0;font-size:26px;letter-spacing:2px}
              .hd p{margin:6px 0 0;color:#aaa;font-size:13px}
              .badge{display:inline-block;background:%s;color:#fff;padding:10px 28px;border-radius:50px;font-size:15px;font-weight:bold;margin:24px auto 8px}
              .body{padding:10px 36px 32px}
              .body p{font-size:15px;color:#333;line-height:1.7}
              table.info{width:100%%;border-collapse:collapse;background:#f8f9fa;border-radius:8px;overflow:hidden;border:1px solid #e9ecef;margin-top:18px}
              table.info td{padding:11px 14px;font-size:13px}
              table.info tr+tr td{border-top:1px solid #e9ecef}
              .lbl{color:#888}.val{color:#222;font-weight:600;text-align:right}
              .amt{color:#e74c3c;font-size:15px;font-weight:bold;text-align:right}
              .ft{background:#f8f9fa;padding:18px;text-align:center;color:#999;font-size:12px;border-top:1px solid #eee}
            </style>
            </head>
            <body>
            <div class="wrap">
              <div class="hd"><h1>🛍️ SHOP OMG</h1><p>Thông báo cập nhật đơn hàng</p></div>
              <div style="text-align:center"><div class="badge">%s %s</div></div>
              <div class="body">
                <p>%s</p>
                <table class="info">
                  <tr><td class="lbl">Mã đơn hàng</td><td class="val">#%d</td></tr>
                  <tr><td class="lbl">Người nhận</td><td class="val">%s</td></tr>
                  <tr><td class="lbl">Số điện thoại</td><td class="val">%s</td></tr>
                  <tr><td class="lbl">Địa chỉ giao</td><td class="val">%s</td></tr>
                  <tr><td class="lbl">Tổng thanh toán</td><td class="amt">%s</td></tr>
                </table>
              </div>
              <div class="ft">Cảm ơn bạn đã mua sắm tại <strong>ShopOMG</strong>. Email này được gửi tự động.</div>
            </div>
            </body></html>
            """.formatted(color, icon, label, bodyMsg,
                order.getId(), name, phone, address, amount);
    }

    private String buildOrderCancelledHtml(Order order, String reason) {
        String name    = order.getReceiverName() != null ? order.getReceiverName() : "Quý khách";
        String address = order.getShippingAddress() != null ? order.getShippingAddress() : "N/A";
        String amount  = formatCurrency(order.getFinalAmount());
        String display = (reason != null && !reason.isBlank()) ? reason : "Không có lý do cụ thể";

        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8">
            <style>
              body{font-family:'Segoe UI',Arial,sans-serif;background:#f4f4f4;margin:0;padding:0}
              .wrap{max-width:600px;margin:40px auto;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1)}
              .hd{background:linear-gradient(135deg,#1a1a2e,#16213e);padding:30px;text-align:center;color:#fff}
              .hd h1{margin:0;font-size:26px;letter-spacing:2px}
              .body{padding:24px 36px 32px}
              .body p{font-size:15px;color:#333;line-height:1.7}
              .reason-box{background:#fff5f5;border-left:4px solid #e74c3c;padding:14px 18px;border-radius:0 8px 8px 0;margin:16px 0}
              .reason-box .r-lbl{font-size:12px;color:#999;font-weight:700;text-transform:uppercase;margin:0}
              .reason-box .r-val{font-size:15px;color:#c0392b;font-weight:500;margin:8px 0 0}
              table.info{width:100%%;border-collapse:collapse;background:#f8f9fa;border-radius:8px;overflow:hidden;border:1px solid #e9ecef;margin-top:16px}
              table.info td{padding:11px 14px;font-size:13px}
              table.info tr+tr td{border-top:1px solid #e9ecef}
              .lbl{color:#888}.val{color:#222;font-weight:600;text-align:right}
              .amt{color:#e74c3c;font-size:15px;font-weight:bold;text-align:right}
              .ft{background:#f8f9fa;padding:18px;text-align:center;color:#999;font-size:12px;border-top:1px solid #eee}
            </style>
            </head>
            <body>
            <div class="wrap">
              <div class="hd"><h1>🛍️ SHOP OMG</h1></div>
              <div class="body">
                <p>Xin chào <strong>%s</strong>,<br><br>
                Đơn hàng <strong>#%d</strong> của bạn đã bị hủy bởi nhân viên ShopOMG.</p>
                <div class="reason-box">
                  <p class="r-lbl">Lý do hủy đơn</p>
                  <p class="r-val">%s</p>
                </div>
                <table class="info">
                  <tr><td class="lbl">Mã đơn hàng</td><td class="val">#%d</td></tr>
                  <tr><td class="lbl">Địa chỉ giao</td><td class="val">%s</td></tr>
                  <tr><td class="lbl">Tổng đơn hàng</td><td class="amt">%s</td></tr>
                </table>
                <p style="margin-top:22px">Nếu có thắc mắc, vui lòng liên hệ với chúng tôi để được hỗ trợ. 💙</p>
              </div>
              <div class="ft">© 2026 ShopOMG. Email này được gửi tự động, vui lòng không trả lời.</div>
            </div>
            </body></html>
            """.formatted(name, order.getId(), display, order.getId(), address, amount);
    }

    private String statusColor(String status) {
        return switch (status) {
            case "CONFIRMED" -> "#2ecc71";
            case "SHIPPING"  -> "#3498db";
            case "COMPLETED" -> "#1abc9c";
            default          -> "#95a5a6";
        };
    }

    private String statusIcon(String status) {
        return switch (status) {
            case "CONFIRMED" -> "✅";
            case "SHIPPING"  -> "🚚";
            case "COMPLETED" -> "🎉";
            default          -> "📦";
        };
    }

    private String statusLabel(String status) {
        return switch (status) {
            case "CONFIRMED" -> "Đã xác nhận";
            case "SHIPPING"  -> "Đang giao hàng";
            case "COMPLETED" -> "Giao thành công";
            default          -> status;
        };
    }

    private String statusBodyMessage(String status, String name) {
        return switch (status) {
            case "CONFIRMED" -> "Xin chào <strong>" + name + "</strong>,<br><br>"
                    + "Đơn hàng của bạn đã được <strong>xác nhận</strong>. "
                    + "Chúng tôi sẽ sớm đóng gói và chuyển cho đơn vị vận chuyển. Cảm ơn bạn! 🙏";
            case "SHIPPING"  -> "Xin chào <strong>" + name + "</strong>,<br><br>"
                    + "Đơn hàng của bạn đang được <strong>vận chuyển</strong> đến địa chỉ đã cung cấp. "
                    + "Vui lòng chú ý điện thoại để nhận hàng từ shipper. 🚚";
            case "COMPLETED" -> "Xin chào <strong>" + name + "</strong>,<br><br>"
                    + "Đơn hàng của bạn đã được <strong>giao thành công</strong>! "
                    + "Chúc bạn hài lòng với sản phẩm. Đừng quên để lại đánh giá nhé! 🌟";
            default          -> "Xin chào <strong>" + name + "</strong>,<br><br>"
                    + "Đơn hàng của bạn vừa được cập nhật trạng thái mới.";
        };
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 ₫";
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount) + " ₫";
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
