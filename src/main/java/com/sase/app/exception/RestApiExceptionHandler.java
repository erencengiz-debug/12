package com.sase.app.exception;

import com.sase.app.dto.common.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.sase.app.controller.api")
public class RestApiExceptionHandler {

    private static String shortCauseMessage(Throwable ex) {
        Throwable t = ex;
        while (t != null) {
            String m = t.getMessage();
            if (m != null && !m.isBlank()) {
                if (m.length() > 280) {
                    return m.substring(0, 277) + "...";
                }
                return m;
            }
            t = t.getCause();
        }
        return "Bilinmeyen neden";
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(EntityNotFoundException ex) {
        log.debug("Kayıt yok: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.debug("Geçersiz istek: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleBinding(MethodArgumentNotValidException ex) {
        FieldError err = ex.getBindingResult().getFieldError();
        String msg = err != null
                ? err.getField() + ": " + (err.getDefaultMessage() != null ? err.getDefaultMessage() : "geçersiz")
                : "Doğrulama hatası";
        log.debug("Validasyon hatası: {}", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableJson(HttpMessageNotReadableException ex) {
        String detail = shortCauseMessage(ex);
        log.warn("JSON okunamadı: {}", detail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("İstek gövdesi geçersiz: " + detail));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String detail = shortCauseMessage(ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause()
                : ex);
        log.warn("Veritabanı bütünlük kısıtı: {}", detail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Kayıt kaydedilemedi (veritabanı kısıtı): " + detail));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccess(DataAccessException ex) {
        String detail = shortCauseMessage(ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause()
                : ex);
        log.error("Veritabanı hatası: {}", detail, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Veritabanı hatası: " + detail));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleOther(Exception ex) {
        log.error("API beklenmeyen hata: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Bir sunucu hatası oluştu."));
    }
}
