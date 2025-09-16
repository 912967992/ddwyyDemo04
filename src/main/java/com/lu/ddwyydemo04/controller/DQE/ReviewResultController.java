package com.lu.ddwyydemo04.controller.DQE;

import com.lu.ddwyydemo04.Service.ReviewResultsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.*;

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
            @RequestParam(required = false) String sampleId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String reviewer,
            @RequestParam(required = false) String reviewDate) {

        Map<String, Object> result = new HashMap<>();

        try {
            // TODO: 这里将来会调用Service层获取真实数据
            // 目前返回模拟数据用于测试
            List<Map<String, Object>> mockData = generateMockReviewResults();

            // 应用筛选条件
            List<Map<String, Object>> filteredData = applyFilters(mockData, sampleId, status, reviewer, reviewDate);

            // 分页处理
            int total = filteredData.size();
            int totalPages = (int) Math.ceil((double) total / size);
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, total);

            List<Map<String, Object>> pageData = filteredData.subList(startIndex, endIndex);

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
            // TODO: 这里将来会调用Service层获取真实数据
            Map<String, Object> reviewDetail = generateMockReviewDetail(id);

            result.put("success", true);
            result.put("data", reviewDetail);

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
            // TODO: 这里将来会实现真实的导出功能
            // 目前返回模拟数据
            List<Map<String, Object>> mockData = generateMockReviewResults();
            List<Map<String, Object>> filteredData = applyFilters(mockData, sampleId, status, reviewer, reviewDate);

            result.put("success", true);
            result.put("message", "导出功能开发中，当前返回筛选后的数据");
            result.put("data", filteredData);
            result.put("total", filteredData.size());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "导出评审结果失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 生成模拟评审结果数据
     * @return 模拟数据列表
     */
    private List<Map<String, Object>> generateMockReviewResults() {
        List<Map<String, Object>> mockData = new ArrayList<>();
        String[] statuses = {"待评审", "已通过", "已拒绝"};
        String[] reviewers = {"张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十"};

        for (int i = 1; i <= 50; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", (long) i);
            item.put("sampleId", "SAMPLE" + String.format("%04d", i));
            item.put("status", statuses[i % statuses.length]);
            item.put("reviewer", reviewers[i % reviewers.length]);
            item.put("reviewDate", "2024-" + String.format("%02d", (i % 12) + 1) + "-" + String.format("%02d", (i % 28) + 1));
            item.put("reviewComment", "这是第" + i + "个样品的评审意见，评审内容详细描述...");
            item.put("createTime", new Date());
            item.put("updateTime", new Date());
            mockData.add(item);
        }

        return mockData;
    }

    /**
     * 生成模拟评审详情数据
     * @param id 评审结果ID
     * @return 模拟详情数据
     */
    private Map<String, Object> generateMockReviewDetail(Long id) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", id);
        detail.put("sampleId", "SAMPLE" + String.format("%04d", id.intValue()));
        detail.put("status", "已通过");
        detail.put("reviewer", "张三");
        detail.put("reviewDate", "2024-01-15");
        detail.put("reviewComment", "样品质量良好，符合要求，建议通过。");
        detail.put("createTime", new Date());
        detail.put("updateTime", new Date());
        detail.put("attachments", Arrays.asList("attachment1.jpg", "attachment2.pdf"));
        detail.put("history", Arrays.asList(
            "2024-01-15 10:30 张三 提交评审",
            "2024-01-15 11:00 李四 审核通过"
        ));

        return detail;
    }

    /**
     * 应用筛选条件
     * @param data 原始数据
     * @param sampleId 样品ID筛选
     * @param status 状态筛选
     * @param reviewer 评审人筛选
     * @param reviewDate 评审日期筛选
     * @return 筛选后的数据
     */
    private List<Map<String, Object>> applyFilters(List<Map<String, Object>> data,
                                                   String sampleId, String status,
                                                   String reviewer, String reviewDate) {
        return data.stream()
                .filter(item -> sampleId == null || sampleId.trim().isEmpty() ||
                        item.get("sampleId").toString().toLowerCase().contains(sampleId.toLowerCase()))
                .filter(item -> status == null || status.trim().isEmpty() ||
                        item.get("status").toString().equals(status))
                .filter(item -> reviewer == null || reviewer.trim().isEmpty() ||
                        item.get("reviewer").toString().toLowerCase().contains(reviewer.toLowerCase()))
                .filter(item -> reviewDate == null || reviewDate.trim().isEmpty() ||
                        item.get("reviewDate").toString().equals(reviewDate))
                .collect(ArrayList::new, (list, item) -> list.add(item), ArrayList::addAll);
    }
}
