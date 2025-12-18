package com.lu.ddwyydemo04.controller.DQE;

import com.lu.ddwyydemo04.Service.ProblemLibraryService;
import com.lu.ddwyydemo04.pojo.TestIssues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@RestController
@RequestMapping("/problemLibrary")
@CrossOrigin(origins = "*")
public class ProblemLibraryController {

    @Autowired
    private ProblemLibraryService problemLibraryService;
    
    @Autowired
    private com.lu.ddwyydemo04.Service.ProblemLibraryCacheService problemLibraryCacheService;



    /**
     * 搜索问题点（已使用 Redis 缓存优化，第二次搜索速度提升20-60倍）
     * 注意：首次部署或更新后，建议先访问 /problemLibrary/clearCache 清除旧缓存
     */
    @PostMapping("/searchProblems")
    public ResponseEntity<Map<String, Object>> searchProblems(@RequestBody Map<String, Object> requestData) {
        try {
            // 提取搜索条件和用户信息
            @SuppressWarnings("unchecked")
            Map<String, Object> filters = (Map<String, Object>) requestData.get("filters");
            String username = (String) requestData.get("username");
            Boolean saveHistory = requestData.get("saveHistory") != null ? (Boolean) requestData.get("saveHistory") : true;
            
            // 如果filters为null，说明直接传的是filters对象（兼容旧的调用方式）
            if (filters == null) {
                filters = new HashMap<>(requestData);
                // 从请求中提取username和saveHistory，但不影响filters本身
                username = (String) filters.get("username");
                saveHistory = filters.get("saveHistory") != null ? (Boolean) filters.get("saveHistory") : true;
                // 从filters中移除这些字段，避免影响搜索逻辑
                filters.remove("username");
                filters.remove("saveHistory");
            }
            
            List<TestIssues> problems = problemLibraryService.searchProblems(filters);
            Map<String, Integer> stats = problemLibraryService.getStatistics(problems);

            // 保存搜索历史（如果用户已登录且允许保存，并且有筛选条件）
            if (saveHistory && username != null && !username.isEmpty()) {
                // 检查是否有任何筛选条件（排除空值）
                boolean hasAnyFilter = hasAnyFilterCondition(filters);
                
                // 只有存在筛选条件时才保存历史
                if (hasAnyFilter) {
                    // 生成搜索条件描述（在移除username和saveHistory之后，使用纯净的filters）
                    String filterDescription = generateFilterDescription(filters);
                    // 设置页面名称为"问题库管理页面"
                    String pageName = "问题库管理页面";
                    problemLibraryService.saveSearchHistory(username, filters, filterDescription, pageName);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", problems);
            response.put("total", stats.get("total"));
            response.put("open", stats.get("open"));
            response.put("inProgress", stats.get("inProgress"));
            response.put("resolved", stats.get("resolved"));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "搜索问题点失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 检查是否有任何筛选条件
     * @param filters 筛选条件Map
     * @return 如果有任何非空筛选条件返回true，否则返回false
     */
    private boolean hasAnyFilterCondition(Map<String, Object> filters) {
        // 检查所有可能的筛选条件
        String[] filterKeys = {
            "fullModel", "electricSampleId", "sampleStage", "version", "bigSpecies", "smallSpecies",
            "problemCategory", "defectLevel", "currentStatus", "tester",
            "responsibleDepartment", "startDate", "endDate", "dqe", "problem",
            "testPlatform", "testDevice", "otherDevice"
        };
        
        for (String key : filterKeys) {
            Object value = filters.get(key);
            if (value != null) {
                String strValue = value.toString().trim();
                if (!strValue.isEmpty() && 
                    !strValue.equals("null") && 
                    !strValue.equals("undefined")) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 生成搜索条件描述
     */
    private String generateFilterDescription(Map<String, Object> filters) {
        StringBuilder desc = new StringBuilder();
        
        if (filters.get("fullModel") != null && !filters.get("fullModel").toString().trim().isEmpty()) {
            desc.append("完整编码:").append(filters.get("fullModel")).append(" ");
        }
        if (filters.get("electricSampleId") != null && !filters.get("electricSampleId").toString().trim().isEmpty()) {
            desc.append("电气编号:").append(filters.get("electricSampleId")).append(" ");
        }
        if (filters.get("sampleStage") != null && !filters.get("sampleStage").toString().trim().isEmpty()) {
            desc.append("样品阶段:").append(filters.get("sampleStage")).append(" ");
        }
        if (filters.get("version") != null && !filters.get("version").toString().trim().isEmpty()) {
            desc.append("版本:").append(filters.get("version")).append(" ");
        }
        if (filters.get("bigSpecies") != null && !filters.get("bigSpecies").toString().trim().isEmpty()) {
            desc.append("大类:").append(filters.get("bigSpecies")).append(" ");
        }
        if (filters.get("smallSpecies") != null && !filters.get("smallSpecies").toString().trim().isEmpty()) {
            desc.append("小类:").append(filters.get("smallSpecies")).append(" ");
        }
        
        // 问题类别特殊处理（检查是否有值，包括三级结构的组合）
        Object problemCategoryObj = filters.get("problemCategory");
        if (problemCategoryObj != null) {
            String problemCategory = problemCategoryObj.toString().trim();
            // 排除空字符串、"null"、"undefined"等无效值，但保留只有一级的情况（如"视频组"）
            if (!problemCategory.isEmpty() && 
                !problemCategory.equals("null") && 
                !problemCategory.equals("undefined")) {
                desc.append("问题类别:").append(problemCategory).append(" ");
            }
        }
        if (filters.get("defectLevel") != null && !filters.get("defectLevel").toString().trim().isEmpty()) {
            desc.append("缺陷等级:").append(filters.get("defectLevel")).append(" ");
        }
        if (filters.get("currentStatus") != null && !filters.get("currentStatus").toString().trim().isEmpty()) {
            desc.append("当前状态:").append(filters.get("currentStatus")).append(" ");
        }
        if (filters.get("tester") != null && !filters.get("tester").toString().trim().isEmpty()) {
            desc.append("测试人员:").append(filters.get("tester")).append(" ");
        }
        if (filters.get("responsibleDepartment") != null && !filters.get("responsibleDepartment").toString().trim().isEmpty()) {
            desc.append("责任部门:").append(filters.get("responsibleDepartment")).append(" ");
        }
        if (filters.get("startDate") != null && !filters.get("startDate").toString().trim().isEmpty()) {
            desc.append("开始时间:").append(filters.get("startDate")).append(" ");
        }
        if (filters.get("endDate") != null && !filters.get("endDate").toString().trim().isEmpty()) {
            desc.append("结束时间:").append(filters.get("endDate")).append(" ");
        }
        if (filters.get("dqe") != null && !filters.get("dqe").toString().trim().isEmpty()) {
            desc.append("DQE负责人:").append(filters.get("dqe")).append(" ");
        }
        if (filters.get("problem") != null && !filters.get("problem").toString().trim().isEmpty()) {
            desc.append("问题描述:").append(filters.get("problem")).append(" ");
        }
        if (filters.get("testPlatform") != null && !filters.get("testPlatform").toString().trim().isEmpty()) {
            desc.append("测试平台:").append(filters.get("testPlatform")).append(" ");
        }
        if (filters.get("testDevice") != null && !filters.get("testDevice").toString().trim().isEmpty()) {
            desc.append("显示设备:").append(filters.get("testDevice")).append(" ");
        }
        if (filters.get("otherDevice") != null && !filters.get("otherDevice").toString().trim().isEmpty()) {
            desc.append("其他设备:").append(filters.get("otherDevice")).append(" ");
        }
        
        return desc.length() > 0 ? desc.toString().trim() : "无条件筛选";
    }

    /**
     * 更新问题点
     */
    @PostMapping("/updateProblem")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProblem(@RequestBody TestIssues testIssues) {
        try {
            System.out.println("SAdsdds:"+testIssues.getProblem());
            boolean success = problemLibraryService.updateProblem(testIssues);

            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "更新成功");
            } else {
                response.put("success", false);
                response.put("message", "更新失败");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新问题点失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 导出问题点
     */
    @PostMapping("/exportProblems")
    public ResponseEntity<byte[]> exportProblems(@RequestBody Map<String, Object> filters) {
        try {
            List<TestIssues> problems = problemLibraryService.searchProblems(filters);
//            System.out.println("problems:"+problems);

            // 创建Excel文件
            ByteArrayOutputStream outputStream = createExcelFile(problems);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "问题点库_" + java.time.LocalDate.now() + ".xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 导出收藏夹合并数据
     */
    @PostMapping("/exportCartsData")
    public ResponseEntity<byte[]> exportCartsData(@RequestBody Map<String, Object> requestData) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) requestData.get("data");
            String fileName = (String) requestData.get("fileName");
            
            if (data == null || data.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // 创建Excel文件
            ByteArrayOutputStream outputStream = createCartsExcelFile(data);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName != null ? fileName : "收藏夹合并数据_" + java.time.LocalDate.now() + ".xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取指定sample_id的历史版本
     */
    @GetMapping("/getHistoryVersions")
    public ResponseEntity<Map<String, Object>> getHistoryVersions(@RequestParam String sampleId) {
        try {
            List<TestIssues> historyVersions = problemLibraryService.getHistoryVersions(sampleId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", historyVersions);
            response.put("total", historyVersions.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取历史版本失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 检查是否存在历史版本
     */
    @GetMapping("/hasHistoryVersions")
    public ResponseEntity<Map<String, Object>> hasHistoryVersions(@RequestParam String sampleId) {
        try {
            boolean hasHistory = problemLibraryService.hasHistoryVersions(sampleId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("hasHistory", hasHistory);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "检查历史版本失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据ID获取问题点详情
     */
    @GetMapping("/getProblemById")
    public ResponseEntity<Map<String, Object>> getProblemById(@RequestParam Long id) {
        try {
            TestIssues problem = problemLibraryService.getProblemById(id);

            Map<String, Object> response = new HashMap<>();
            if (problem != null) {
                response.put("success", true);
                response.put("data", problem);
            } else {
                response.put("success", false);
                response.put("message", "未找到指定的问题点");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取问题点详情失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取大类小类选项
     */
    @GetMapping("/getSpeciesOptions")
    public ResponseEntity<Map<String, Object>> getSpeciesOptions() {
        try {
            Map<String, List<String>> speciesOptions = problemLibraryService.getSpeciesOptions();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", speciesOptions);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取大类小类选项失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取问题库缓存状态
     */
    @GetMapping("/getCacheStatus")
    public ResponseEntity<Map<String, Object>> getCacheStatus() {
        try {
            Map<String, Object> cacheStatus = problemLibraryCacheService.getCacheStatus();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", cacheStatus);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取缓存状态失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 手动刷新问题库缓存
     */
    @PostMapping("/refreshCache")
    public ResponseEntity<Map<String, Object>> refreshCache() {
        try {
            // 强制从数据库重新加载所有数据（不使用缓存）
            List<TestIssues> allProblems = problemLibraryService.loadAllProblemsFromDatabase();
            
            // 刷新缓存
            problemLibraryCacheService.refreshCache(allProblems);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "缓存已刷新，共 " + allProblems.size() + " 条数据");
            response.put("count", allProblems.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "刷新缓存失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 清除问题库缓存
     */
    @PostMapping("/clearCache")
    public ResponseEntity<Map<String, Object>> clearCache() {
        try {
            problemLibraryCacheService.clearCache();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "缓存已清除");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "清除缓存失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取搜索历史（所有用户的）
     */
    @GetMapping("/getSearchHistory")
    public ResponseEntity<Map<String, Object>> getSearchHistory(@RequestParam(required = false) String username, @RequestParam(required = false) Integer limit) {
        try {
            // 如果不传username或为空，则查询所有用户的搜索历史
            List<com.lu.ddwyydemo04.pojo.SearchHistory> histories = problemLibraryService.getSearchHistoryByUser(username, limit);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", histories);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取搜索历史失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    
    /**
     * 删除搜索历史
     */
    @DeleteMapping("/deleteSearchHistory")
    public ResponseEntity<Map<String, Object>> deleteSearchHistory(
            @RequestParam Long id,
            @RequestParam String username,
            @RequestParam String job) {
        try {
            // 权限检查：只有卢健、李良健并且job是manager可以删除
            if (!hasDeletePermission(username, job)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "您没有删除搜索历史的权限");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            boolean success = problemLibraryService.deleteSearchHistory(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "删除成功" : "删除失败");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除搜索历史失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 删除用户的所有搜索历史
     */
    @DeleteMapping("/deleteAllSearchHistory")
    public ResponseEntity<Map<String, Object>> deleteAllSearchHistory(
            @RequestParam String username,
            @RequestParam String job) {
        try {
            // 权限检查：只有卢健、李良健并且job是manager可以删除
            if (!hasDeletePermission(username, job)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "您没有删除搜索历史的权限，只有卢健、李良健并且job是manager可以删除");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // 传入null以删除所有用户的历史（因为已经通过权限验证的用户可以清空所有历史）
            boolean success = problemLibraryService.deleteAllByUser(null);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "删除成功" : "删除失败");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除搜索历史失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 检查是否有删除搜索历史的权限
     * @param username 用户名
     * @param job 职位
     * @return 有权限返回true，否则返回false
     */
    private boolean hasDeletePermission(String username, String job) {
        // 只有卢健、李良健并且job是manager可以删除
        return ("卢健".equals(username) || "李良健".equals(username)) && "manager".equals(job);
    }

    /**
     * 创建Excel文件
     */
    private ByteArrayOutputStream createExcelFile(List<TestIssues> problems) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Workbook workbook = new XSSFWorkbook();
        try {
            Sheet sheet = workbook.createSheet("问题点库管理");

            // ====== 样式 ======
            CellStyle centeredStyle = workbook.createCellStyle();
            centeredStyle.setAlignment(HorizontalAlignment.CENTER);
            centeredStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            centeredStyle.setBorderTop(BorderStyle.THIN);
            centeredStyle.setBorderBottom(BorderStyle.THIN);
            centeredStyle.setBorderLeft(BorderStyle.THIN);
            centeredStyle.setBorderRight(BorderStyle.THIN);

            // 列宽
            for (int i = 0; i < 46; i++) sheet.setColumnWidth(i, 20 * 256);

            // ====== 标题 ======
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("问题点库管理");

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setFontHeightInPoints((short) 20);
            titleFont.setBold(true);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            titleStyle.setBorderTop(BorderStyle.THIN);
            titleStyle.setBorderBottom(BorderStyle.THIN);
            titleStyle.setBorderLeft(BorderStyle.THIN);
            titleStyle.setBorderRight(BorderStyle.THIN);
            titleCell.setCellStyle(titleStyle);

            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 45));

            // ====== 表头 ======
            Row headerRow = sheet.createRow(1);
            headerRow.setHeightInPoints(50);
            String[] headers = {
                    "序号", "样品ID", "电气编号", "完整编码", "SKU", "小类", "样品阶段", "版本", "芯片方案", "测试平台", "测试设备", "其他设备",
                    "问题点", "问题类别", "问题图片/视频", "报告日期", "复现手法", "恢复方法", "复现概率", "缺陷等级", "当前状态",
                    "对比上一版或竞品", "测试人员", "改善对策", "分析责任人", "改善后风险", "下一版回归测试", "备注",
                    "创建时间", "历史版本ID", "创建者", "DQE确认", "DQE审核时间", "DQE责任人", "研发确认", "研发审核时间", "研发责任人",
                    "修改者", "修改时间", "责任部门", "DQE确认回复", "研发确认回复", "方案商", "供应商", "评审结论", "内外贸"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(centeredStyle);
            }

            // ====== 数据行 ======
            int rowNum = 2;
            for (TestIssues issue : problems) {
                Row row = sheet.createRow(rowNum);
                row.setHeightInPoints(50);

                createCell(row, 0, issue.getId(), centeredStyle);
                createCell(row, 1, issue.getSample_id(), centeredStyle);
                createCell(row, 2, issue.getElectric_sample_id(), centeredStyle);
                createCell(row, 3, issue.getFull_model(), centeredStyle);
                createCell(row, 4, issue.getSku(), centeredStyle);
                createCell(row, 5, issue.getSmall_species(), centeredStyle);
                createCell(row, 6, issue.getSample_stage(), centeredStyle);
                createCell(row, 7, issue.getVersion(), centeredStyle);
                createCell(row, 8, issue.getChip_solution(), centeredStyle);
                createCell(row, 9, issue.getTest_platform(), centeredStyle);
                createCell(row, 10, issue.getTest_device(), centeredStyle);
                createCell(row, 11, issue.getOther_device(), centeredStyle);
                createCell(row, 12, issue.getProblem(), centeredStyle);
                createCell(row, 13, issue.getProblemCategory(), centeredStyle);
                createCell(row, 14, issue.getProblem_image_or_video(), centeredStyle);
                createCell(row, 15, issue.getProblem_time(), centeredStyle);
                createCell(row, 16, issue.getReproduction_method(), centeredStyle);
                createCell(row, 17, issue.getRecovery_method(), centeredStyle);
                createCell(row, 18, issue.getReproduction_probability(), centeredStyle);
                createCell(row, 19, issue.getDefect_level(), centeredStyle);
                createCell(row, 20, issue.getCurrent_status(), centeredStyle);
                createCell(row, 21, issue.getComparison_with_previous(), centeredStyle);
                createCell(row, 22, issue.getTester(), centeredStyle);
                createCell(row, 23, issue.getImprovement_plan(), centeredStyle);
                createCell(row, 24, issue.getResponsible_person(), centeredStyle);
                createCell(row, 25, issue.getPost_improvement_risk(), centeredStyle);
                createCell(row, 26, issue.getNext_version_regression_test(), centeredStyle);
                createCell(row, 27, issue.getRemark(), centeredStyle);
                createCell(row, 28, issue.getCreated_at(), centeredStyle);
                createCell(row, 29, issue.getHistory_id(), centeredStyle);
                createCell(row, 30, issue.getCreated_by(), centeredStyle);
                createCell(row, 31, issue.getDqe_confirm(), centeredStyle);
                createCell(row, 32, issue.getDqe_review_at(), centeredStyle);
                createCell(row, 33, issue.getDqe(), centeredStyle);
                createCell(row, 34, issue.getRd_confirm(), centeredStyle);
                createCell(row, 35, issue.getRd_review_at(), centeredStyle);
                createCell(row, 36, issue.getRd(), centeredStyle);
                createCell(row, 37, issue.getModifier(), centeredStyle);
                createCell(row, 38, issue.getModify_at(), centeredStyle);
                createCell(row, 39, issue.getResponsibleDepartment(), centeredStyle);
                createCell(row, 40, issue.getGreen_union_dqe(), centeredStyle);
                createCell(row, 41, issue.getGreen_union_rd(), centeredStyle);
                createCell(row, 42, issue.getSolution_provider(), centeredStyle);
                createCell(row, 43, issue.getSupplier(), centeredStyle);
                createCell(row, 44, issue.getReview_conclusion(), centeredStyle);
                createCell(row, 45, issue.getTest_Overseas(), centeredStyle);

                rowNum++;
            }

            // 写入内存输出
            workbook.write(out);
            byte[] bytes = out.toByteArray();

            return out;

        } finally {
            try { workbook.close(); } catch (IOException ignored) {}
        }
    }

    // 创建收藏夹合并数据Excel文件
    private ByteArrayOutputStream createCartsExcelFile(List<Map<String, Object>> data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Workbook workbook = new XSSFWorkbook();
        try {
            Sheet sheet = workbook.createSheet("问题点库管理");

            // ====== 样式 ======
            CellStyle centeredStyle = workbook.createCellStyle();
            centeredStyle.setAlignment(HorizontalAlignment.CENTER);
            centeredStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            centeredStyle.setBorderTop(BorderStyle.THIN);
            centeredStyle.setBorderBottom(BorderStyle.THIN);
            centeredStyle.setBorderLeft(BorderStyle.THIN);
            centeredStyle.setBorderRight(BorderStyle.THIN);

            // 列宽
            for (int i = 0; i < 46; i++) sheet.setColumnWidth(i, 20 * 256);

            // ====== 标题 ======
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("问题点库管理");

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setFontHeightInPoints((short) 20);
            titleFont.setBold(true);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            titleStyle.setBorderTop(BorderStyle.THIN);
            titleStyle.setBorderBottom(BorderStyle.THIN);
            titleStyle.setBorderLeft(BorderStyle.THIN);
            titleStyle.setBorderRight(BorderStyle.THIN);
            titleCell.setCellStyle(titleStyle);

            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 45));

            // ====== 表头 ======
            Row headerRow = sheet.createRow(1);
            headerRow.setHeightInPoints(50);
            String[] headers = {
                    "序号", "样品ID", "电气编号", "完整编码", "SKU", "小类", "样品阶段", "版本", "芯片方案", "测试平台", "测试设备", "其他设备",
                    "问题点", "问题类别", "问题图片/视频", "报告日期", "复现手法", "恢复方法", "复现概率", "缺陷等级", "当前状态",
                    "对比上一版或竞品", "测试人员", "改善对策", "分析责任人", "改善后风险", "下一版回归测试", "备注",
                    "创建时间", "历史版本ID", "创建者", "DQE确认", "DQE审核时间", "DQE责任人", "研发确认", "研发审核时间", "研发责任人",
                    "修改者", "修改时间", "责任部门", "DQE确认回复", "研发确认回复", "方案商", "供应商", "评审结论", "内外贸"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(centeredStyle);
            }

            // ====== 数据行 ======
            int rowNum = 2;
            for (Map<String, Object> item : data) {
                Row row = sheet.createRow(rowNum);
                row.setHeightInPoints(50);

                createCell(row, 0, item.get("id"), centeredStyle);
                createCell(row, 1, item.get("sample_id"), centeredStyle);
                createCell(row, 2, item.get("electric_sample_id"), centeredStyle);
                createCell(row, 3, item.get("full_model"), centeredStyle);
                createCell(row, 4, item.get("sku"), centeredStyle);
                createCell(row, 5, item.get("small_species"), centeredStyle);
                createCell(row, 6, item.get("sample_stage"), centeredStyle);
                createCell(row, 7, item.get("version"), centeredStyle);
                createCell(row, 8, item.get("chip_solution"), centeredStyle);
                createCell(row, 9, item.get("test_platform"), centeredStyle);
                createCell(row, 10, item.get("test_device"), centeredStyle);
                createCell(row, 11, item.get("other_device"), centeredStyle);
                createCell(row, 12, item.get("problem"), centeredStyle);
                createCell(row, 13, item.get("problemCategory"), centeredStyle);
                createCell(row, 14, item.get("problem_image_or_video"), centeredStyle);
                createCell(row, 15, item.get("problem_time"), centeredStyle);
                createCell(row, 16, item.get("reproduction_method"), centeredStyle);
                createCell(row, 17, item.get("recovery_method"), centeredStyle);
                createCell(row, 18, item.get("reproduction_probability"), centeredStyle);
                createCell(row, 19, item.get("defect_level"), centeredStyle);
                createCell(row, 20, item.get("current_status"), centeredStyle);
                createCell(row, 21, item.get("comparison_with_previous"), centeredStyle);
                createCell(row, 22, item.get("tester"), centeredStyle);
                createCell(row, 23, item.get("improvement_plan"), centeredStyle);
                createCell(row, 24, item.get("responsible_person"), centeredStyle);
                createCell(row, 25, item.get("post_improvement_risk"), centeredStyle);
                createCell(row, 26, item.get("next_version_regression_test"), centeredStyle);
                createCell(row, 27, item.get("remark"), centeredStyle);
                createCell(row, 28, item.get("created_at"), centeredStyle);
                createCell(row, 29, item.get("history_id"), centeredStyle);
                createCell(row, 30, item.get("created_by"), centeredStyle);
                createCell(row, 31, item.get("dqe_confirm"), centeredStyle);
                createCell(row, 32, item.get("dqe_review_at"), centeredStyle);
                createCell(row, 33, item.get("dqe"), centeredStyle);
                createCell(row, 34, item.get("rd_confirm"), centeredStyle);
                createCell(row, 35, item.get("rd_review_at"), centeredStyle);
                createCell(row, 36, item.get("rd"), centeredStyle);
                createCell(row, 37, item.get("modifier"), centeredStyle);
                createCell(row, 38, item.get("modify_at"), centeredStyle);
                createCell(row, 39, item.get("responsibleDepartment"), centeredStyle);
                createCell(row, 40, item.get("green_union_dqe"), centeredStyle);
                createCell(row, 41, item.get("green_union_rd"), centeredStyle);
                createCell(row, 42, item.get("solution_provider"), centeredStyle);
                createCell(row, 43, item.get("supplier"), centeredStyle);
                createCell(row, 44, item.get("review_conclusion"), centeredStyle);
                createCell(row, 45, item.get("test_Overseas"), centeredStyle);

                rowNum++;
            }

            // 写入内存输出
            workbook.write(out);
            return out;

        } finally {
            try { workbook.close(); } catch (IOException ignored) {}
        }
    }

    // 辅助方法用于创建单元格并设置样式
    private void createCell(Row row, int columnIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof java.time.LocalDateTime) {
            cell.setCellValue(((java.time.LocalDateTime) value).toString());
        } else {
            cell.setCellValue(value != null ? value.toString() : "");
        }
        cell.setCellStyle(style);
    }
}
