package com.lu.ddwyydemo04.exceptions;
import com.taobao.api.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ExcelOperationException.class)
    public ResponseEntity<String> handleExcelOperationException(ExcelOperationException e) {
        logger.error("操作文件失败:", e.getMessage(), e);
        return ResponseEntity.status(e.getStatusCode()).body(e.getMessage());
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException e) {
        logger.error("钉钉API调用异常: {}", e.getMessage(), e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        String errorMsg = e.getMessage();
        
        if (errorMsg != null) {
            if (errorMsg.contains("timeout") || errorMsg.contains("timed out") || errorMsg.contains("connect")) {
                errorResponse.put("errorCode", -2);
                errorResponse.put("errorMessage", "连接钉钉服务器超时，请检查网络连接后重试");
            } else {
                errorResponse.put("errorCode", -3);
                errorResponse.put("errorMessage", "钉钉API调用失败：" + errorMsg);
            }
        } else {
            errorResponse.put("errorCode", -4);
            errorResponse.put("errorMessage", "获取用户信息失败，请稍后重试");
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

}
