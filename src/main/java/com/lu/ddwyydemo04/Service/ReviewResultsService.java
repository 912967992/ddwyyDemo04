package com.lu.ddwyydemo04.Service;

import com.lu.ddwyydemo04.dao.ReviewResultsDao;
import com.lu.ddwyydemo04.pojo.ReviewResults;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 评审结果服务层
 */
@Service
public class ReviewResultsService {

    @Autowired
    private ReviewResultsDao reviewResultsDao;

    /**
     * 获取所有评审结果数据
     * @return 评审结果列表
     */
    public List<ReviewResults> getAllReviewResults() {
        List<ReviewResults> results = reviewResultsDao.findAll();
//        System.out.println("Service层获取到的数据量: " + (results != null ? results.size() : 0));
//        if (results != null && !results.isEmpty()) {
//            System.out.println("第一条数据: " + results.get(0).getMajorCode() + " - " + results.get(0).getMinorCode());
//        }
        return results;
    }

    /**
     * 根据ID获取评审结果
     * @param id 评审结果ID
     * @return 评审结果对象
     */
    public ReviewResults getReviewResultById(Long id) {
        return reviewResultsDao.findById(id);
    }

    /**
     * 导入Excel文件并解析评审结果数据
     * @param file Excel文件
     * @return 导入结果统计
     */
    public ImportResult importReviewResultsFromExcel(MultipartFile file) {
        ImportResult result = new ImportResult();
        
        try {
            // 检查文件格式
            if (!isExcelFile(file)) {
                result.setSuccess(false);
                result.setMessage("文件格式不正确，请上传.xlsx格式的Excel文件");
                return result;
            }

            // 解析Excel文件
            List<ReviewResults> reviewResultsList = parseExcelFile(file);
            
            if (reviewResultsList.isEmpty()) {
                result.setSuccess(false);
                result.setMessage("未找到符合条件的工作表或数据为空");
                return result;
            }

            // 处理数据插入和更新
            int insertCount = 0;
            int updateCount = 0;
            int errorCount = 0;

            for (ReviewResults reviewResult : reviewResultsList) {
                try {
                    // 检查是否存在相同记录
                    Long existingId = reviewResultsDao.findExistingRecord(
                        reviewResult.getMajorCode(),
                        reviewResult.getMinorCode(),
                        reviewResult.getProjectPhase(),
                        reviewResult.getVersion(),
                        reviewResult.getSupplier(),
                        reviewResult.getProblemPoint()
                    );

                    if (existingId != null) {
                        // 更新现有记录
                        reviewResult.setId(existingId);
                        reviewResult.setUpdatedTime(LocalDateTime.now());
                        reviewResultsDao.update(reviewResult);
                        updateCount++;
                    } else {
                        // 插入新记录
                        reviewResult.setCreatedTime(LocalDateTime.now());
                        reviewResult.setUpdatedTime(LocalDateTime.now());
                        reviewResultsDao.insert(reviewResult);
                        insertCount++;
                    }
                } catch (Exception e) {
                    errorCount++;
                    System.err.println("处理记录时出错: " + e.getMessage());
                }
            }

            result.setSuccess(true);
            result.setMessage(String.format("导入完成！新增 %d 条记录，更新 %d 条记录，错误 %d 条记录", 
                insertCount, updateCount, errorCount));
            result.setInsertCount(insertCount);
            result.setUpdateCount(updateCount);
            result.setErrorCount(errorCount);

        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("导入失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 解析Excel文件
     * @param file Excel文件
     * @return 评审结果列表
     */
    private List<ReviewResults> parseExcelFile(MultipartFile file) throws IOException {
        List<ReviewResults> reviewResultsList = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            // 遍历所有工作表
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                String sheetName = sheet.getSheetName();
                
                // 只处理工作表名字包含"问题汇总清单"的工作表
                if (sheetName.contains("问题汇总清单")) {
                    List<ReviewResults> sheetData = parseSheet(sheet);
                    reviewResultsList.addAll(sheetData);
                }
            }
        }
        
        return reviewResultsList;
    }

    /**
     * 解析单个工作表
     * @param sheet 工作表
     * @return 评审结果列表
     */
    private List<ReviewResults> parseSheet(Sheet sheet) {
        List<ReviewResults> reviewResultsList = new ArrayList<>();
        
        // 获取表头行（假设第一行是表头）
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return reviewResultsList;
        }
        
        // 创建列索引映射
        ColumnMapping columnMapping = createColumnMapping(headerRow);
        
        // 打印列映射信息
//        System.out.println("列映射信息:");
//        System.out.println("测试日期列: " + columnMapping.testDateIndex);
//        System.out.println("大编码列: " + columnMapping.majorCodeIndex);
//        System.out.println("小编码列: " + columnMapping.minorCodeIndex);
//        System.out.println("项目阶段列: " + columnMapping.projectPhaseIndex);
//        System.out.println("版本列: " + columnMapping.versionIndex);
//        System.out.println("供应商列: " + columnMapping.supplierIndex);
//        System.out.println("问题点列: " + columnMapping.problemPointIndex);
        
        // 从第二行开始解析数据
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;
            
            try {
                ReviewResults reviewResult = parseRow(row, columnMapping);
                if (reviewResult != null) {
                    reviewResultsList.add(reviewResult);
//                    System.out.println("成功解析第" + (rowIndex + 1) + "行数据: " +
//                        "大编码=" + reviewResult.getMajorCode() +
//                        ", 小编码=" + reviewResult.getMinorCode() +
//                        ", 项目阶段=" + reviewResult.getProjectPhase() +
//                        ", 问题点=" + reviewResult.getProblemPoint());
                } else {
                    // 创建一个临时对象来获取缺失字段信息
                    ReviewResults tempResult = new ReviewResults();
                    tempResult.setTestDate(getCellValueAsDate(getCellSafely(row, columnMapping.testDateIndex)));
                    tempResult.setMajorCode(getCellValueAsString(getCellSafely(row, columnMapping.majorCodeIndex)));
                    tempResult.setMinorCode(getCellValueAsString(getCellSafely(row, columnMapping.minorCodeIndex)));
                    tempResult.setProjectPhase(getCellValueAsString(getCellSafely(row, columnMapping.projectPhaseIndex)));
                    tempResult.setVersion(getCellValueAsString(getCellSafely(row, columnMapping.versionIndex)));
                    tempResult.setSupplier(getCellValueAsString(getCellSafely(row, columnMapping.supplierIndex)));
                    
                    // 检查具体缺失的字段
                    StringBuilder missingFields = new StringBuilder();
                    if (tempResult.getMajorCode() == null || tempResult.getMajorCode().trim().isEmpty()) {
                        missingFields.append("大编码 ");
                    }
                    if (tempResult.getMinorCode() == null || tempResult.getMinorCode().trim().isEmpty()) {
                        missingFields.append("小编码 ");
                    }
                    if (tempResult.getProjectPhase() == null || tempResult.getProjectPhase().trim().isEmpty()) {
                        missingFields.append("项目阶段 ");
                    }
                    if (tempResult.getVersion() == null || tempResult.getVersion().trim().isEmpty()) {
                        missingFields.append("版本 ");
                    }
                    if (tempResult.getSupplier() == null || tempResult.getSupplier().trim().isEmpty()) {
                        missingFields.append("供应商 ");
                    }
                    
//                    System.out.println("跳过第" + (rowIndex + 1) + "行数据（缺失必填字段: " + missingFields.toString().trim() + "）");
                }
            } catch (Exception e) {
                System.err.println("解析第" + (rowIndex + 1) + "行数据时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return reviewResultsList;
    }

    /**
     * 创建列索引映射
     * @param headerRow 表头行
     * @return 列映射对象
     */
    private ColumnMapping createColumnMapping(Row headerRow) {
        ColumnMapping mapping = new ColumnMapping();
        
        for (int cellIndex = 0; cellIndex < headerRow.getLastCellNum(); cellIndex++) {
            Cell cell = headerRow.getCell(cellIndex);
            if (cell == null) continue;
            
            String headerValue = getCellValueAsString(cell).trim();
            
            // 根据表头内容映射到对应字段
            if (headerValue.contains("测试日期") || headerValue.contains("日期")) {
                mapping.testDateIndex = cellIndex;
            } else if (headerValue.contains("大编码") || headerValue.contains("大码")) {
                mapping.majorCodeIndex = cellIndex;
            } else if (headerValue.contains("小编码") || headerValue.contains("小码")) {
                mapping.minorCodeIndex = cellIndex;
            } else if (headerValue.contains("项目阶段") || headerValue.contains("阶段")) {
                mapping.projectPhaseIndex = cellIndex;
            } else if (headerValue.contains("版本")) {
                mapping.versionIndex = cellIndex;
            } else if (headerValue.contains("问题工序") || headerValue.contains("工序")) {
                mapping.problemProcessIndex = cellIndex;
            } else if (headerValue.contains("问题等级") || headerValue.contains("等级")) {
                mapping.problemLevelIndex = cellIndex;
            } else if (headerValue.contains("开发方式") || headerValue.contains("开发")) {
                mapping.developmentMethodIndex = cellIndex;
            } else if (headerValue.contains("供应商")) {
                mapping.supplierIndex = cellIndex;
            } else if (headerValue.contains("方案商")) {
                mapping.solutionProviderIndex = cellIndex;
            } else if (headerValue.contains("问题点")) {
                mapping.problemPointIndex = cellIndex;
            } else if (headerValue.contains("问题原因") || headerValue.contains("原因")) {
                mapping.problemReasonIndex = cellIndex;
            } else if (headerValue.contains("改善对策") || headerValue.contains("对策")) {
                mapping.improvementMeasuresIndex = cellIndex;
            } else if (headerValue.contains("是否可预防") || headerValue.contains("可预防")) {
                mapping.isPreventableIndex = cellIndex;
            } else if (headerValue.contains("责任部门") || headerValue.contains("部门")) {
                mapping.responsibleDepartmentIndex = cellIndex;
            } else if (headerValue.contains("预计完成时间") || headerValue.contains("预计")) {
                mapping.plannedCompletionTimeIndex = cellIndex;
            } else if (headerValue.contains("实际完成时间") || headerValue.contains("实际")) {
                mapping.actualCompletionTimeIndex = cellIndex;
            } else if (headerValue.contains("Delay天数") || headerValue.contains("延迟")) {
                mapping.delayDaysIndex = cellIndex;
            } else if (headerValue.contains("问题状态") || headerValue.contains("状态")) {
                mapping.problemStatusIndex = cellIndex;
            } else if (headerValue.contains("问题打标1") || headerValue.contains("打标1")) {
                mapping.problemTag1Index = cellIndex;
            } else if (headerValue.contains("问题打标2") || headerValue.contains("打标2")) {
                mapping.problemTag2Index = cellIndex;
            } else if (headerValue.contains("预防备注") || headerValue.contains("备注")) {
                mapping.preventionNotesIndex = cellIndex;
            }
        }
        
        return mapping;
    }

    /**
     * 解析单行数据
     * @param row 数据行
     * @param mapping 列映射
     * @return 评审结果对象
     */
    private ReviewResults parseRow(Row row, ColumnMapping mapping) {
        ReviewResults reviewResult = new ReviewResults();
        
        // 解析各个字段，使用安全的getter方法
        reviewResult.setTestDate(getCellValueAsDate(getCellSafely(row, mapping.testDateIndex)));
        reviewResult.setMajorCode(getCellValueAsString(getCellSafely(row, mapping.majorCodeIndex)));
        reviewResult.setMinorCode(getCellValueAsString(getCellSafely(row, mapping.minorCodeIndex)));
        reviewResult.setProjectPhase(getCellValueAsString(getCellSafely(row, mapping.projectPhaseIndex)));
        reviewResult.setVersion(getCellValueAsString(getCellSafely(row, mapping.versionIndex)));
        reviewResult.setProblemProcess(getCellValueAsString(getCellSafely(row, mapping.problemProcessIndex)));
        reviewResult.setProblemLevel(getCellValueAsString(getCellSafely(row, mapping.problemLevelIndex)));
        reviewResult.setDevelopmentMethod(getCellValueAsString(getCellSafely(row, mapping.developmentMethodIndex)));
        reviewResult.setSupplier(getCellValueAsString(getCellSafely(row, mapping.supplierIndex)));
        reviewResult.setSolutionProvider(getCellValueAsString(getCellSafely(row, mapping.solutionProviderIndex)));
        reviewResult.setProblemPoint(getCellValueAsString(getCellSafely(row, mapping.problemPointIndex)));
        reviewResult.setProblemReason(getCellValueAsString(getCellSafely(row, mapping.problemReasonIndex)));
        reviewResult.setImprovementMeasures(getCellValueAsString(getCellSafely(row, mapping.improvementMeasuresIndex)));
        reviewResult.setIsPreventable(getCellValueAsString(getCellSafely(row, mapping.isPreventableIndex)));
        reviewResult.setResponsibleDepartment(getCellValueAsString(getCellSafely(row, mapping.responsibleDepartmentIndex)));
        reviewResult.setPlannedCompletionTime(getCellValueAsDateTime(getCellSafely(row, mapping.plannedCompletionTimeIndex)));
        reviewResult.setActualCompletionTime(getCellValueAsDateTime(getCellSafely(row, mapping.actualCompletionTimeIndex)));
        reviewResult.setDelayDays(getCellValueAsInteger(getCellSafely(row, mapping.delayDaysIndex)));
        reviewResult.setProblemStatus(getCellValueAsString(getCellSafely(row, mapping.problemStatusIndex)));
        reviewResult.setProblemTag1(getCellValueAsString(getCellSafely(row, mapping.problemTag1Index)));
        reviewResult.setProblemTag2(getCellValueAsString(getCellSafely(row, mapping.problemTag2Index)));
        reviewResult.setPreventionNotes(getCellValueAsString(getCellSafely(row, mapping.preventionNotesIndex)));
        
        // 检查必填字段
        StringBuilder missingFields = new StringBuilder();
        
        if (reviewResult.getMajorCode() == null || reviewResult.getMajorCode().trim().isEmpty()) {
            missingFields.append("大编码 ");
        }
        if (reviewResult.getMinorCode() == null || reviewResult.getMinorCode().trim().isEmpty()) {
            missingFields.append("小编码 ");
        }
        if (reviewResult.getProjectPhase() == null || reviewResult.getProjectPhase().trim().isEmpty()) {
            missingFields.append("项目阶段 ");
        }
        if (reviewResult.getVersion() == null || reviewResult.getVersion().trim().isEmpty()) {
            missingFields.append("版本 ");
        }
        if (reviewResult.getSupplier() == null || reviewResult.getSupplier().trim().isEmpty()) {
            missingFields.append("供应商 ");
        }
        
        if (missingFields.length() > 0) {
            // 将缺失字段信息存储到对象中，供调用方使用
            reviewResult.setPreventionNotes("缺失必填字段: " + missingFields.toString().trim());
            return null; // 跳过必填字段为空的记录
        }
        
        return reviewResult;
    }

    /**
     * 安全获取单元格，避免索引越界
     */
    private Cell getCellSafely(Row row, int columnIndex) {
        if (row == null || columnIndex < 0) {
            return null;
        }
        return row.getCell(columnIndex);
    }

    /**
     * 获取单元格字符串值
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue().trim();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue().toString();
                    } else {
                        double numericValue = cell.getNumericCellValue();
                        // 如果是整数，不显示小数点
                        if (numericValue == (long) numericValue) {
                            return String.valueOf((long) numericValue);
                        } else {
                            return String.valueOf(numericValue);
                        }
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    // 处理公式，尝试获取计算结果
                    try {
                        if (cell.getCachedFormulaResultType() == CellType.STRING) {
                            return cell.getStringCellValue().trim();
                        } else if (cell.getCachedFormulaResultType() == CellType.NUMERIC) {
                            double numericValue = cell.getNumericCellValue();
                            if (numericValue == (long) numericValue) {
                                return String.valueOf((long) numericValue);
                            } else {
                                return String.valueOf(numericValue);
                            }
                        } else {
                            // 如果无法获取计算结果，返回公式本身
                            return cell.getCellFormula();
                        }
                    } catch (Exception e) {
                        // 公式解析失败，返回公式本身
                        return cell.getCellFormula();
                    }
                default:
                    return "";
            }
        } catch (Exception e) {
            System.err.println("解析单元格字符串失败: " + e.getMessage());
            return "";
        }
    }

    /**
     * 获取单元格日期值
     */
    private LocalDate getCellValueAsDate(Cell cell) {
        if (cell == null) return null;
        
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                if (DateUtil.isCellDateFormatted(cell)) {
                    // 标准日期格式
                    return cell.getDateCellValue().toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();
                } else {
                    // Excel日期序列号格式（从1900年1月1日开始计算）
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue > 0 && numericValue < 100000) { // 合理的日期范围
                        return DateUtil.getJavaDate(numericValue).toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate();
                    }
                }
            } else if (cell.getCellType() == CellType.STRING) {
                String dateStr = cell.getStringCellValue().trim();
                if (!dateStr.isEmpty()) {
                    // 尝试多种日期格式
                    String[] patterns = {
                        "yyyy-MM-dd", "yyyy/MM/dd", "MM/dd/yyyy", "dd/MM/yyyy",
                        "yyyy年MM月dd日", "MM月dd日", "yyyy-MM", "yyyy"
                    };
                    
                    for (String pattern : patterns) {
                        try {
                            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
                        } catch (Exception ignored) {
                            // 继续尝试下一个格式
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析日期失败: " + e.getMessage() + ", 单元格值: " + getCellValueAsString(cell));
        }
        
        return null;
    }

    /**
     * 获取单元格日期时间值
     */
    private LocalDateTime getCellValueAsDateTime(Cell cell) {
        if (cell == null) return null;
        
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                if (DateUtil.isCellDateFormatted(cell)) {
                    // 标准日期时间格式
                    return cell.getDateCellValue().toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime();
                } else {
                    // Excel日期序列号格式
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue > 0 && numericValue < 100000) {
                        return DateUtil.getJavaDate(numericValue).toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime();
                    }
                }
            } else if (cell.getCellType() == CellType.STRING) {
                String dateTimeStr = cell.getStringCellValue().trim();
                if (!dateTimeStr.isEmpty()) {
                    // 尝试多种日期时间格式
                    String[] patterns = {
                        "yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss", "MM/dd/yyyy HH:mm:ss",
                        "yyyy-MM-dd", "yyyy/MM/dd", "MM/dd/yyyy",
                        "yyyy年MM月dd日 HH:mm:ss", "yyyy年MM月dd日"
                    };
                    
                    for (String pattern : patterns) {
                        try {
                            if (pattern.contains("HH:mm:ss")) {
                                return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
                            } else {
                                return LocalDate.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern))
                                    .atStartOfDay();
                            }
                        } catch (Exception ignored) {
                            // 继续尝试下一个格式
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析日期时间失败: " + e.getMessage() + ", 单元格值: " + getCellValueAsString(cell));
        }
        
        return null;
    }

    /**
     * 获取单元格整数值
     */
    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null) return null;
        
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.FORMULA) {
                // 处理Excel公式
                try {
                    // 尝试获取公式的计算结果
                    if (cell.getCachedFormulaResultType() == CellType.NUMERIC) {
                        return (int) cell.getNumericCellValue();
                    } else {
                        // 如果公式结果是字符串，尝试解析
                        String formulaResult = cell.getStringCellValue();
                        if (formulaResult != null && !formulaResult.trim().isEmpty()) {
                            // 移除可能的文本内容，只保留数字
                            String numericPart = formulaResult.replaceAll("[^0-9.-]", "");
                            if (!numericPart.isEmpty()) {
                                return Integer.parseInt(numericPart);
                            }
                        }
                    }
                } catch (Exception e) {
                    // 公式解析失败，返回null
                    System.err.println("解析公式失败: " + e.getMessage() + ", 公式: " + cell.getCellFormula());
                }
            } else {
                String intStr = getCellValueAsString(cell);
                if (intStr != null && !intStr.trim().isEmpty()) {
                    // 检查是否包含公式或特殊字符
                    if (intStr.contains("IF(") || intStr.contains("TODAY()") || intStr.contains("=")) {
                        // 这是Excel公式，尝试提取数字部分
                        String numericPart = intStr.replaceAll("[^0-9.-]", "");
                        if (!numericPart.isEmpty()) {
                            return Integer.parseInt(numericPart);
                        }
                        return null; // 无法解析的公式
                    } else {
                        // 普通字符串，尝试解析为整数
                        return Integer.parseInt(intStr);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析整数失败: " + e.getMessage() + ", 单元格值: " + getCellValueAsString(cell));
        }
        
        return null;
    }

    /**
     * 检查是否为Excel文件
     */
    private boolean isExcelFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        return fileName != null && fileName.toLowerCase().endsWith(".xlsx");
    }

