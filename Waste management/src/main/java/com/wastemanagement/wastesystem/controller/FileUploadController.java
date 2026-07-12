package com.wastemanagement.wastesystem.controller;

import com.wastemanagement.wastesystem.dto.response.ApiResponse;
import com.wastemanagement.wastesystem.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Generic image upload endpoints, used as a preliminary step before
 * submitting a Complaint or CollectionRecord.
 *
 * The frontend flow is always two steps: (1) upload image file(s) here to
 * get back stored URL(s), then (2) submit the actual ComplaintRequest /
 * CollectionRecordRequest JSON body with those URLs already populated in
 * its imageUrls field. This keeps multipart file handling completely
 * separate from the JSON-bodied domain endpoints (ComplaintController,
 * CollectionRecordController), which never need to deal with
 * multipart/form-data themselves.
 *
 * Both endpoints live under "/api/uploads", reachable by CITIZEN and
 * WORKER respectively per SecurityConfig's existing role-scoped matchers
 * ("/api/citizen/**" would need this path added, or — more precisely —
 * these routes are placed here as their own prefix and permitted to any
 * authenticated user, since upload itself carries no sensitive
 * information; the resulting URL only becomes meaningful once attached
 * to a Complaint/CollectionRecord, which IS properly role/ownership
 * checked at that point).
 */
@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    /**
     * POST /api/uploads/complaints (multipart/form-data, field name "files")
     * Uploads one or more complaint evidence images. Returns their stored
     * URLs for the citizen to include in a subsequent
     * POST /api/citizen/complaints call.
     */
    @PostMapping(value = "/complaints", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<List<String>>> uploadComplaintImages(
            @RequestParam("files") List<MultipartFile> files) {
        List<String> urls = fileStorageService.storeImages(files, "complaints");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Images uploaded successfully", urls));
    }

    /**
     * POST /api/uploads/collections (multipart/form-data, field name "files")
     * Uploads one or more before/after proof-of-collection images. Returns
     * their stored URLs for the worker to include in a subsequent
     * POST /api/worker/collection-records call.
     */
    @PostMapping(value = "/collections", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<List<String>>> uploadCollectionImages(
            @RequestParam("files") List<MultipartFile> files) {
        List<String> urls = fileStorageService.storeImages(files, "collections");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Images uploaded successfully", urls));
    }
}