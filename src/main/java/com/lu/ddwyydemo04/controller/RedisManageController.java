package com.lu.ddwyydemo04.controller;

import com.lu.ddwyydemo04.Service.RedisService;
import com.lu.ddwyydemo04.Service.DingTalkUserCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Redis 缓存管理控制器
 * 用于查看和管理 Redis 中的所有缓存数据
 */
@Controller
@RequestMapping("/redis")
public class RedisManageController {

    @Autowired
    private RedisService redisService;

    @Autowired
    private DingTalkUserCacheService userCacheService;

    /**
     * Redis 缓存管理页面
     */
    @GetMapping("/manage")
    public String redisManagePage() {
        return "redisManage";
    }

    /**
     * 获取所有 Redis 键（分类统计）
     */
    @GetMapping("/api/keys/all")
    @ResponseBody
    public Map<String, Object> getAllKeys() {
        Map<String, Object> result = new HashMap<>();
        try {
            Set<String> allKeys = redisService.keys("*");
            
            // 分类统计
            Map<String, List<Map<String, Object>>> categories = new LinkedHashMap<>();
            categories.put("用户缓存", new ArrayList<>());
            categories.put("Access Token", new ArrayList<>());
            categories.put("JSAPI Ticket", new ArrayList<>());
            categories.put("其他缓存", new ArrayList<>());
            
            if (allKeys != null && !allKeys.isEmpty()) {
                for (String key : allKeys) {
                    Map<String, Object> keyInfo = getKeyInfo(key);
                    
                    if (key.startsWith("dingtalk:user:info:")) {
                        categories.get("用户缓存").add(keyInfo);
                    } else if (key.contains("access_token") || key.equals("dingtalk:access_token")) {
                        categories.get("Access Token").add(keyInfo);
                    } else if (key.contains("jsapi_ticket")) {
                        categories.get("JSAPI Ticket").add(keyInfo);
                    } else {
                        categories.get("其他缓存").add(keyInfo);
                    }
                }
            }
            
            result.put("success", true);
            result.put("totalKeys", allKeys != null ? allKeys.size() : 0);
            result.put("categories", categories);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取 Redis 全局统计信息
     */
    @GetMapping("/api/stats/all")
    @ResponseBody
    public Map<String, Object> getAllStats() {
        Map<String, Object> result = new HashMap<>();
        try {
            Set<String> allKeys = redisService.keys("*");
            int totalKeys = allKeys != null ? allKeys.size() : 0;
            
            // 统计不同类型的键
            int userCacheCount = 0;
            int tokenCount = 0;
            int otherCount = 0;
            long totalMemory = 0;
            
            if (allKeys != null) {
                for (String key : allKeys) {
                    if (key.startsWith("dingtalk:user:info:")) {
                        userCacheCount++;
                    } else if (key.contains("token")) {
                        tokenCount++;
                    } else {
                        otherCount++;
                    }
                }
            }
            
            result.put("success", true);
            result.put("totalKeys", totalKeys);
            result.put("userCacheCount", userCacheCount);
            result.put("tokenCount", tokenCount);
            result.put("otherCount", otherCount);
            
            // Access Token 状态
            boolean hasAccessToken = redisService.hasKey("dingtalk:access_token");
            result.put("hasAccessToken", hasAccessToken);
            if (hasAccessToken) {
                Long tokenExpire = redisService.getExpire("dingtalk:access_token");
                result.put("accessTokenExpire", tokenExpire);
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 获取键的详细信息
     */
    @GetMapping("/api/key/detail")
    @ResponseBody
    public Map<String, Object> getKeyDetail(@RequestParam String key) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (!redisService.hasKey(key)) {
                result.put("success", false);
                result.put("error", "键不存在");
                return result;
            }
            
            result.put("success", true);
            result.putAll(getKeyInfo(key));
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    // ==========================================
    // 注意：删除功能已禁用，此页面仅供查看
    // ==========================================

    /**
     * 搜索键
     */
    @GetMapping("/api/keys/search")
    @ResponseBody
    public Map<String, Object> searchKeys(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            Set<String> allKeys = redisService.keys("*" + keyword + "*");
            List<Map<String, Object>> keysList = new ArrayList<>();
            
            if (allKeys != null && !allKeys.isEmpty()) {
                for (String key : allKeys) {
                    keysList.add(getKeyInfo(key));
                }
            }
            
            result.put("success", true);
            result.put("count", keysList.size());
            result.put("keys", keysList);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 获取键的详细信息（内部方法）
     */
    private Map<String, Object> getKeyInfo(String key) {
        Map<String, Object> info = new HashMap<>();
        info.put("key", key);
        
        try {
            // 获取键的类型
            String type = getKeyType(key);
            info.put("type", type);
            
            // 获取过期时间
            Long ttl = redisService.getExpire(key);
            info.put("ttl", ttl);
            info.put("ttlFormatted", formatTTL(ttl));
            
            // 根据类型获取值
            Object value = null;
            String valuePreview = "";
            
            switch (type) {
                case "string":
                    value = redisService.get(key);
                    valuePreview = value != null ? value.toString() : "";
                    if (valuePreview.length() > 100) {
                        valuePreview = valuePreview.substring(0, 100) + "...";
                    }
                    break;
                case "hash":
                    Map<Object, Object> hashValue = redisService.hGetAll(key);
                    value = hashValue;
                    valuePreview = "Hash (" + (hashValue != null ? hashValue.size() : 0) + " 字段)";
                    break;
                case "list":
                    Long listSize = redisService.lLen(key);
                    valuePreview = "List (" + (listSize != null ? listSize : 0) + " 元素)";
                    break;
                case "set":
                    Set<Object> setValue = redisService.sMembers(key);
                    valuePreview = "Set (" + (setValue != null ? setValue.size() : 0) + " 元素)";
                    break;
                default:
                    valuePreview = "未知类型";
            }
            
            info.put("value", value);
            info.put("valuePreview", valuePreview);
            
        } catch (Exception e) {
            info.put("error", e.getMessage());
        }
        
        return info;
    }

    /**
     * 获取键的类型
     */
    private String getKeyType(String key) {
        try {
            // Redis 返回的类型需要通过判断来确定
            if (redisService.hExists(key, "userId") || redisService.hGetAll(key).size() > 0) {
                return "hash";
            }
            Object value = redisService.get(key);
            if (value != null) {
                return "string";
            }
            Long listSize = redisService.lLen(key);
            if (listSize != null && listSize > 0) {
                return "list";
            }
            Set<Object> setValue = redisService.sMembers(key);
            if (setValue != null && !setValue.isEmpty()) {
                return "set";
            }
            return "string";
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 格式化 TTL 显示
     */
    private String formatTTL(Long ttl) {
        if (ttl == null || ttl < 0) {
            return "永久";
        }
        if (ttl < 60) {
            return ttl + "秒";
        }
        if (ttl < 3600) {
            return String.format("%.1f分钟", ttl / 60.0);
        }
        if (ttl < 86400) {
            return String.format("%.1f小时", ttl / 3600.0);
        }
        return String.format("%.1f天", ttl / 86400.0);
    }

    /**
     * 获取所有缓存的用户信息
     */
    @GetMapping("/api/users")
    @ResponseBody
    public Map<String, Object> getAllCachedUsers() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取所有用户缓存键
            Set<String> userKeys = redisService.keys("dingtalk:user:info:*");
            List<Map<String, Object>> users = new ArrayList<>();
            
            if (userKeys != null && !userKeys.isEmpty()) {
                for (String key : userKeys) {
                    Map<Object, Object> userData = redisService.hGetAll(key);
                    if (userData != null && !userData.isEmpty()) {
                        Map<String, Object> user = new HashMap<>();
                        user.put("cacheKey", key);
                        user.put("userId", userData.get("userId"));
                        user.put("username", userData.get("username"));
                        user.put("job", userData.get("job"));
                        user.put("departmentId", userData.get("departmentId"));
                        user.put("departmentName", userData.get("departmentName"));
                        user.put("corpId", userData.get("corpId"));
                        user.put("templatespath", userData.get("templatespath"));
                        user.put("imagepath", userData.get("imagepath"));
                        user.put("savepath", userData.get("savepath"));
                        
                        // 获取过期时间
                        Long ttl = redisService.getExpire(key);
                        user.put("ttl", ttl);
                        user.put("ttlDays", ttl / 86400.0); // 转换为天数
                        
                        users.add(user);
                    }
                }
            }
            
            result.put("success", true);
            result.put("count", users.size());
            result.put("users", users);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取 Redis 缓存统计信息
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public Map<String, Object> getCacheStats() {
        return userCacheService.getCacheStats();
    }

    // ==========================================
    // 用户缓存管理功能已禁用（只读模式）
    // ==========================================

    /**
     * 搜索用户缓存
     */
    @GetMapping("/api/search")
    @ResponseBody
    public Map<String, Object> searchUser(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            Set<String> userKeys = redisService.keys("dingtalk:user:info:*");
            List<Map<String, Object>> matchedUsers = new ArrayList<>();
            
            if (userKeys != null && !userKeys.isEmpty()) {
                for (String key : userKeys) {
                    Map<Object, Object> userData = redisService.hGetAll(key);
                    if (userData != null && !userData.isEmpty()) {
                        String username = (String) userData.get("username");
                        String userId = (String) userData.get("userId");
                        String job = (String) userData.get("job");
                        
                        // 搜索匹配
                        if ((username != null && username.contains(keyword)) ||
                            (userId != null && userId.contains(keyword)) ||
                            (job != null && job.contains(keyword))) {
                            
                            Map<String, Object> user = new HashMap<>();
                            user.put("cacheKey", key);
                            user.put("userId", userId);
                            user.put("username", username);
                            user.put("job", job);
                            user.put("departmentId", userData.get("departmentId"));
                            user.put("ttl", redisService.getExpire(key));
                            
                            matchedUsers.add(user);
                        }
                    }
                }
            }
            
            result.put("success", true);
            result.put("count", matchedUsers.size());
            result.put("users", matchedUsers);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}

