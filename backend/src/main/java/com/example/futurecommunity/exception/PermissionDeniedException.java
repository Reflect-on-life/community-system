package com.example.futurecommunity.exception;

public class PermissionDeniedException extends RuntimeException {
    
    private String code;
    
    public PermissionDeniedException(String message) {
        super(message);
        this.code = "PERMISSION_DENIED";
    }
    
    public PermissionDeniedException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
}