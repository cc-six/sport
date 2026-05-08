package com.sporthall.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        if (e.getMessage() != null && (e.getMessage().contains("Token") || e.getMessage().contains("未登录"))) {
            return Result.error(401, e.getMessage());
        }
        return Result.error(e.getMessage());
    }
}
