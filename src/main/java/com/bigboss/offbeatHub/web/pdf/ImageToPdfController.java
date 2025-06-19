package com.bigboss.offbeatHub.web.pdf;

import com.bigboss.offbeatHub.dto.pdf.ConversionResponse;
import com.bigboss.offbeatHub.service.pdf.ImageToPdfConverterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/pdf-converter")
@RequiredArgsConstructor
public class ImageToPdfController {
    
    private final ImageToPdfConverterService converterService;
    
    /**
     * 단일 이미지를 PDF로 변환
     */
    @PostMapping("/single")
    public ResponseEntity<?> convertSingleImageToPdf(
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam(value = "filename", required = false) String customFilename) {
        
        try {
            log.info("Starting single image to PDF conversion for file: {}", imageFile.getOriginalFilename());
            
            byte[] pdfBytes = converterService.convertImageToPdf(imageFile);
            
            String filename = generatePdfFilename(customFilename, imageFile.getOriginalFilename());
            HttpHeaders headers = createPdfResponseHeaders(filename);
            
            log.info("Successfully converted single image to PDF: {}", filename);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
                    
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for single image conversion: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ConversionResponse.error("Invalid input: " + e.getMessage()));
                    
        } catch (Exception e) {
            log.error("Error converting single image to PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ConversionResponse.error("Conversion failed: " + e.getMessage()));
        }
    }
    
    /**
     * 여러 이미지를 하나의 PDF로 변환
     */
    @PostMapping("/multiple")
    public ResponseEntity<?> convertMultipleImagesToPdf(
            @RequestParam("images") List<MultipartFile> imageFiles,
            @RequestParam(value = "filename", required = false) String customFilename) {
        
        try {
            log.info("Starting multiple images to PDF conversion for {} files", imageFiles.size());
            
            byte[] pdfBytes = converterService.convertImagesToPdf(imageFiles);
            
            String filename = generatePdfFilename(customFilename, "merged_images.pdf");
            HttpHeaders headers = createPdfResponseHeaders(filename);
            
            log.info("Successfully converted {} images to PDF: {}", imageFiles.size(), filename);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
                    
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for multiple images conversion: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ConversionResponse.error("Invalid input: " + e.getMessage()));
                    
        } catch (Exception e) {
            log.error("Error converting multiple images to PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ConversionResponse.error("Conversion failed: " + e.getMessage()));
        }
    }
    
    /**
     * Base64 인코딩된 이미지를 PDF로 변환
     */
    @PostMapping("/base64")
    public ResponseEntity<?> convertBase64ImageToPdf(
            @RequestBody Base64ImageRequest request) {
        
        try {
            log.info("Starting base64 image to PDF conversion");
            
            // Base64 디코딩
            byte[] imageBytes = java.util.Base64.getDecoder().decode(request.getImageData());
            
            byte[] pdfBytes = converterService.convertImageByteArrayToPdf(
                    imageBytes, request.getFilename());
            
            String filename = generatePdfFilename(request.getFilename(), "base64_image.pdf");
            HttpHeaders headers = createPdfResponseHeaders(filename);
            
            log.info("Successfully converted base64 image to PDF: {}", filename);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
                    
        } catch (IllegalArgumentException e) {
            log.warn("Invalid base64 image conversion request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ConversionResponse.error("Invalid input: " + e.getMessage()));
                    
        } catch (Exception e) {
            log.error("Error converting base64 image to PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ConversionResponse.error("Conversion failed: " + e.getMessage()));
        }
    }
    
    /**
     * 서비스 상태 확인
     */
    @GetMapping("/health")
    public ResponseEntity<ConversionResponse> healthCheck() {
        return ResponseEntity.ok(ConversionResponse.success("PDF Converter Service is running"));
    }
    
    /**
     * PDF 파일명 생성
     */
    private String generatePdfFilename(String customFilename, String originalFilename) {
        if (customFilename != null && !customFilename.trim().isEmpty()) {
            return customFilename.endsWith(".pdf") ? customFilename : customFilename + ".pdf";
        }
        
        if (originalFilename != null) {
            String nameWithoutExtension = originalFilename.replaceFirst("[.][^.]+$", "");
            return nameWithoutExtension + ".pdf";
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "converted_image_" + timestamp + ".pdf";
    }
    
    /**
     * PDF 응답 헤더 생성
     */
    private HttpHeaders createPdfResponseHeaders(String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return headers;
    }
    
    /**
     * Base64 이미지 요청 DTO
     */
    public static class Base64ImageRequest {
        private String imageData;
        private String filename;
        
        public String getImageData() {
            return imageData;
        }
        
        public void setImageData(String imageData) {
            this.imageData = imageData;
        }
        
        public String getFilename() {
            return filename;
        }
        
        public void setFilename(String filename) {
            this.filename = filename;
        }
    }
}