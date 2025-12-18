package com.lu.ddwyydemo04.Service;

import com.lu.ddwyydemo04.pojo.TestIssues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 问题库缓存服务
 * 用于缓存问题库的所有数据，提升搜索性能
 */
@Service
public class ProblemLibraryCacheService {

    @Autowired
    private RedisService redisService;

    // 缓存键名
    private static final String PROBLEM_LIBRARY_ALL_DATA_KEY = "problemLibrary:all:data";
    private static final String PROBLEM_LIBRARY_CACHE_TIME_KEY = "problemLibrary:cache:time";
    
    // 缓存有效期（1小时）
    private static final long CACHE_DURATION = 1;
    private static final TimeUnit CACHE_TIME_UNIT = TimeUnit.HOURS;

    /**
     * 缓存所有问题点数据
     * @param problems 问题点列表
     */
    public void cacheAllProblems(List<TestIssues> problems) {
        try {
            System.out.println("🔄 开始缓存问题库数据，共 " + problems.size() + " 条");
            
            // 先删除旧缓存（避免类型冲突）
            redisService.delete(PROBLEM_LIBRARY_ALL_DATA_KEY);
            
            // 使用 Redis List 结构存储每个问题点对象
            // 这样可以避免序列化整个大对象导致的问题
            for (TestIssues problem : problems) {
                redisService.rPush(PROBLEM_LIBRARY_ALL_DATA_KEY, problem);
            }
            
            // 设置过期时间
            redisService.expire(PROBLEM_LIBRARY_ALL_DATA_KEY, CACHE_DURATION, CACHE_TIME_UNIT);
            
            // 记录缓存时间（使用可读的日期时间格式）
            String cacheTimeStr = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            redisService.set(PROBLEM_LIBRARY_CACHE_TIME_KEY, 
                "缓存时间: " + cacheTimeStr + ", 共 " + problems.size() + " 条数据", 
                CACHE_DURATION, CACHE_TIME_UNIT);
            
            System.out.println("✅ 问题库数据已缓存到Redis（List结构），有效期: " + CACHE_DURATION + " 小时");
        } catch (Exception e) {
            System.err.println("❌ 缓存问题库数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 从缓存中获取所有问题点数据
     * @return 问题点列表，如果缓存不存在返回null
     */
    @SuppressWarnings("unchecked")
    public List<TestIssues> getCachedProblems() {
        try {
            // 检查缓存是否存在
            if (!redisService.hasKey(PROBLEM_LIBRARY_ALL_DATA_KEY)) {
                System.out.println("⚠️ 问题库缓存不存在");
                return null;
            }
            
            // 先尝试获取List长度，如果失败说明类型不对（旧缓存是String类型）
            Long listSize = null;
            try {
                listSize = redisService.lLen(PROBLEM_LIBRARY_ALL_DATA_KEY);
            } catch (org.springframework.data.redis.RedisSystemException e) {
                // 类型错误，说明是旧的String缓存，自动清除
                System.out.println("⚠️ 检测到旧格式缓存（String类型），自动清除...");
                clearCache();
                return null;
            }
            
            if (listSize == null || listSize == 0) {
                System.out.println("⚠️ 问题库缓存为空");
                return null;
            }
            
            System.out.println("📥 从Redis List读取 " + listSize + " 条数据...");
            
            // 使用 LRANGE 一次性获取所有元素（0 到 -1 表示全部）
            List<Object> cachedObjects = redisService.lRange(PROBLEM_LIBRARY_ALL_DATA_KEY, 0, -1);
            
            if (cachedObjects == null || cachedObjects.isEmpty()) {
                System.out.println("⚠️ 读取到的缓存数据为空");
                return null;
            }
            
            // 转换为 TestIssues 列表
            List<TestIssues> problems = new ArrayList<>((int) listSize.longValue());
            for (Object obj : cachedObjects) {
                if (obj instanceof TestIssues) {
                    problems.add((TestIssues) obj);
                } else if (obj instanceof LinkedHashMap) {
                    // 如果是 LinkedHashMap（RedisTemplate 反序列化的中间格式），需要手动转换
                    System.out.println("⚠️ 检测到 LinkedHashMap 格式，清除缓存...");
                    clearCache();
                    return null;
                } else {
                    System.err.println("⚠️ 缓存中的对象类型不匹配: " + (obj != null ? obj.getClass() : "null"));
                    clearCache();
                    return null;
                }
            }
            
            System.out.println("✅ 从Redis缓存获取问题库数据，共 " + problems.size() + " 条");
            return problems;
            
        } catch (Exception e) {
            System.err.println("❌ 获取缓存失败: " + e.getMessage());
            // 缓存读取失败，清除可能损坏的缓存
            System.out.println("🗑️ 自动清除损坏的缓存...");
            clearCache();
            return null;
        }
    }

    /**
     * 检查缓存是否存在且有效
     * @return true-缓存有效，false-缓存不存在或已过期
     */
    public boolean isCacheValid() {
        return redisService.hasKey(PROBLEM_LIBRARY_ALL_DATA_KEY);
    }

    /**
     * 清除问题库缓存（当数据更新时调用）
     */
    public void clearCache() {
        try {
            redisService.delete(PROBLEM_LIBRARY_ALL_DATA_KEY);
            redisService.delete(PROBLEM_LIBRARY_CACHE_TIME_KEY);
            System.out.println("🗑️ 问题库缓存已清除");
        } catch (Exception e) {
            System.err.println("❌ 清除缓存失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取缓存状态信息
     * @return 缓存状态Map
     */
    public Map<String, Object> getCacheStatus() {
        Map<String, Object> status = new HashMap<>();
        
        boolean isValid = isCacheValid();
        status.put("cached", isValid);
        
        if (isValid) {
            Long ttl = redisService.getExpire(PROBLEM_LIBRARY_ALL_DATA_KEY);
            status.put("ttlSeconds", ttl);
            status.put("ttlMinutes", ttl != null ? ttl / 60 : 0);
            
            // 获取数据量
            Long dataCount = redisService.lLen(PROBLEM_LIBRARY_ALL_DATA_KEY);
            status.put("dataCount", dataCount);
            
            // 获取缓存时间信息（现在是可读字符串格式）
            Object cacheTimeInfo = redisService.get(PROBLEM_LIBRARY_CACHE_TIME_KEY);
            if (cacheTimeInfo != null) {
                String cacheTimeStr = cacheTimeInfo.toString();
                status.put("cacheTimeInfo", cacheTimeStr);
                
                // 尝试从字符串中提取时间
                // 格式：缓存时间: 2024-11-17 15:48:21, 共 12 条数据
                try {
                    if (cacheTimeStr.contains("缓存时间: ")) {
                        String timeStr = cacheTimeStr.substring(
                            cacheTimeStr.indexOf("缓存时间: ") + 6, 
                            cacheTimeStr.indexOf(",")
                        );
                        java.time.LocalDateTime cachedAt = java.time.LocalDateTime.parse(
                            timeStr, 
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        );
                        status.put("cachedAt", cachedAt);
                        status.put("cachedAtStr", timeStr);
                    }
                } catch (Exception e) {
                    // 解析失败，使用原始字符串
                    status.put("cachedAtStr", cacheTimeStr);
                }
            }
        }
        
        return status;
    }

    /**
     * 在内存中根据条件过滤问题点
     * @param allProblems 所有问题点数据
     * @param filters 过滤条件
     * @return 过滤后的问题点列表
     */
    public List<TestIssues> filterProblemsInMemory(List<TestIssues> allProblems, Map<String, Object> filters) {
        if (allProblems == null || allProblems.isEmpty()) {
            return new ArrayList<>();
        }

        return allProblems.stream()
            .filter(problem -> matchesFilters(problem, filters))
            .collect(Collectors.toList());
    }

    /**
     * 检查问题点是否匹配过滤条件
     */
    private boolean matchesFilters(TestIssues problem, Map<String, Object> filters) {
        // 完整编码
        if (!matchesFilter(problem.getFull_model(), filters.get("fullModel"))) {
            return false;
        }
        
        // 电气编号
        if (!matchesFilter(problem.getElectric_sample_id(), filters.get("electricSampleId"))) {
            return false;
        }
        
        // 样品阶段（不区分大小写）
        if (filters.get("sampleStage") != null && !filters.get("sampleStage").toString().isEmpty()) {
            String sampleStage = problem.getSample_stage();
            String filterValue = filters.get("sampleStage").toString();
            if (sampleStage == null || !sampleStage.toLowerCase().contains(filterValue.toLowerCase())) {
                return false;
            }
        }
        
        // 版本
        if (!matchesFilter(problem.getVersion(), filters.get("version"))) {
            return false;
        }
        
        // 大类
        if (!matchesFilter(problem.getBig_species(), filters.get("bigSpecies"))) {
            return false;
        }
        
        // 小类
        if (!matchesFilter(problem.getSmall_species(), filters.get("smallSpecies"))) {
            return false;
        }
        
        // 问题类别（精确前缀匹配）
        if (filters.get("problemCategory") != null && !filters.get("problemCategory").toString().isEmpty()) {
            String problemCategory = problem.getProblemCategory();
            String filterValue = filters.get("problemCategory").toString();
            
            if (problemCategory == null) {
                return false;
            }
            
            // 标准化分隔符（全角"－"转半角"-"）
            String normalizedCategory = problemCategory.replace('－', '-');
            String normalizedFilter = filterValue.replace('－', '-');
            
            // 精确前缀匹配
            String categoryLower = normalizedCategory.toLowerCase();
            String filterLower = normalizedFilter.toLowerCase();
            
            if (!categoryLower.equals(filterLower) && !categoryLower.startsWith(filterLower + "-")) {
                return false;
            }
        }
        
        // 缺陷等级（精确匹配）
        if (filters.get("defectLevel") != null && !filters.get("defectLevel").toString().isEmpty()) {
            String defectLevel = problem.getDefect_level();
            String filterValue = filters.get("defectLevel").toString();
            if (!filterValue.equals(defectLevel)) {
                return false;
            }
        }
        
        // 当前状态（支持兼容性）
        if (filters.get("currentStatus") != null && !filters.get("currentStatus").toString().isEmpty()) {
            String currentStatus = problem.getCurrent_status();
            String filterValue = filters.get("currentStatus").toString();
            
            if (currentStatus == null) {
                return false;
            }
            
            // 标准化状态比较
            String normalizedStatus = normalizeStatus(currentStatus);
            String normalizedFilter = normalizeStatus(filterValue);
            
            if (!normalizedStatus.equals(normalizedFilter)) {
                return false;
            }
        }
        
        // 测试人员
        if (!matchesFilter(problem.getTester(), filters.get("tester"))) {
            return false;
        }
        
        // 责任部门
        if (!matchesFilter(problem.getResponsibleDepartment(), filters.get("responsibleDepartment"))) {
            return false;
        }
        
        // 测试平台
        if (!matchesFilter(problem.getTest_platform(), filters.get("testPlatform"))) {
            return false;
        }
        
        // 显示设备
        if (!matchesFilter(problem.getTest_device(), filters.get("testDevice"))) {
            return false;
        }
        
        // 其他设备
        if (!matchesFilter(problem.getOther_device(), filters.get("otherDevice"))) {
            return false;
        }
        
        // DQE负责人
        if (!matchesFilter(problem.getDqe(), filters.get("dqe"))) {
            return false;
        }
        
        // 问题描述
        if (!matchesFilter(problem.getProblem(), filters.get("problem"))) {
            return false;
        }
        
        // 日期范围过滤
        if (filters.get("startDate") != null && !filters.get("startDate").toString().isEmpty()) {
            java.time.LocalDateTime createdAt = problem.getCreated_at();
            if (createdAt == null) {
                return false;
            }
            
            // 将字符串日期转换为LocalDateTime进行比较
            try {
                String startDateStr = filters.get("startDate").toString();
                java.time.LocalDate startDate = java.time.LocalDate.parse(startDateStr);
                java.time.LocalDateTime startDateTime = startDate.atStartOfDay();
                
                if (createdAt.isBefore(startDateTime)) {
                    return false;
                }
            } catch (Exception e) {
                // 日期格式错误，忽略此过滤条件
                System.err.println("日期格式错误: " + filters.get("startDate"));
            }
        }
        
        if (filters.get("endDate") != null && !filters.get("endDate").toString().isEmpty()) {
            java.time.LocalDateTime createdAt = problem.getCreated_at();
            if (createdAt == null) {
                return false;
            }
            
            // 将字符串日期转换为LocalDateTime进行比较
            try {
                String endDateStr = filters.get("endDate").toString();
                java.time.LocalDate endDate = java.time.LocalDate.parse(endDateStr);
                java.time.LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
                
                if (createdAt.isAfter(endDateTime)) {
                    return false;
                }
            } catch (Exception e) {
                // 日期格式错误，忽略此过滤条件
                System.err.println("日期格式错误: " + filters.get("endDate"));
            }
        }
        
        return true;
    }

    /**
     * 通用的字符串匹配方法（模糊匹配）
     */
    private boolean matchesFilter(String value, Object filterValue) {
        if (filterValue == null || filterValue.toString().isEmpty()) {
            return true;
        }
        
        if (value == null) {
            return false;
        }
        
        return value.toLowerCase().contains(filterValue.toString().toLowerCase());
    }

    /**
     * 标准化状态（处理兼容性）
     */
    private String normalizeStatus(String status) {
        if (status == null) return "";
        
        String normalized = status.toLowerCase().trim();
        switch (normalized) {
            case "open":
                return "Open";
            case "closed":
            case "close":
                return "Closed";
            case "follow up":
            case "followup":
                return "Follow up";
            default:
                return status;
        }
    }

    /**
     * 手动刷新缓存（强制从数据库重新加载）
     * @param problems 从数据库获取的最新数据
     */
    public void refreshCache(List<TestIssues> problems) {
        clearCache();
        cacheAllProblems(problems);
        System.out.println("🔄 缓存已手动刷新");
    }
}

