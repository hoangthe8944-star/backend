package com.example.beatboxcompany.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom Exception để xử lý trường hợp tài nguyên (Resource) không được tìm thấy.
 * Khi exception này được ném ra, Spring Boot tự động trả về HTTP Status 404 NOT_FOUND.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND) // <--- Chỉ định HTTP Status Code
public class ResourceNotFoundException extends RuntimeException {

    private String resourceName;
    private String fieldName;
    private Object fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        // Ví dụ: Resource User not found with field 'id' = '123'
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    // --- Getters (Tùy chọn, hữu ích cho Debugging/Global Exception Handler) ---
    
    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}