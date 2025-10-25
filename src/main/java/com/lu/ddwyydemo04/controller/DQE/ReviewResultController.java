package com.lu.ddwyydemo04.controller.DQE;

import com.lu.ddwyydemo04.Service.ReviewResultsService;
import com.lu.ddwyydemo04.pojo.ReviewResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 评审结果页面控制器
 * 处理评审结果相关的页面跳转和API请求
 */
@Controller
public class ReviewResultController {

    @Value("${file.storage.templatespath}")
    private String templatesPath;

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
            @RequestParam(required = false) String testStartDate,
            @RequestParam(required = false) String testEndDate) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 调用Service层获取真实数据
            List<ReviewResults> allData = reviewResultsService.getAllReviewResults();
//            System.out.println("从数据库获取的总数据量: " + allData.size());
            
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
                .filter(item -> testStartDate == null || testStartDate.isEmpty() || 
                    (item.getTestDate() != null && !item.getTestDate().isBefore(LocalDate.parse(testStartDate))))
                .filter(item -> testEndDate == null || testEndDate.isEmpty() || 
                    (item.getTestDate() != null && !item.getTestDate().isAfter(LocalDate.parse(testEndDate))))
                .collect(Collectors.toList());

//            System.out.println("筛选后的数据量: " + filteredData.size());
//            System.out.println("筛选条件: majorCode=" + majorCode + ", minorCode=" + minorCode + ", projectPhase=" + projectPhase);
//            System.out.println("其他筛选条件: problemProcess=" + problemProcess + ", problemLevel=" + problemLevel + ", developmentMethod=" + developmentMethod);
//            System.out.println("更多筛选条件: solutionProvider=" + solutionProvider + ", isPreventable=" + isPreventable + ", problemStatus=" + problemStatus);
//            System.out.println("剩余筛选条件: responsibleDepartment=" + responsibleDepartment + ", problemPoint=" + problemPoint + ", problemTag1=" + problemTag1);
//            System.out.println("新增筛选条件: problemTag2=" + problemTag2 + ", problemReason=" + problemReason + ", improvementMeasures=" + improvementMeasures);
//            System.out.println("更多筛选条件: delayDays=" + delayDays + ", preventionNotes=" + preventionNotes);
//            System.out.println("测试日期筛选条件: testStartDate=" + testStartDate + ", testEndDate=" + testEndDate);

            // 分页处理
            int total = filteredData.size();
            int totalPages = (int) Math.ceil((double) total / size);
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, total);

