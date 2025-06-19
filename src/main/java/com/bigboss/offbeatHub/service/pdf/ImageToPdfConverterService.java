package com.bigboss.offbeatHub.service.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class ImageToPdfConverterService {
    
    private static final List<String> SUPPORTED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp"
    );
    
    private static final float DEFAULT_MARGIN = 36f; // 0.5 inch
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_FILES_COUNT = 50;
    
    /**
     * 단일 이미지 파일을 PDF로 변환
     */
    public byte[] convertImageToPdf(MultipartFile imageFile) throws Exception {
        validateImageFile(imageFile);
        
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = createPdfDocument();
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            
            document.open();
            
            // 이미지를 PDF에 추가
            addImageToDocument(document, imageFile);
            
            document.close();
            writer.close();
            
            log.info("Successfully converted image {} to PDF", imageFile.getOriginalFilename());
            return outputStream.toByteArray();
        }
    }
    
    /**
     * 여러 이미지 파일들을 하나의 PDF로 변환
     */
    public byte[] convertImagesToPdf(List<MultipartFile> imageFiles) throws Exception {
        validateImageFiles(imageFiles);
        
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = createPdfDocument();
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            
            document.open();
            
            // 각 이미지를 새 페이지에 추가
            for (int i = 0; i < imageFiles.size(); i++) {
                if (i > 0) {
                    document.newPage();
                }
                addImageToDocument(document, imageFiles.get(i));
            }
            
            document.close();
            writer.close();
            
            log.info("Successfully converted {} images to PDF", imageFiles.size());
            return outputStream.toByteArray();
        }
    }
    
    /**
     * 바이트 배열 이미지를 PDF로 변환
     */
    public byte[] convertImageByteArrayToPdf(byte[] imageData, String fileName) throws Exception {
        if (imageData == null || imageData.length == 0) {
            throw new IllegalArgumentException("Image data cannot be null or empty");
        }
        
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = createPdfDocument();
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            
            document.open();
            
            // 바이트 배열을 이미지로 변환
            Image pdfImage = Image.getInstance(imageData);
            fitImageToPage(document, pdfImage);
            document.add(pdfImage);
            
            document.close();
            writer.close();
            
            log.info("Successfully converted byte array image {} to PDF", fileName);
            return outputStream.toByteArray();
        }
    }
    
    /**
     * PDF 문서 생성 및 기본 설정
     */
    private Document createPdfDocument() {
        Document document = new Document(PageSize.A4);
        document.setMargins(DEFAULT_MARGIN, DEFAULT_MARGIN, DEFAULT_MARGIN, DEFAULT_MARGIN);
        
        // 메타데이터 설정
        document.addTitle("Image to PDF Conversion");
        document.addAuthor("Image Converter Service");
        document.addCreator("Spring Boot Application");
        document.addCreationDate();
        
        return document;
    }
    
    /**
     * 이미지를 PDF 문서에 추가
     */
    private void addImageToDocument(Document document, MultipartFile imageFile) throws Exception {
        try (InputStream inputStream = imageFile.getInputStream()) {
            byte[] imageBytes = IOUtils.toByteArray(inputStream);
            
            // 이미지 크기 최적화
            byte[] optimizedImageBytes = optimizeImageSize(imageBytes);
            
            Image pdfImage = Image.getInstance(optimizedImageBytes);
            fitImageToPage(document, pdfImage);
            
            document.add(pdfImage);
            
            log.debug("Added image {} to PDF document", imageFile.getOriginalFilename());
        }
    }
    
    /**
     * 이미지를 페이지 크기에 맞게 조정
     */
    private void fitImageToPage(Document document, Image image) {
        float documentWidth = document.getPageSize().getWidth() - (DEFAULT_MARGIN * 2);
        float documentHeight = document.getPageSize().getHeight() - (DEFAULT_MARGIN * 2);
        
        float imageWidth = image.getWidth();
        float imageHeight = image.getHeight();
        
        // 이미지가 페이지보다 큰 경우 비율을 유지하며 축소
        if (imageWidth > documentWidth || imageHeight > documentHeight) {
            float widthRatio = documentWidth / imageWidth;
            float heightRatio = documentHeight / imageHeight;
            float scaleRatio = Math.min(widthRatio, heightRatio);
            
            image.scalePercent(scaleRatio * 100);
        }
        
        // 이미지를 페이지 중앙에 배치
        image.setAlignment(Element.ALIGN_CENTER);
    }
    
    /**
     * 이미지 크기 최적화 (메모리 효율성을 위해)
     */
    private byte[] optimizeImageSize(byte[] imageBytes) throws Exception {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes)) {
            BufferedImage bufferedImage = ImageIO.read(bis);
            
            if (bufferedImage == null) {
                throw new IllegalArgumentException("Invalid image format");
            }
            
            // 이미지가 너무 큰 경우 리사이징
            int maxWidth = 2048;
            int maxHeight = 2048;
            
            if (bufferedImage.getWidth() > maxWidth || bufferedImage.getHeight() > maxHeight) {
                return resizeImage(bufferedImage, maxWidth, maxHeight);
            }
            
            return imageBytes;
        }
    }
    
    /**
     * 이미지 리사이징
     */
    private byte[] resizeImage(BufferedImage originalImage, int maxWidth, int maxHeight) throws Exception {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double scaleRatio = Math.min(widthRatio, heightRatio);
        
        int newWidth = (int) (originalWidth * scaleRatio);
        int newHeight = (int) (originalHeight * scaleRatio);
        
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        resizedImage.createGraphics().drawImage(
            originalImage.getScaledInstance(newWidth, newHeight, java.awt.Image.SCALE_SMOOTH),
            0, 0, null
        );
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(resizedImage, "jpg", baos);
            return baos.toByteArray();
        }
    }
    
    /**
     * 단일 이미지 파일 유효성 검증
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file cannot be null or empty");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                String.format("File size exceeds maximum limit of %d MB", MAX_FILE_SIZE / (1024 * 1024))
            );
        }
        
        String contentType = file.getContentType();
        if (!SUPPORTED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                String.format("Unsupported image type: %s. Supported types: %s", 
                    contentType, String.join(", ", SUPPORTED_IMAGE_TYPES))
            );
        }
    }
    
    /**
     * 여러 이미지 파일들 유효성 검증
     */
    private void validateImageFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Image files list cannot be null or empty");
        }
        
        if (files.size() > MAX_FILES_COUNT) {
            throw new IllegalArgumentException(
                String.format("Number of files exceeds maximum limit of %d", MAX_FILES_COUNT)
            );
        }
        
        for (MultipartFile file : files) {
            validateImageFile(file);
        }
    }
}