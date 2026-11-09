package com.cput.laundryecommercebookingsystem.service.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Talent Nocuze
 * 230405886
 */
@Component
public class ProductImageStorage {

    public static final String PUBLIC_PREFIX = "/api/public/product-images/";
    static final long MAX_BYTES = 2L * 1024 * 1024;
    private static final Pattern SAFE_NAME = Pattern.compile("^[a-f0-9\\-]{36}\\.(png|jpg|webp)$");

    private final Path directory;

    public ProductImageStorage(@Value("${app.upload.dir:uploads/product-images}") String dir) {
        this.directory = Paths.get(dir).toAbsolutePath().normalize();
    }


    public String store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Choose an image file to upload.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("The image must be 2 MB or smaller.");
        }
        byte[] bytes = file.getBytes();
        String ext = detectExtension(bytes);
        if (ext == null) {
            throw new IllegalArgumentException("Only PNG, JPEG or WEBP images are allowed.");
        }
        Files.createDirectories(directory);
        String name = UUID.randomUUID() + "." + ext;
        Files.write(directory.resolve(name), bytes);
        return PUBLIC_PREFIX + name;
    }


    public Optional<Path> find(String filename) {
        if (filename == null || !SAFE_NAME.matcher(filename).matches()) {
            return Optional.empty();
        }
        Path path = directory.resolve(filename).normalize();
        return path.startsWith(directory) && Files.isRegularFile(path) ? Optional.of(path) : Optional.empty();
    }


    public void deleteQuietly(String publicPath) {
        if (publicPath == null || !publicPath.startsWith(PUBLIC_PREFIX)) {
            return;
        }
        find(publicPath.substring(PUBLIC_PREFIX.length())).ifPresent(p -> {
            try {
                Files.deleteIfExists(p);
            } catch (IOException ignored) {
                // leaving a file is harmless
            }
        });
    }

    public static String contentTypeFor(String filename) {
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

    static String detectExtension(byte[] b) {
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') return "png";
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return "jpg";
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return "webp";
        return null;
    }
}
