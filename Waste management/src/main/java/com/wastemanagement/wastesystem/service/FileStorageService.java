package com.wastemanagement.wastesystem.service;

import com.wastemanagement.wastesystem.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Handles physical storage of uploaded image files (complaint evidence from
 * SubmitComplaint.js, proof-of-collection images from UploadWasteImages.js)
 * and returns the URL path to persist on Complaint.imageUrls /
 * CollectionRecord.imageUrls.
 *
 * Files are stored on local disk under app.upload.dir (see
 * application.properties), organized into a subfolder per use case
 * (complaints/, collections/) for basic organization. Static serving of
 * these files back to the frontend is handled separately by
 * WebMvcConfig's resource handler mapping "/uploads/**" to this same
 * directory (upcoming), matching the "/uploads/**" permitAll() matcher
 * already present in SecurityConfig.
 *
 * A production deployment would more likely use cloud object storage
 * (S3, Azure Blob, etc.) instead of local disk — this local-disk
 * implementation is deliberately kept as the simplest correct approach
 * appropriate for a college/hackathon-scale deployment (Rule 18), while
 * being isolated behind this single service so swapping the storage
 * backend later would only require changing this one class.
 */
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024; // 10MB, matches application.properties

    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * Saves a single uploaded image under the given subfolder (e.g.
     * "complaints" or "collections"), returning the relative URL path
     * (e.g. "/uploads/complaints/3f2a1c9e-....jpg") to store on the
     * owning document.
     */
    public String storeImage(MultipartFile file, String subfolder) {
        validateFile(file);

        String extension = extractExtension(file.getOriginalFilename());
        String storedFilename = UUID.randomUUID() + "." + extension;

        try {
            Path targetDir = Paths.get(uploadDir, subfolder).toAbsolutePath().normalize();
            Files.createDirectories(targetDir);

            Path targetFile = targetDir.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BadRequestException("Failed to store uploaded file: " + file.getOriginalFilename());
        }

        return "/uploads/" + subfolder + "/" + storedFilename;
    }

    /**
     * Saves multiple uploaded images in one call, e.g. all photos attached
     * to a single complaint submission or a single collection log entry.
     */
    public List<String> storeImages(List<MultipartFile> files, String subfolder) {
        return files.stream()
                .map(file -> storeImage(file, subfolder))
                .toList();
    }

    /**
     * Rejects empty files, oversized files, and disallowed file types
     * before ever touching the filesystem — cheap validation upfront
     * avoids partially writing an invalid upload.
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("Uploaded file exceeds the 10MB size limit");
        }

        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BadRequestException(
                    "Unsupported file type. Allowed types: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    /**
     * Extracts a file's extension for both validation and to build the
     * stored (UUID-randomized) filename. The original filename is never
     * reused directly as the stored filename — deliberately avoids path
     * traversal risks (e.g. a malicious filename like "../../etc/passwd")
     * and filename collisions between different uploaders.
     */
    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BadRequestException("Uploaded file must have a valid file extension");
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
    }
}