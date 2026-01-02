package poly.edu.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileService {
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public String save(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;
        String ext = getExtension(file.getOriginalFilename());
        String name = UUID.randomUUID().toString().replaceAll("-", "");
        String filename = name + (ext.isEmpty() ? "" : ("." + ext));
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        Path dest = dir.resolve(filename);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/" + filename; // public URL
    }

    private String getExtension(String original) {
        if (original == null) return "";
        int dot = original.lastIndexOf('.');
        if (dot < 0) return "";
        return original.substring(dot + 1);
    }
}