//            System.out.println("分页信息: total=" + total + ", page=" + page + ", size=" + size + ", startIndex=" + startIndex + ", endIndex=" + endIndex);

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
     * 更新评审结果详情
     * @param requestData 评审结果详情数据
     * @return 更新结果
     */
    @PostMapping("/reviewResult/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateReviewDetail(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取请求参数
            Long id = Long.valueOf(requestData.get("id").toString());
            
            // 参数验证
            if (id == null) {
                result.put("success", false);
                result.put("message", "评审结果ID不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            // 先查询现有记录
            ReviewResults existingReview = reviewResultsService.getReviewResultById(id);
            if (existingReview == null) {
                result.put("success", false);
                result.put("message", "未找到指定的评审结果记录");
                return ResponseEntity.badRequest().body(result);
            }

            // 更新字段
            updateReviewFields(existingReview, requestData);

            // 调用Service层更新数据库
            boolean updateSuccess = reviewResultsService.updateReviewResult(existingReview);
            
            if (updateSuccess) {
                result.put("success", true);
                result.put("message", "评审结果详情更新成功");
                result.put("data", existingReview);
            } else {
                result.put("success", false);
                result.put("message", "数据库更新失败");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新评审结果详情失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 更新评审结果字段
     * @param review 评审结果对象
     * @param requestData 请求数据
     */
    private void updateReviewFields(ReviewResults review, Map<String, Object> requestData) {
        // 更新基本信息
        if (requestData.get("testDate") != null) {
            String testDateStr = requestData.get("testDate").toString();
            if (!testDateStr.isEmpty()) {
                review.setTestDate(LocalDate.parse(testDateStr));
            }
        }
        if (requestData.get("majorCode") != null) {
            review.setMajorCode(requestData.get("majorCode").toString());
        }
        if (requestData.get("minorCode") != null) {
            review.setMinorCode(requestData.get("minorCode").toString());
        }
        if (requestData.get("projectPhase") != null) {
            review.setProjectPhase(requestData.get("projectPhase").toString());
        }
        if (requestData.get("version") != null) {
            review.setVersion(requestData.get("version").toString());
        }
        if (requestData.get("problemProcess") != null) {
            review.setProblemProcess(requestData.get("problemProcess").toString());
        }
        if (requestData.get("problemLevel") != null) {
            review.setProblemLevel(requestData.get("problemLevel").toString());
        }
        if (requestData.get("developmentMethod") != null) {
            review.setDevelopmentMethod(requestData.get("developmentMethod").toString());
        }
        if (requestData.get("supplier") != null) {
            review.setSupplier(requestData.get("supplier").toString());
        }
        if (requestData.get("solutionProvider") != null) {
            review.setSolutionProvider(requestData.get("solutionProvider").toString());
        }

        // 更新问题信息
        if (requestData.get("problemPoint") != null) {
            review.setProblemPoint(requestData.get("problemPoint").toString());
        }
        if (requestData.get("problemReason") != null) {
            review.setProblemReason(requestData.get("problemReason").toString());
        }
        if (requestData.get("improvementMeasures") != null) {
            review.setImprovementMeasures(requestData.get("improvementMeasures").toString());
        }
        if (requestData.get("isPreventable") != null) {
            review.setIsPreventable(requestData.get("isPreventable").toString());
        }
        if (requestData.get("responsibleDepartment") != null) {
            review.setResponsibleDepartment(requestData.get("responsibleDepartment").toString());
        }

        // 更新时间信息
        if (requestData.get("plannedCompletionTime") != null) {
            String plannedTimeStr = requestData.get("plannedCompletionTime").toString();
            if (!plannedTimeStr.isEmpty()) {
                review.setPlannedCompletionTime(LocalDateTime.parse(plannedTimeStr + "T00:00:00"));
            }
        }
        if (requestData.get("actualCompletionTime") != null) {
            String actualTimeStr = requestData.get("actualCompletionTime").toString();
            if (!actualTimeStr.isEmpty()) {
                review.setActualCompletionTime(LocalDateTime.parse(actualTimeStr + "T00:00:00"));
            }
        }
        if (requestData.get("delayDays") != null) {
            String delayDaysStr = requestData.get("delayDays").toString();
            if (!delayDaysStr.isEmpty()) {
                review.setDelayDays(Integer.valueOf(delayDaysStr));
            }
        }
        if (requestData.get("problemStatus") != null) {
            review.setProblemStatus(requestData.get("problemStatus").toString());
        }

        // 更新其他信息
        if (requestData.get("problemTag1") != null) {
            review.setProblemTag1(requestData.get("problemTag1").toString());
        }
        if (requestData.get("problemTag2") != null) {
            review.setProblemTag2(requestData.get("problemTag2").toString());
        }
        if (requestData.get("preventionNotes") != null) {
            review.setPreventionNotes(requestData.get("preventionNotes").toString());
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
     * 批量删除选中的评审结果
     * @param requestData 包含要删除的ID列表
     * @return 删除结果
     */
    @PostMapping("/reviewResult/deleteSelected")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteSelectedReviewResults(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取要删除的ID列表
            @SuppressWarnings("unchecked")
            List<Long> ids = (List<Long>) requestData.get("ids");
            
            // 参数验证
            if (ids == null || ids.isEmpty()) {
                result.put("success", false);
                result.put("message", "请选择要删除的数据");
                return ResponseEntity.badRequest().body(result);
            }

            // 调用Service层批量删除
            int deletedCount = reviewResultsService.deleteReviewResultsByIds(ids);
            
            result.put("success", true);
            result.put("message", "批量删除成功");
            result.put("deletedCount", deletedCount);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "批量删除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 新增评审结果
     * @param requestData 评审结果数据
     * @return 新增结果
     */
    @PostMapping("/reviewResult/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addReviewResult(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 创建新的评审结果对象
            ReviewResults newReview = new ReviewResults();
            
            // 设置创建时间
            newReview.setCreatedTime(LocalDateTime.now());
            newReview.setUpdatedTime(LocalDateTime.now());
            
            // 更新字段
            updateReviewFields(newReview, requestData);

            // 调用Service层插入数据
            int insertResult = reviewResultsService.insertReviewResult(newReview);
            
            if (insertResult > 0) {
                result.put("success", true);
                result.put("message", "评审结果新增成功");
                result.put("data", newReview);
            } else {
                result.put("success", false);
                result.put("message", "数据库插入失败");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "新增评审结果失败: " + e.getMessage());
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
     * 导出评审结果Excel文件
     * @param requestData 包含导出数据的请求体
     * @return Excel文件
     */
    @PostMapping("/reviewResult/exportReviewResultsExcel")
    public ResponseEntity<byte[]> exportReviewResultsExcel(@RequestBody Map<String, Object> requestData) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) requestData.get("data");
            String fileName = (String) requestData.get("fileName");
            
            if (data == null || data.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // 调试：打印前几条数据的日期字段格式
            if (!data.isEmpty()) {
                System.out.println("=== 导出数据调试信息 ===");
                Map<String, Object> firstItem = data.get(0);
                System.out.println("预计完成时间字段类型: " + firstItem.get("plannedCompletionTime").getClass().getSimpleName());
                System.out.println("预计完成时间字段值: " + firstItem.get("plannedCompletionTime"));
                System.out.println("实际完成时间字段类型: " + firstItem.get("actualCompletionTime").getClass().getSimpleName());
                System.out.println("实际完成时间字段值: " + firstItem.get("actualCompletionTime"));
                System.out.println("========================");
            }

            // 创建Excel文件
            ByteArrayOutputStream outputStream = createReviewResultsExcelFile(data);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName != null ? fileName : "评审结果数据_" + java.time.LocalDate.now() + ".xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
            String templatePath = templatesPath + "/评审结果导入模板.xlsx";
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
            String templatePath = "Z:/ddwyy-lj/templatesDirectory/评审结果导入模板.xlsx";
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
     * 问题状态匹配方法，支持模糊搜索和大小写不敏感
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
        
        // 如果数据库状态为空，不匹配任何筛选条件
        if (dbStatusLower.isEmpty()) {
            return false;
        }
        
        // 如果筛选条件为空，不匹配任何数据库状态
        if (filterStatusLower.isEmpty()) {
            return false;
        }
        
        // 模糊搜索：检查数据库状态是否包含筛选条件
        // 例如：筛选条件"follow"可以匹配"followingUp"、"Follow"、"FOLLOW"等
        if (dbStatusLower.contains(filterStatusLower)) {
            return true;
        }
        
        // 反向模糊搜索：检查筛选条件是否包含数据库状态
        // 例如：筛选条件"followingUp"可以匹配"follow"
        if (filterStatusLower.contains(dbStatusLower)) {
            return true;
        }
        
        // 直接匹配（保持原有精确匹配功能）
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

    /**
     * 创建评审结果Excel文件
     * @param data 评审结果数据
     * @return Excel文件输出流
     * @throws IOException IO异常
     */
    private ByteArrayOutputStream createReviewResultsExcelFile(List<Map<String, Object>> data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Workbook workbook = new XSSFWorkbook();
        try {
            Sheet sheet = workbook.createSheet("问题汇总清单");

            // ====== 样式 ======
            CellStyle centeredStyle = workbook.createCellStyle();
            centeredStyle.setAlignment(HorizontalAlignment.CENTER);
            centeredStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            centeredStyle.setBorderTop(BorderStyle.THIN);
            centeredStyle.setBorderBottom(BorderStyle.THIN);
            centeredStyle.setBorderLeft(BorderStyle.THIN);
            centeredStyle.setBorderRight(BorderStyle.THIN);

            // 列宽
            for (int i = 0; i < 23; i++) sheet.setColumnWidth(i, 20 * 256);

            // ====== 表头 ======
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(50);
            String[] headers = {
                    "序号", "测试日期", "大编码", "小编码", "项目阶段", "版本", "问题工序", "问题等级", 
                    "开发方式", "供应商", "方案商", "问题点", "问题原因", "改善对策", "是否可预防", 
                    "责任部门", "预计完成时间", "实际完成时间", "Delay天数", "问题状态", "问题打标1", 
                    "问题打标2", "预防备注"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(centeredStyle);
            }

            // ====== 数据行 ======
            int rowNum = 1;
            for (int i = 0; i < data.size(); i++) {
                Map<String, Object> item = data.get(i);
                Row row = sheet.createRow(rowNum);
                row.setHeightInPoints(50);

                // 序号（从1开始）
                createReviewResultsCell(row, 0, i + 1, centeredStyle);
                createReviewResultsCell(row, 1, item.get("testDate"), centeredStyle);
                createReviewResultsCell(row, 2, item.get("majorCode"), centeredStyle);
                createReviewResultsCell(row, 3, item.get("minorCode"), centeredStyle);
                createReviewResultsCell(row, 4, item.get("projectPhase"), centeredStyle);
                createReviewResultsCell(row, 5, item.get("version"), centeredStyle);
                createReviewResultsCell(row, 6, item.get("problemProcess"), centeredStyle);
                createReviewResultsCell(row, 7, item.get("problemLevel"), centeredStyle);
                createReviewResultsCell(row, 8, item.get("developmentMethod"), centeredStyle);
                createReviewResultsCell(row, 9, item.get("supplier"), centeredStyle);
                createReviewResultsCell(row, 10, item.get("solutionProvider"), centeredStyle);
                createReviewResultsCell(row, 11, item.get("problemPoint"), centeredStyle);
                createReviewResultsCell(row, 12, item.get("problemReason"), centeredStyle);
                createReviewResultsCell(row, 13, item.get("improvementMeasures"), centeredStyle);
                createReviewResultsCell(row, 14, item.get("isPreventable"), centeredStyle);
                createReviewResultsCell(row, 15, item.get("responsibleDepartment"), centeredStyle);
                createReviewResultsCellWithDateFormat(row, 16, item.get("plannedCompletionTime"), centeredStyle);
                createReviewResultsCellWithDateFormat(row, 17, item.get("actualCompletionTime"), centeredStyle);
                createReviewResultsCell(row, 18, item.get("delayDays"), centeredStyle);
                createReviewResultsCell(row, 19, item.get("problemStatus"), centeredStyle);
                createReviewResultsCell(row, 20, item.get("problemTag1"), centeredStyle);
                createReviewResultsCell(row, 21, item.get("problemTag2"), centeredStyle);
                createReviewResultsCell(row, 22, item.get("preventionNotes"), centeredStyle);

                rowNum++;
            }

            // 写入内存输出
            workbook.write(out);
            return out;

        } finally {
            try { workbook.close(); } catch (IOException ignored) {}
        }
    }

    // 辅助方法用于创建评审结果单元格并设置样式
    private void createReviewResultsCell(Row row, int columnIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof java.time.LocalDateTime) {
            cell.setCellValue(((java.time.LocalDateTime) value).toString());
        } else if (value instanceof java.time.LocalDate) {
            cell.setCellValue(((java.time.LocalDate) value).toString());
        } else {
            cell.setCellValue(value != null ? value.toString() : "");
        }
        cell.setCellStyle(style);
    }

    // 辅助方法用于创建日期格式的单元格
    private void createReviewResultsCellWithDateFormat(Row row, int columnIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        if (value instanceof java.time.LocalDateTime) {
            java.time.LocalDateTime dateTime = (java.time.LocalDateTime) value;
            // 格式化为 2025-09-15 格式
            String formattedDate = String.format("%d-%02d-%02d", 
                dateTime.getYear(), 
                dateTime.getMonthValue(), 
                dateTime.getDayOfMonth());
            cell.setCellValue(formattedDate);
        } else if (value instanceof java.time.LocalDate) {
            java.time.LocalDate date = (java.time.LocalDate) value;
            // 格式化为 2025-09-15 格式
            String formattedDate = String.format("%d-%02d-%02d", 
                date.getYear(), 
                date.getMonthValue(), 
                date.getDayOfMonth());
            cell.setCellValue(formattedDate);
        } else if (value instanceof String) {
            String dateStr = (String) value;
            // 处理字符串格式的日期，如 "2025-09-12T00:00:00"
            if (dateStr.contains("T")) {
                try {
                    java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(dateStr);
                    String formattedDate = String.format("%d-%02d-%02d", 
                        dateTime.getYear(), 
                        dateTime.getMonthValue(), 
                        dateTime.getDayOfMonth());
                    cell.setCellValue(formattedDate);
                } catch (Exception e) {
                    // 如果解析失败，直接使用原字符串
                    cell.setCellValue(dateStr);
                }
            } else if (dateStr.contains("-")) {
                try {
                    java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
                    String formattedDate = String.format("%d-%02d-%02d", 
                        date.getYear(), 
                        date.getMonthValue(), 
                        date.getDayOfMonth());
                    cell.setCellValue(formattedDate);
                } catch (Exception e) {
                    // 如果解析失败，直接使用原字符串
                    cell.setCellValue(dateStr);
                }
            } else {
                cell.setCellValue(dateStr);
            }
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else {
            cell.setCellValue(value != null ? value.toString() : "");
        }
        cell.setCellStyle(style);
    }

}
