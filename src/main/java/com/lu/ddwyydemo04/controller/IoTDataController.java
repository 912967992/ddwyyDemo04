package com.lu.ddwyydemo04.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Controller
@RequestMapping("/iot")
@CrossOrigin(origins = "*")
public class IoTDataController {

    // 使用内存队列存储接收到的数据，实际项目中建议使用数据库
    private static final Queue<Map<String, Object>> receivedData = new ConcurrentLinkedQueue<>();
    private static final int MAX_RECORDS = 1000; // 最多保存1000条记录

    /**
     * 物联网数据接收页面
     */
    @GetMapping("/receive")
    public String iotReceivePage() {
        return "iotReceive";
    }

    /**
     * 接收物联网数据的接口 - 支持所有HTTP方法和内容类型
     */
    @RequestMapping(value = "/data", 
                   method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    @ResponseBody
    public ResponseEntity<Map<String, Object>> receiveData(
            @RequestParam(required = false) Map<String, String> allParams,
            @RequestHeader Map<String, String> allHeaders,
            HttpServletRequest request) {
        
        try {
            // 创建数据记录
            Map<String, Object> dataRecord = new HashMap<>();
            dataRecord.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            dataRecord.put("method", request.getMethod());
            dataRecord.put("url", request.getRequestURL().toString());
            dataRecord.put("queryParams", allParams != null ? allParams : new HashMap<>());
            dataRecord.put("headers", allHeaders);
            dataRecord.put("contentType", request.getContentType());
            dataRecord.put("remoteAddr", request.getRemoteAddr());
            dataRecord.put("userAgent", request.getHeader("User-Agent"));
            
            // 处理请求体数据
            Map<String, Object> bodyData = new HashMap<>();
            
            // 对于表单数据，参数已经在 allParams 中
            if ("application/x-www-form-urlencoded".equals(request.getContentType()) || 
                "multipart/form-data".equals(request.getContentType())) {
                bodyData.put("formData", allParams != null ? allParams : new HashMap<>());
            } else {
                // 对于其他类型的数据，尝试读取原始请求体
                try {
                    String body = request.getReader().lines().collect(java.util.stream.Collectors.joining("\n"));
                    if (body != null && !body.trim().isEmpty()) {
                        bodyData.put("rawBody", body);
                        
                        // 尝试解析JSON
                        try {
                            Object jsonData = new com.fasterxml.jackson.databind.ObjectMapper().readValue(body, Object.class);
                            bodyData.put("jsonData", jsonData);
                        } catch (Exception e) {
                            // 不是JSON格式，保持原始数据
                        }
                    }
                } catch (Exception e) {
                    bodyData.put("error", "无法读取请求体: " + e.getMessage());
                }
            }
            
            dataRecord.put("body", bodyData);
            
            // 添加到队列
            receivedData.offer(dataRecord);
            
            // 保持队列大小不超过最大值
            while (receivedData.size() > MAX_RECORDS) {
                receivedData.poll();
            }
            
            // 返回成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "数据接收成功");
            response.put("timestamp", dataRecord.get("timestamp"));
            response.put("totalRecords", receivedData.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "数据接收失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 专门处理JSON数据的接口
     */
    @PostMapping(value = "/data/json", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> receiveJsonData(
            @RequestBody(required = false) Map<String, Object> requestBody,
            @RequestParam(required = false) Map<String, String> allParams,
            @RequestHeader Map<String, String> allHeaders,
            HttpServletRequest request) {
        
        try {
            // 创建数据记录
            Map<String, Object> dataRecord = new HashMap<>();
            dataRecord.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            dataRecord.put("method", request.getMethod());
            dataRecord.put("url", request.getRequestURL().toString());
            dataRecord.put("queryParams", allParams != null ? allParams : new HashMap<>());
            dataRecord.put("headers", allHeaders);
            dataRecord.put("contentType", request.getContentType());
            dataRecord.put("remoteAddr", request.getRemoteAddr());
            dataRecord.put("userAgent", request.getHeader("User-Agent"));
            dataRecord.put("body", requestBody != null ? requestBody : new HashMap<>());
            
            // 添加到队列
            receivedData.offer(dataRecord);
            
            // 保持队列大小不超过最大值
            while (receivedData.size() > MAX_RECORDS) {
                receivedData.poll();
            }
            
            // 返回成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "JSON数据接收成功");
            response.put("timestamp", dataRecord.get("timestamp"));
            response.put("totalRecords", receivedData.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "JSON数据接收失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取所有接收到的数据
     */
    @GetMapping("/data/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllData() {
        try {
            List<Map<String, Object>> dataList = new ArrayList<>(receivedData);
            Collections.reverse(dataList); // 最新的数据在前
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", dataList);
            response.put("total", dataList.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取数据失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 清空所有数据
     */
    @DeleteMapping("/data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearAllData() {
        try {
            receivedData.clear();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "数据已清空");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "清空数据失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取数据统计信息
     */
    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalRecords", receivedData.size());
            stats.put("maxRecords", MAX_RECORDS);
            
            // 统计各种HTTP方法的使用次数
            Map<String, Integer> methodStats = new HashMap<>();
            for (Map<String, Object> record : receivedData) {
                String method = (String) record.get("method");
                methodStats.put(method, methodStats.getOrDefault(method, 0) + 1);
            }
            stats.put("methodStats", methodStats);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
