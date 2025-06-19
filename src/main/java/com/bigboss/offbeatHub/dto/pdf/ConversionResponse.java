package com.bigboss.offbeatHub.dto.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionResponse {
    
    private boolean success;
    private String message;
    private LocalDateTime timestamp;
    private Object data;
    
    public static ConversionResponse success(String message) {
        return ConversionResponse.builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ConversionResponse success(String message, Object data) {
        return ConversionResponse.builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
    }
    
    public static ConversionResponse error(String message) {
        return ConversionResponse.builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}