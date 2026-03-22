package poly.edu.controller.common;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

/**
 * ChatMediaController - Upload ảnh/video cho chat
 * POST /api/chat/upload → trả về { url, mediaType }
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatMediaController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private static final long MAX_SIZE   = 20 * 1024 * 1024L; // 20MB
    private static final java.util.Set<String> ALLOWED_TYPES = java.util.Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "video/mp4", "video/webm", "video/ogg"
    );

    @PostMapping("/upload")
    public ResponseEntity<?> uploadMedia(@RequestParam("file") MultipartFile file) {
        try {
            // Validate
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File trống!"));
            }
            if (file.getSize() > MAX_SIZE) {
                return ResponseEntity.badRequest().body(Map.of("error", "File quá 20MB!"));
            }
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Chỉ hỗ trợ ảnh và video!"));
            }

            // Xác định loại media
            String mediaType = contentType.startsWith("image/") ? "image" : "video";

            // Tạo tên file unique
            String ext      = getExtension(file.getOriginalFilename());
            String fileName = "chat_" + UUID.randomUUID() + ext;

            // Lưu vào thư mục uploads/chat/
            Path chatDir = Paths.get(uploadDir, "chat");
            Files.createDirectories(chatDir);
            Files.copy(file.getInputStream(), chatDir.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING);

            String url = "/uploads/chat/" + fileName;
            return ResponseEntity.ok(Map.of("url", url, "mediaType", mediaType));

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Lỗi lưu file: " + e.getMessage()));
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}