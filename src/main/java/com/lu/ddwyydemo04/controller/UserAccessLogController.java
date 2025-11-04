package com.lu.ddwyydemo04.controller;

import com.lu.ddwyydemo04.Service.UserAccessLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户访问日志控制器
 * 提供前端记录用户访问行为的API接口
 */
@Controller
@RequestMapping("/api/userAccessLog")
public class UserAccessLogController {

    private static final Logger logger = LoggerFactory.getLogger(UserAccessLogController.class);

    @Autowired
    private UserAccessLogService userAccessLogService;

    /**
     * 记录用户访问行为（供前端调用）
     * 用于记录按钮点击、页面操作等前端交互行为
     * 
     * @param requestData 请求数据，包含：
     *                    - username: 用户名（必填）
     *                    - job: 职位/角色（必填）
     *                    - accessPage: 访问页面/按钮名称（必填），例如："导出按钮"、"搜索按钮"、"新增按钮"等
     * @param request HttpServletRequest对象，用于获取User-Agent和IP地址
     * @return 记录结果
     */
    @PostMapping("/record")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> recordUserAccess(
            @RequestBody Map<String, String> requestData,
            HttpServletRequest request) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取请求参数
            String username = requestData.get("username");
            String job = requestData.get("job");
            String accessPage = requestData.get("accessPage");
            
            // 参数验证
            if (username == null || username.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "用户名不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            if (job == null || job.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "职位不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            if (accessPage == null || accessPage.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "访问页面/按钮名称不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 调用Service层记录访问日志
            userAccessLogService.recordUserAccess(username, job, accessPage, request);
            
            result.put("success", true);
            result.put("message", "访问记录已保存");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("记录用户访问失败", e);
            result.put("success", false);
            result.put("message", "记录访问失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 记录用户访问行为（GET方式，方便前端简单调用）
     * 
     * @param username 用户名（必填）
     * @param job 职位/角色（必填）
     * @param accessPage 访问页面/按钮名称（必填）
     * @param request HttpServletRequest对象
     * @return 记录结果
     */
    @GetMapping("/record")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> recordUserAccessGet(
            @RequestParam String username,
            @RequestParam String job,
            @RequestParam String accessPage,
            HttpServletRequest request) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 参数验证
            if (username == null || username.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "用户名不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            if (job == null || job.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "职位不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            if (accessPage == null || accessPage.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "访问页面/按钮名称不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 调用Service层记录访问日志
            userAccessLogService.recordUserAccess(username, job, accessPage, request);
            
            result.put("success", true);
            result.put("message", "访问记录已保存");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("记录用户访问失败", e);
            result.put("success", false);
            result.put("message", "记录访问失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
}







