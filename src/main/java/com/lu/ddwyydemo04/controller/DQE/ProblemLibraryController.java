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

    /**
     * 获取所有问题点
     */
    @GetMapping("/getProblems")
    public ResponseEntity<Map<String, Object>> getProblems() {
        try {
            List<TestIssues> problems = problemLibraryService.getAllProblems();
            Map<String, Integer> stats = problemLibraryService.getStatistics(problems);

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
            response.put("message", "获取问题点数据失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 搜索问题点
     */
    @PostMapping("/searchProblems")
    public ResponseEntity<Map<String, Object>> searchProblems(@RequestBody Map<String, Object> filters) {
        try {
            List<TestIssues> problems = problemLibraryService.searchProblems(filters);
            Map<String, Integer> stats = problemLibraryService.getStatistics(problems);

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
            System.out.println("problems:"+problems);

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
            for (int i = 0; i < 44; i++) sheet.setColumnWidth(i, 20 * 256);

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

            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 43));

            // ====== 表头 ======
            Row headerRow = sheet.createRow(1);
            headerRow.setHeightInPoints(50);
            String[] headers = {
                    "序号", "样品ID", "完整编码", "SKU", "样品阶段", "版本", "芯片方案", "测试平台", "测试设备", "其他设备",
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
                createCell(row, 2, issue.getFull_model(), centeredStyle);
                createCell(row, 3, issue.getSku(), centeredStyle);
                createCell(row, 4, issue.getSample_stage(), centeredStyle);
                createCell(row, 5, issue.getVersion(), centeredStyle);
                createCell(row, 6, issue.getChip_solution(), centeredStyle);
                createCell(row, 7, issue.getTest_platform(), centeredStyle);
                createCell(row, 8, issue.getTest_device(), centeredStyle);
                createCell(row, 9, issue.getOther_device(), centeredStyle);
                createCell(row, 10, issue.getProblem(), centeredStyle);
                createCell(row, 11, issue.getProblemCategory(), centeredStyle);
                createCell(row, 12, issue.getProblem_image_or_video(), centeredStyle);
                createCell(row, 13, issue.getProblem_time(), centeredStyle);
                createCell(row, 14, issue.getReproduction_method(), centeredStyle);
                createCell(row, 15, issue.getRecovery_method(), centeredStyle);
                createCell(row, 16, issue.getReproduction_probability(), centeredStyle);
                createCell(row, 17, issue.getDefect_level(), centeredStyle);
                createCell(row, 18, issue.getCurrent_status(), centeredStyle);
                createCell(row, 19, issue.getComparison_with_previous(), centeredStyle);
                createCell(row, 20, issue.getTester(), centeredStyle);
                createCell(row, 21, issue.getImprovement_plan(), centeredStyle);
                createCell(row, 22, issue.getResponsible_person(), centeredStyle);
                createCell(row, 23, issue.getPost_improvement_risk(), centeredStyle);
                createCell(row, 24, issue.getNext_version_regression_test(), centeredStyle);
                createCell(row, 25, issue.getRemark(), centeredStyle);
                createCell(row, 26, issue.getCreated_at(), centeredStyle);
                createCell(row, 27, issue.getHistory_id(), centeredStyle);
                createCell(row, 28, issue.getCreated_by(), centeredStyle);
                createCell(row, 29, issue.getDqe_confirm(), centeredStyle);
                createCell(row, 30, issue.getDqe_review_at(), centeredStyle);
                createCell(row, 31, issue.getDqe(), centeredStyle);
                createCell(row, 32, issue.getRd_confirm(), centeredStyle);
                createCell(row, 33, issue.getRd_review_at(), centeredStyle);
                createCell(row, 34, issue.getRd(), centeredStyle);
                createCell(row, 35, issue.getModifier(), centeredStyle);
                createCell(row, 36, issue.getModify_at(), centeredStyle);
                createCell(row, 37, issue.getResponsibleDepartment(), centeredStyle);
                createCell(row, 38, issue.getGreen_union_dqe(), centeredStyle);
                createCell(row, 39, issue.getGreen_union_rd(), centeredStyle);
                createCell(row, 40, issue.getSolution_provider(), centeredStyle);
                createCell(row, 41, issue.getSupplier(), centeredStyle);
                createCell(row, 42, issue.getReview_conclusion(), centeredStyle);
                createCell(row, 43, issue.getTest_Overseas(), centeredStyle);

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
            Sheet sheet = workbook.createSheet("收藏夹合并数据");

            // ====== 样式 ======
            CellStyle centeredStyle = workbook.createCellStyle();
            centeredStyle.setAlignment(HorizontalAlignment.CENTER);
            centeredStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            centeredStyle.setBorderTop(BorderStyle.THIN);
            centeredStyle.setBorderBottom(BorderStyle.THIN);
            centeredStyle.setBorderLeft(BorderStyle.THIN);
            centeredStyle.setBorderRight(BorderStyle.THIN);

            // 列宽
            for (int i = 0; i < 19; i++) sheet.setColumnWidth(i, 20 * 256);

            // ====== 标题 ======
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("收藏夹合并数据");

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

            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 18));

            // ====== 表头 ======
            Row headerRow = sheet.createRow(1);
            headerRow.setHeightInPoints(50);
            String[] headers = {
                    "ID", "完整编码", "大类", "小类", "样品阶段", "版本", "测试平台", 
                    "显示设备", "其他设备", "问题描述", "问题类别", "缺陷等级", "当前状态", 
                    "测试人员", "DQE负责人", "责任部门", "DQE确认回复", "研发确认回复", "提交时间"
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
                createCell(row, 1, item.get("full_model"), centeredStyle);
                createCell(row, 2, item.get("big_species"), centeredStyle);
                createCell(row, 3, item.get("small_species"), centeredStyle);
                createCell(row, 4, item.get("sample_stage"), centeredStyle);
                createCell(row, 5, item.get("version"), centeredStyle);
                createCell(row, 6, item.get("test_platform"), centeredStyle);
                createCell(row, 7, item.get("test_device"), centeredStyle);
                createCell(row, 8, item.get("other_device"), centeredStyle);
                createCell(row, 9, item.get("problem"), centeredStyle);
                createCell(row, 10, item.get("problemCategory"), centeredStyle);
                createCell(row, 11, item.get("defect_level"), centeredStyle);
                createCell(row, 12, item.get("current_status"), centeredStyle);
                createCell(row, 13, item.get("tester"), centeredStyle);
                createCell(row, 14, item.get("dqe"), centeredStyle);
                createCell(row, 15, item.get("responsibleDepartment"), centeredStyle);
                createCell(row, 16, item.get("green_union_dqe"), centeredStyle);
                createCell(row, 17, item.get("green_union_rd"), centeredStyle);
                createCell(row, 18, item.get("created_at"), centeredStyle);

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
