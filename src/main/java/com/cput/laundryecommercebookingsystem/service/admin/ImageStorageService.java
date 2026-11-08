package com.cput.laundryecommercebookingsystem.service.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/*ImageStorageService.java
 * Saves product pictures in the uploads/products folder and gives back the address to show them.
 * Safety: the file's real bytes are checked (not just its name), only JPEG, PNG and WEBP are accepted
 * (no SVG, which can contain scripts), files get random names, and paths can't escape the folder.
 */
@Service
public class ImageStorageService {

    private static final Logger log = LoggerFactory.getLogger(ImageStorageService.class);
    private static final long MAX_BYTES = 2 * 1024 * 1024;
    public static final String PUBLIC_PREFIX = "/uploads/products/";

    private final Path rootDir;
    private final Path productDir;

    public ImageStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) throws IOException {
        this.rootDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.productDir = rootDir.resolve("products");
        Files.createDirectories(productDir);
    }

    public Path getRootDir() {
        return rootDir;
    }

    /** Stores the picture and returns its address, for example /uploads/products/2f1c....jpg */
    public String storeProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Choose a picture to upload");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("The picture must be 2 MB or smaller");
        }

        String extension;
        try (InputStream in = file.getInputStream()) {
            extension = detectImageType(in.readNBytes(12));
        } catch (IOException e) {
            throw new IllegalArgumentException("The picture could not be read");
        }
        if (extension == null) {
            throw new IllegalArgumentException("Only JPEG, PNG or WEBP pictures are allowed");
        }

        String name = UUID.randomUUID() + "." + extension;
        Path target = productDir.resolve(name).normalize();
        if (!target.startsWith(productDir)) {
            throw new IllegalArgumentException("Invalid file name");
        }
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target);
        } catch (IOException e) {
            log.error("Could not save a product picture", e);
            throw new IllegalStateException("The picture could not be saved. Try again.");
        }
        return PUBLIC_PREFIX + name;
    }

    /** Deletes a picture this service saved earlier. Anything else is ignored. */
    public void delete(String publicUrl) {
        if (publicUrl == null || !publicUrl.startsWith(PUBLIC_PREFIX)) {
            return;
        }
        Path file = productDir.resolve(publicUrl.substring(PUBLIC_PREFIX.length())).normalize();
        if (!file.startsWith(productDir)) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("Could not delete an old product picture");
        }
    }

    /** Looks at the first bytes of the file to see what it really is. */
    private static String detectImageType(byte[] h) {
        if (h.length >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (h.length >= 8 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G'
                && h[4] == 0x0D && h[5] == 0x0A && h[6] == 0x1A && h[7] == 0x0A) {
            return "png";
        }
        if (h.length >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') {
            return "webp";
        }
        return null;
    }
}
