package com.materialmaker.exception;

import com.materialmaker.dto.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiResponse<Void> handleMax(MaxUploadSizeExceededException e) {
        return ApiResponse.fail("文件过大或超出上传限制");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handle(Exception e) {
        return ApiResponse.fail("请求异常：" + e.getMessage());
    }
}