    /**
     * 列映射内部类
     */
    private static class ColumnMapping {
        int testDateIndex = -1;
        int majorCodeIndex = -1;
        int minorCodeIndex = -1;
        int projectPhaseIndex = -1;
        int versionIndex = -1;
        int problemProcessIndex = -1;
        int problemLevelIndex = -1;
        int developmentMethodIndex = -1;
        int supplierIndex = -1;
        int solutionProviderIndex = -1;
        int problemPointIndex = -1;
        int problemReasonIndex = -1;
        int improvementMeasuresIndex = -1;
        int isPreventableIndex = -1;
        int responsibleDepartmentIndex = -1;
        int plannedCompletionTimeIndex = -1;
        int actualCompletionTimeIndex = -1;
        int delayDaysIndex = -1;
        int problemStatusIndex = -1;
        int problemTag1Index = -1;
        int problemTag2Index = -1;
        int preventionNotesIndex = -1;
    }

    /**
     * 导入结果内部类
     */
    public static class ImportResult {
        private boolean success;
        private String message;
        private int insertCount;
        private int updateCount;
        private int errorCount;

        // Getters and Setters
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getInsertCount() {
            return insertCount;
        }

        public void setInsertCount(int insertCount) {
            this.insertCount = insertCount;
        }

        public int getUpdateCount() {
            return updateCount;
        }

