package com.example.futurecommunity.exception;

/**
 * 快递业务异常类
 * 用于处理快递相关的业务逻辑异常，如取件码无效、快递已取件等
 */
public class ExpressException extends RuntimeException {
    
    private String code;
    
    public ExpressException(String message) {
        super(message);
        this.code = "EXPRESS_ERROR";
    }
    
    public ExpressException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
}
