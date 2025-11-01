package com.lu.ddwyydemo04.Service;

import com.lu.ddwyydemo04.dao.UserAccessLogDao;
import com.lu.ddwyydemo04.pojo.UserAccessLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 用户访问日志服务类
 * 用于封装记录用户访问日志的功能
 */
@Service
public class UserAccessLogService {

    private static final Logger logger = LoggerFactory.getLogger(UserAccessLogService.class);

    @Autowired
    private UserAccessLogDao userAccessLogDao;

    /**
     * 记录用户访问日志
     * 
     * @param username 用户名
     * @param job 职位/角色
     * @param accessPage 访问页面（由调用者自定义，例如："首页"、"测试报告页面"、"新品进度管理"等）
     * @param request HttpServletRequest对象，用于获取User-Agent和IP地址
     */
    public void recordUserAccess(String username, String job, String accessPage, HttpServletRequest request) {
        try {
            // 获取浏览器User-Agent信息并转换为简洁格式
            String rawUserAgent = request.getHeader("User-Agent");
            String userAgent = parseUserAgent(rawUserAgent);
            
            // 获取IP地址（考虑代理情况）
            String ipAddress = getClientIpAddress(request);
            
            // 创建访问记录
            UserAccessLog accessLog = new UserAccessLog();
            accessLog.setUsername(username);
            accessLog.setJob(job);
            accessLog.setAccessTime(LocalDateTime.now());
            accessLog.setUserAgent(userAgent);
            accessLog.setIpAddress(ipAddress);
            accessLog.setAccessPage(accessPage);
            
            // 插入访问记录
            userAccessLogDao.insertUserAccessLog(accessLog);
            
            // 查询访问系统的次数（访问系统的值）
            Integer accessSystemCount = userAccessLogDao.getAccessSystemCountByUsername(username);
            logger.info("用户访问记录已保存: 用户={}, 页面={}, 访问系统的值={}, IP={}", 
                    username, accessPage, accessSystemCount, ipAddress);
        } catch (Exception e) {
            // 访问记录失败不影响主要业务流程，只记录日志
            logger.error("保存用户访问记录失败: 用户={}, 页面={}", username, accessPage, e);
        }
    }

    /**
     * 解析User-Agent，转换为简洁格式
     * PC端：显示系统+（PC），例如：Windows 10（PC）
     * 手机端：显示品牌，例如：iPhone、华为、小米等
     */
    private String parseUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "未知";
        }
        
        String ua = userAgent.toLowerCase();
        
        // 检测是否为移动设备（手机或平板）
        boolean isMobile = (ua.contains("mobile") && !ua.contains("ipad")) || 
                          (ua.contains("android") && ua.contains("mobile")) ||
                          ua.contains("iphone") || ua.contains("ipod") ||
                          ua.contains("windows phone");
        
        if (ua.contains("ipad")) {
            return "iPad（平板）";
        } else if (isMobile) {
            // 手机设备：提取品牌
            if (ua.contains("iphone")) {
                return "iPhone（手机）";
            } else if (ua.contains("android")) {
                // Android设备，尝试提取品牌（按常见品牌顺序检测）
                if (ua.contains("huawei") || ua.contains("honor")) {
                    return "华为（手机）";
                } else if (ua.contains("xiaomi") || ua.contains("redmi")) {
                    return "小米（手机）";
                } else if (ua.contains("oppo")) {
                    return "OPPO（手机）";
                } else if (ua.contains("vivo")) {
                    return "vivo（手机）";
                } else if (ua.contains("samsung")) {
                    return "三星（手机）";
                } else if (ua.contains("oneplus")) {
                    return "一加（手机）";
                } else if (ua.contains("meizu")) {
                    return "魅族（手机）";
                } else if (ua.contains("realme")) {
                    return "realme（手机）";
                } else {
                    return "Android（手机）";
                }
            } else if (ua.contains("windows phone")) {
                return "Windows Phone（手机）";
            } else {
                return "移动设备";
            }
        } else {
            // PC设备：提取操作系统
            if (ua.contains("windows")) {
                // 提取Windows版本（按新版本到旧版本顺序）
                if (ua.contains("windows nt 10.0") || ua.contains("windows 10")) {
                    return "Windows 10（PC）";
                } else if (ua.contains("windows nt 11.0") || ua.contains("windows 11")) {
                    return "Windows 11（PC）";
                } else if (ua.contains("windows nt 6.3") || ua.contains("windows 8.1")) {
                    return "Windows 8.1（PC）";
                } else if (ua.contains("windows nt 6.2") || ua.contains("windows 8")) {
                    return "Windows 8（PC）";
                } else if (ua.contains("windows nt 6.1") || ua.contains("windows 7")) {
                    return "Windows 7（PC）";
                } else if (ua.contains("windows nt 6.0") || ua.contains("windows vista")) {
                    return "Windows Vista（PC）";
                } else if (ua.contains("windows nt 5.1") || ua.contains("windows xp")) {
                    return "Windows XP（PC）";
                } else {
                    return "Windows（PC）";
                }
            } else if (ua.contains("mac os x") || ua.contains("macintosh")) {
                // 提取macOS版本
                if (ua.contains("mac os x 10_15") || ua.contains("mac os x 10.15") || ua.contains("macos 10.15")) {
                    return "macOS Catalina（PC）";
                } else if (ua.contains("mac os x 10_14") || ua.contains("mac os x 10.14") || ua.contains("macos 10.14")) {
                    return "macOS Mojave（PC）";
                } else if (ua.contains("mac os x 10_13") || ua.contains("mac os x 10.13") || ua.contains("macos 10.13")) {
                    return "macOS High Sierra（PC）";
                } else if (ua.contains("macos 11") || ua.contains("mac os x 11")) {
                    return "macOS Big Sur（PC）";
                } else if (ua.contains("macos 12") || ua.contains("mac os x 12")) {
                    return "macOS Monterey（PC）";
                } else if (ua.contains("macos 13") || ua.contains("mac os x 13")) {
                    return "macOS Ventura（PC）";
                } else if (ua.contains("macos 14") || ua.contains("mac os x 14")) {
                    return "macOS Sonoma（PC）";
                } else if (ua.contains("mac os x") || ua.contains("macos")) {
                    return "macOS（PC）";
                } else {
                    return "macOS（PC）";
                }
            } else if (ua.contains("linux")) {
                return "Linux（PC）";
            } else if (ua.contains("unix")) {
                return "Unix（PC）";
            } else {
                return "PC";
            }
        }
    }

    /**
     * 获取客户端真实IP地址
     * 考虑了代理、Nginx等反向代理的情况
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理多个IP的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}