        public void setUpdateCount(int updateCount) {
            this.updateCount = updateCount;
        }

        public int getErrorCount() {
            return errorCount;
        }

        public void setErrorCount(int errorCount) {
            this.errorCount = errorCount;
        }
    }

    /**
     * 更新评审结果详情
     * @param reviewResults 评审结果对象
     * @return 更新结果
     */
    public boolean updateReviewResult(ReviewResults reviewResults) {
        try {
            // 设置更新时间
            reviewResults.setUpdatedTime(LocalDateTime.now());
            
            // 调用DAO层更新数据
            int affectedRows = reviewResultsDao.update(reviewResults);
            
            return affectedRows > 0;
        } catch (Exception e) {
            System.err.println("更新评审结果失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 插入评审结果
     * @param reviewResults 评审结果对象
     * @return 插入的记录数
     */
    public int insertReviewResult(ReviewResults reviewResults) {
        try {
            if (reviewResults == null) {
                return 0;
            }
            
            // 调用DAO层插入数据
            int insertCount = reviewResultsDao.insert(reviewResults);
            
//            System.out.println("插入评审结果: " + (insertCount > 0 ? "成功" : "失败"));
            
            return insertCount;
        } catch (Exception e) {
            System.err.println("插入评审结果失败: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 批量删除评审结果
     * @param ids 要删除的ID列表
     * @return 删除的记录数
     */
    public int deleteReviewResultsByIds(List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return 0;
            }
            
            // 调用DAO层批量删除
            int deletedCount = reviewResultsDao.deleteByIds(ids);
            
            System.out.println("批量删除评审结果: 请求删除 " + ids.size() + " 条，实际删除 " + deletedCount + " 条");
            
            return deletedCount;
        } catch (Exception e) {
            System.err.println("批量删除评审结果失败: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
}
