package com.lu.ddwyydemo04.controller.DQE;

import com.lu.ddwyydemo04.Service.ReviewResultsService;
import com.lu.ddwyydemo04.pojo.ReviewResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 评审结果页面控制器
 * 处理评审结果相关的页面跳转和API请求
 */
@Controller
public class ReviewResultController {

    @Autowired
    private ReviewResultsService reviewResultsService;

    /**
     * 评审结果页面跳转
     * @return 评审结果页面视图
     */
    @GetMapping("/reviewResults")
    public String reviewResultPage() {
        return "DQE/reviewResults";
    }

    /**
     * 获取评审结果列表
     * @param username 用户名
     * @param job 角色
     * @param page 页码（可选，默认为1）
     * @param size 每页大小（可选，默认为20）
     * @param sampleId 样品ID筛选（可选）
     * @param status 评审状态筛选（可选）
     * @param reviewer 评审人筛选（可选）
     * @param reviewDate 评审日期筛选（可选）
     * @return 评审结果列表
     */
    @GetMapping("/reviewResult/getReviewResults")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getReviewResults(
            @RequestParam String username,
            @RequestParam String job,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String majorCode,
            @RequestParam(required = false) String minorCode,
            @RequestParam(required = false) String projectPhase,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false) String problemProcess,
            @RequestParam(required = false) String problemLevel,
            @RequestParam(required = false) String developmentMethod,
            @RequestParam(required = false) String solutionProvider,
            @RequestParam(required = false) String isPreventable,
            @RequestParam(required = false) String problemStatus,
            @RequestParam(required = false) String responsibleDepartment,
            @RequestParam(required = false) String problemPoint,
            @RequestParam(required = false) String problemTag1,
            @RequestParam(required = false) String problemTag2,
            @RequestParam(required = false) String problemReason,
            @RequestParam(required = false) String improvementMeasures,
            @RequestParam(required = false) String delayDays,
            @RequestParam(required = false) String preventionNotes,
            @RequestParam(required = false) String plannedStartDate,
            @RequestParam(required = false) String plannedEndDate,
            @RequestParam(required = false) String actualStartDate,
            @RequestParam(required = false) String actualEndDate) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 调用Service层获取真实数据
            List<ReviewResults> allData = reviewResultsService.getAllReviewResults();
            System.out.println("从数据库获取的总数据量: " + allData.size());
            
            // 应用筛选条件
            List<ReviewResults> filteredData = allData.stream()
                .filter(item -> majorCode == null || majorCode.isEmpty() || 
                    (item.getMajorCode() != null && item.getMajorCode().toLowerCase().contains(majorCode.toLowerCase())))
                .filter(item -> minorCode == null || minorCode.isEmpty() || 
                    (item.getMinorCode() != null && item.getMinorCode().toLowerCase().contains(minorCode.toLowerCase())))
                .filter(item -> projectPhase == null || projectPhase.isEmpty() || 
                    (item.getProjectPhase() != null && item.getProjectPhase().toLowerCase().contains(projectPhase.toLowerCase())))
                .filter(item -> version == null || version.isEmpty() || 
                    (item.getVersion() != null && item.getVersion().toLowerCase().contains(version.toLowerCase())))
                .filter(item -> supplier == null || supplier.isEmpty() || 
                    (item.getSupplier() != null && item.getSupplier().toLowerCase().contains(supplier.toLowerCase())))
                .filter(item -> problemProcess == null || problemProcess.isEmpty() || 
                    (item.getProblemProcess() != null && item.getProblemProcess().toLowerCase().contains(problemProcess.toLowerCase())))
                .filter(item -> problemLevel == null || problemLevel.isEmpty() || 
                    (item.getProblemLevel() != null && item.getProblemLevel().equals(problemLevel)))
                .filter(item -> developmentMethod == null || developmentMethod.isEmpty() || 
                    (item.getDevelopmentMethod() != null && item.getDevelopmentMethod().toLowerCase().contains(developmentMethod.toLowerCase())))
                .filter(item -> solutionProvider == null || solutionProvider.isEmpty() || 
                    (item.getSolutionProvider() != null && item.getSolutionProvider().toLowerCase().contains(solutionProvider.toLowerCase())))
                .filter(item -> isPreventable == null || isPreventable.isEmpty() || 
                    (item.getIsPreventable() != null && item.getIsPreventable().equals(isPreventable)))
                .filter(item -> problemStatus == null || problemStatus.isEmpty() || 
                    (item.getProblemStatus() != null && isProblemStatusMatch(item.getProblemStatus(), problemStatus)))
                .filter(item -> responsibleDepartment == null || responsibleDepartment.isEmpty() || 
                    (item.getResponsibleDepartment() != null && item.getResponsibleDepartment().toLowerCase().contains(responsibleDepartment.toLowerCase())))
                .filter(item -> problemPoint == null || problemPoint.isEmpty() || 
                    (item.getProblemPoint() != null && item.getProblemPoint().toLowerCase().contains(problemPoint.toLowerCase())))
                .filter(item -> problemTag1 == null || problemTag1.isEmpty() || 
                    (item.getProblemTag1() != null && item.getProblemTag1().toLowerCase().contains(problemTag1.toLowerCase())))
                .filter(item -> problemTag2 == null || problemTag2.isEmpty() || 
                    (item.getProblemTag2() != null && item.getProblemTag2().toLowerCase().contains(problemTag2.toLowerCase())))
                .filter(item -> problemReason == null || problemReason.isEmpty() || 
                    (item.getProblemReason() != null && item.getProblemReason().toLowerCase().contains(problemReason.toLowerCase())))
                .filter(item -> improvementMeasures == null || improvementMeasures.isEmpty() || 
                    (item.getImprovementMeasures() != null && item.getImprovementMeasures().toLowerCase().contains(improvementMeasures.toLowerCase())))
                .filter(item -> delayDays == null || delayDays.isEmpty() || 
                    (item.getDelayDays() != null && item.getDelayDays().toString().equals(delayDays)))
                .filter(item -> preventionNotes == null || preventionNotes.isEmpty() || 
                    (item.getPreventionNotes() != null && item.getPreventionNotes().toLowerCase().contains(preventionNotes.toLowerCase())))
                .filter(item -> plannedStartDate == null || plannedStartDate.isEmpty() || 
                    (item.getPlannedCompletionTime() != null && !item.getPlannedCompletionTime().toLocalDate().isBefore(LocalDate.parse(plannedStartDate))))
                .filter(item -> plannedEndDate == null || plannedEndDate.isEmpty() || 
                    (item.getPlannedCompletionTime() != null && !item.getPlannedCompletionTime().toLocalDate().isAfter(LocalDate.parse(plannedEndDate))))
                .filter(item -> actualStartDate == null || actualStartDate.isEmpty() || 
                    (item.getActualCompletionTime() != null && !item.getActualCompletionTime().toLocalDate().isBefore(LocalDate.parse(actualStartDate))))
                .filter(item -> actualEndDate == null || actualEndDate.isEmpty() || 
                    (item.getActualCompletionTime() != null && !item.getActualCompletionTime().toLocalDate().isAfter(LocalDate.parse(actualEndDate))))
                .collect(Collectors.toList());

            System.out.println("筛选后的数据量: " + filteredData.size());
            System.out.println("筛选条件: majorCode=" + majorCode + ", minorCode=" + minorCode + ", projectPhase=" + projectPhase);
            System.out.println("其他筛选条件: problemProcess=" + problemProcess + ", problemLevel=" + problemLevel + ", developmentMethod=" + developmentMethod);
            System.out.println("更多筛选条件: solutionProvider=" + solutionProvider + ", isPreventable=" + isPreventable + ", problemStatus=" + problemStatus);
            System.out.println("剩余筛选条件: responsibleDepartment=" + responsibleDepartment + ", problemPoint=" + problemPoint + ", problemTag1=" + problemTag1);
            System.out.println("新增筛选条件: problemTag2=" + problemTag2 + ", problemReason=" + problemReason + ", improvementMeasures=" + improvementMeasures);
            System.out.println("更多筛选条件: delayDays=" + delayDays + ", preventionNotes=" + preventionNotes);
            System.out.println("预计完成日期筛选条件: plannedStartDate=" + plannedStartDate + ", plannedEndDate=" + plannedEndDate);
            System.out.println("实际完成日期筛选条件: actualStartDate=" + actualStartDate + ", actualEndDate=" + actualEndDate);

            // 分页处理
            int total = filteredData.size();
            int totalPages = (int) Math.ceil((double) total / size);
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, total);

            System.out.println("分页信息: total=" + total + ", page=" + page + ", size=" + size + ", startIndex=" + startIndex + ", endIndex=" + endIndex);

            List<ReviewResults> pageData;
            if (total == 0) {
                pageData = new ArrayList<>();
            } else {
                pageData = filteredData.subList(startIndex, endIndex);
            }

            result.put("success", true);
            result.put("data", pageData);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", totalPages);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "获取评审结果失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 获取评审结果详情
     * @param id 评审结果ID
     * @param username 用户名
     * @return 评审结果详情
     */
    @GetMapping("/reviewResult/getReviewDetail")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getReviewDetail(
            @RequestParam Long id,
            @RequestParam String username) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 调用Service层获取真实数据
            ReviewResults reviewDetail = reviewResultsService.getReviewResultById(id);
            
            if (reviewDetail != null) {
                result.put("success", true);
                result.put("data", reviewDetail);
            } else {
                result.put("success", false);
                result.put("error", "未找到指定的评审结果");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "获取评审详情失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 保存评审结果
     * @param requestData 评审结果数据
     * @return 保存结果
     */
    @PostMapping("/reviewResult/saveReviewResult")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveReviewResult(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取请求参数
            String username = (String) requestData.get("username");
            String job = (String) requestData.get("job");
            Long id = requestData.get("id") != null ? Long.valueOf(requestData.get("id").toString()) : null;
            String sampleId = (String) requestData.get("sampleId");
            String status = (String) requestData.get("status");
            String reviewer = (String) requestData.get("reviewer");
            String reviewComment = (String) requestData.get("reviewComment");

            // TODO: 这里将来会调用Service层保存真实数据
            // 目前只做参数验证
            if (sampleId == null || sampleId.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "样品ID不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            if (status == null || status.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "评审状态不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            if (reviewer == null || reviewer.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "评审人不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            // 模拟保存成功
            result.put("success", true);
            result.put("message", "评审结果保存成功");
            result.put("id", id != null ? id : System.currentTimeMillis()); // 模拟生成ID

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "保存评审结果失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 更新评审结果
     * @param requestData 评审结果数据
     * @return 更新结果
     */
    @PostMapping("/reviewResult/updateReviewResult")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateReviewResult(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取请求参数
            Long id = Long.valueOf(requestData.get("id").toString());
            String username = (String) requestData.get("username");
            String status = (String) requestData.get("status");
            String reviewer = (String) requestData.get("reviewer");
            String reviewComment = (String) requestData.get("reviewComment");

            // TODO: 这里将来会调用Service层更新真实数据
            // 目前只做参数验证
            if (id == null) {
                result.put("success", false);
                result.put("message", "评审结果ID不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            // 模拟更新成功
            result.put("success", true);
            result.put("message", "评审结果更新成功");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新评审结果失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 删除评审结果
     * @param id 评审结果ID
     * @param username 用户名
     * @return 删除结果
     */
    @PostMapping("/reviewResult/deleteReviewResult")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteReviewResult(
            @RequestParam Long id,
            @RequestParam String username) {

        Map<String, Object> result = new HashMap<>();

        try {
            // TODO: 这里将来会调用Service层删除真实数据
            // 目前只做参数验证
            if (id == null) {
                result.put("success", false);
                result.put("message", "评审结果ID不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            // 模拟删除成功
            result.put("success", true);
            result.put("message", "评审结果删除成功");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除评审结果失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 导入评审结果数据
     * @param file Excel文件
     * @param username 用户名
     * @return 导入结果
     */
    @PostMapping("/reviewResult/importReviewResults")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> importReviewResults(
            @RequestParam("file") MultipartFile file,
            @RequestParam String username) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "请选择要导入的文件");
                return ResponseEntity.badRequest().body(result);
            }

            // 检查文件格式
            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
                result.put("success", false);
                result.put("message", "文件格式不正确，请上传.xlsx格式的Excel文件");
                return ResponseEntity.badRequest().body(result);
            }

            // 调用Service层处理导入
            ReviewResultsService.ImportResult importResult = reviewResultsService.importReviewResultsFromExcel(file);

            result.put("success", importResult.isSuccess());
            result.put("message", importResult.getMessage());
            result.put("insertCount", importResult.getInsertCount());
            result.put("updateCount", importResult.getUpdateCount());
            result.put("errorCount", importResult.getErrorCount());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "导入失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 导出评审结果
     * @param username 用户名
     * @param job 角色
     * @param sampleId 样品ID筛选（可选）
     * @param status 评审状态筛选（可选）
     * @param reviewer 评审人筛选（可选）
     * @param reviewDate 评审日期筛选（可选）
     * @return 导出结果
     */
    @GetMapping("/reviewResult/exportReviewResults")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> exportReviewResults(
            @RequestParam String username,
            @RequestParam String job,
            @RequestParam(required = false) String sampleId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String reviewer,
            @RequestParam(required = false) String reviewDate) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 调用Service层获取真实数据
            List<ReviewResults> allData = reviewResultsService.getAllReviewResults();
            
            result.put("success", true);
            result.put("message", "导出功能开发中，当前返回所有数据");
            result.put("data", allData);
            result.put("total", allData.size());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "导出评审结果失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 下载评审结果导入模板
     * @return 模板文件信息
     */
    @GetMapping("/reviewResult/downloadTemplate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> downloadTemplate() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 从application.yml中获取模板路径
            String templatePath = "E:/ddwyy-lj/templatesDirectory/评审结果导入模板.xlsx";
            Path file = Paths.get(templatePath);
            
            System.out.println("尝试下载模板文件，路径: " + templatePath);
            System.out.println("文件是否存在: " + Files.exists(file));
            
            if (!Files.exists(file)) {
                // 如果模板文件不存在，返回提示信息
                result.put("success", false);
                result.put("message", "模板文件不存在，请先创建模板文件");
                result.put("templatePath", templatePath);
                result.put("instructions", "请将评审结果导入模板.xlsx文件放到以下路径：" + templatePath);
                return ResponseEntity.ok(result);
            }
            
            // 如果文件存在，返回文件信息
            result.put("success", true);
            result.put("message", "模板文件存在，可以下载");
            result.put("templatePath", templatePath);
            result.put("fileSize", Files.size(file));
            result.put("downloadUrl", "/reviewResult/downloadTemplateFile");
            
            return ResponseEntity.ok(result);
                    
        } catch (Exception e) {
            System.err.println("检查模板文件时发生错误: " + e.getMessage());
            e.printStackTrace();
            
            result.put("success", false);
            result.put("message", "检查模板文件时发生错误: " + e.getMessage());
            result.put("templatePath", "E:/ddwyy-lj/templatesDirectory/评审结果导入模板.xlsx");
            return ResponseEntity.ok(result);
        }
    }
    
    /**
     * 实际下载模板文件
     * @return 模板文件
     */
    @GetMapping("/reviewResult/downloadTemplateFile")
    public ResponseEntity<Resource> downloadTemplateFile() {
        try {
            String templatePath = "E:/ddwyy-lj/templatesDirectory/评审结果导入模板.xlsx";
            Path file = Paths.get(templatePath);
            
            if (!Files.exists(file)) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new UrlResource(file.toUri());
            
            // 对中文文件名进行URL编码
            String filename = "评审结果导入模板.xlsx";
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString());
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 问题状态匹配方法，支持大小写不敏感和closed/close兼容
     * @param dbStatus 数据库中的状态
     * @param filterStatus 筛选条件中的状态
     * @return 是否匹配
     */
    private boolean isProblemStatusMatch(String dbStatus, String filterStatus) {
        if (dbStatus == null || filterStatus == null) {
            return false;
        }
        
        String dbStatusLower = dbStatus.toLowerCase().trim();
        String filterStatusLower = filterStatus.toLowerCase().trim();
        
        // 直接匹配
        if (dbStatusLower.equals(filterStatusLower)) {
            return true;
        }
        
        // 特殊处理：closed 和 close 的兼容
        if (filterStatusLower.equals("closed")) {
            return dbStatusLower.equals("closed") || dbStatusLower.equals("close");
        }
        if (filterStatusLower.equals("close")) {
            return dbStatusLower.equals("closed") || dbStatusLower.equals("close");
        }
        
        // 特殊处理：open 的大小写兼容
        if (filterStatusLower.equals("open")) {
            return dbStatusLower.equals("open");
        }
        
        // 特殊处理：follow up 的兼容
        if (filterStatusLower.equals("follow up")) {
            return dbStatusLower.equals("follow up") || dbStatusLower.equals("followup") || dbStatusLower.equals("follow-up");
        }
        
        return false;
    }

}
