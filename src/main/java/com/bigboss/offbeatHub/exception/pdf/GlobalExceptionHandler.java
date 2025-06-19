package com.bigboss.offbeatHub.exception.pdf;

import com.bigboss.offbeatHub.dto.pdf.ConversionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 파일 업로드 크기 초과 예외 처리
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ConversionResponse> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e) {
        log.warn("File upload size exceeded: {}", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ConversionResponse.error("파일 크기가 너무 큽니다. 최대 10MB까지 업로드 가능합니다."));
    }
    
    /**
     * 멀티파트 예외 처리
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ConversionResponse> handleMultipartException(MultipartException e) {
        log.warn("Multipart request error: {}", e.getMessage());
        
        return ResponseEntity.badRequest()
                .body(ConversionResponse.error("파일 업로드 중 오류가 발생했습니다: " + e.getMessage()));
    }
    
    /**
     * 잘못된 인자 예외 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ConversionResponse> handleIllegalArgumentException(
            IllegalArgumentException e) {
        log.warn("Invalid argument: {}", e.getMessage());
        
        return ResponseEntity.badRequest()
                .body(ConversionResponse.error("잘못된 요청입니다: " + e.getMessage()));
    }
    
    /**
     * 일반적인 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ConversionResponse> handleGeneralException(Exception e) {
        log.error("Unexpected error occurred", e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ConversionResponse.error("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }
}