package com.lu.ddwyydemo04.Service;

import com.lu.ddwyydemo04.dao.ReviewResultsDao;
import com.lu.ddwyydemo04.pojo.ReviewResults;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * 根据ID列表获取评审结果
     * @param ids ID列表
     * @return 评审结果列表
     */
    public List<ReviewResults> getReviewResultsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return reviewResultsDao.findByIds(ids);
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

            // 先进行预检查，获取所有解析的错误信息
            List<String> validationErrors = new ArrayList<>();
            List<ReviewResults> reviewResultsList = parseExcelFileWithValidation(file, validationErrors);
            
            // 如果有验证错误，直接返回错误信息，不允许导入
            if (!validationErrors.isEmpty()) {
                result.setSuccess(false);
                StringBuilder errorMessage = new StringBuilder("导入失败：发现以下问题，请修正后再导入：\\n\\n");
                int maxErrors = Math.min(validationErrors.size(), 10); // 最多显示10条错误
                for (int i = 0; i < maxErrors; i++) {
                    errorMessage.append((i + 1)).append(". ").append(validationErrors.get(i)).append("\\n");
                }
                if (validationErrors.size() > 10) {
                    errorMessage.append("... 还有 ").append(validationErrors.size() - 10).append(" 条错误未显示\\n");
                }
                errorMessage.append("\\n必填数据字段：大编码、小编码、项目阶段、供应商、问题点");
                result.setMessage(errorMessage.toString());
                return result;
            }
            
            if (reviewResultsList.isEmpty()) {
                result.setSuccess(false);
                String errorMessage = "导入失败：未找到有效数据。";
                if (!lastParseError.isEmpty()) {
                    errorMessage += " " + lastParseError + "请检查数据行是否包含必填字段：大编码、小编码、项目阶段、供应商、问题点";
                } else {
                    errorMessage += " 可能原因：1)Excel文件中没有名为'问题汇总清单'的工作表；2)工作表存在但所有数据行都因必填字段缺失而被跳过。请检查数据行是否包含必填字段：大编码、小编码、项目阶段、供应商、问题点";
                }
                result.setMessage(errorMessage);
                return result;
            }

            // 处理数据插入和更新
            int insertCount = 0;
            int updateCount = 0;
            int errorCount = 0;

            for (ReviewResults reviewResult : reviewResultsList) {
                try {
                    // 检查是否存在相同记录（版本字段可为空）
                    Long existingId = reviewResultsDao.findExistingRecord(
                        reviewResult.getMajorCode(),
                        reviewResult.getMinorCode(),
                        reviewResult.getProjectPhase(),
                        reviewResult.getVersion(), // 版本字段可为null或空字符串
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
     * 解析Excel文件并收集验证错误
     * @param file Excel文件
     * @param validationErrors 验证错误列表
     * @return 评审结果列表
     */
    private List<ReviewResults> parseExcelFileWithValidation(MultipartFile file, List<String> validationErrors) throws IOException {
        List<ReviewResults> reviewResultsList = new ArrayList<>();
        boolean foundTargetSheet = false;
        int totalRows = 0;
        int skippedRows = 0;
        StringBuilder errorDetails = new StringBuilder();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            System.out.println("Excel文件总工作表数量: " + workbook.getNumberOfSheets());
            
            // 遍历所有工作表
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                String sheetName = sheet.getSheetName();
                System.out.println("工作表 " + sheetIndex + ": " + sheetName);
                
                // 只处理工作表名字包含"问题汇总清单"的工作表
                if (sheetName.contains("问题汇总清单")) {
                    foundTargetSheet = true;
                    System.out.println("找到匹配的工作表: " + sheetName);
                    
                    // 统计总行数和跳过的行数
                    totalRows = sheet.getLastRowNum();
                    System.out.println("工作表总行数: " + totalRows);
                    
                    List<ReviewResults> sheetData = parseSheetWithValidation(sheet, validationErrors);
                    skippedRows = totalRows - sheetData.size();
                    System.out.println("解析到数据条数: " + sheetData.size() + ", 跳过行数: " + skippedRows);
                    reviewResultsList.addAll(sheetData);
                    
                    // 如果跳过了很多行，记录详细信息
                    if (skippedRows > 0) {
                        errorDetails.append("工作表'").append(sheetName).append("'中跳过了").append(skippedRows).append("行数据，");
                    }
                }
            }
            
            System.out.println("最终解析结果总数: " + reviewResultsList.size());
            
            // 如果没有找到目标工作表，记录所有工作表名称
            if (!foundTargetSheet) {
                System.out.println("未找到包含'问题汇总清单'的工作表");
                errorDetails.append("未找到名为'问题汇总清单'的工作表，");
            }
        }
        
        // 将错误详情存储到静态变量中，供调用方使用
        if (errorDetails.length() > 0) {
            this.lastParseError = errorDetails.toString();
        }
        
        return reviewResultsList;
    }

    /**
     * 解析Excel文件
     * @param file Excel文件
     * @return 评审结果列表
     */
    private List<ReviewResults> parseExcelFile(MultipartFile file) throws IOException {
        List<ReviewResults> reviewResultsList = new ArrayList<>();
        boolean foundTargetSheet = false;
        int totalRows = 0;
        int skippedRows = 0;
        StringBuilder errorDetails = new StringBuilder();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            System.out.println("Excel文件总工作表数量: " + workbook.getNumberOfSheets());
            
            // 遍历所有工作表
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                String sheetName = sheet.getSheetName();
                System.out.println("工作表 " + sheetIndex + ": " + sheetName);
                
                // 只处理工作表名字包含"问题汇总清单"的工作表
                if (sheetName.contains("问题汇总清单")) {
                    foundTargetSheet = true;
                    System.out.println("找到匹配的工作表: " + sheetName);
                    
                    // 统计总行数和跳过的行数
                    totalRows = sheet.getLastRowNum();
                    System.out.println("工作表总行数: " + totalRows);
                    
                    List<ReviewResults> sheetData = parseSheet(sheet);
                    skippedRows = totalRows - sheetData.size();
                    System.out.println("解析到数据条数: " + sheetData.size() + ", 跳过行数: " + skippedRows);
                    reviewResultsList.addAll(sheetData);
                    
                    // 如果跳过了很多行，记录详细信息
                    if (skippedRows > 0) {
                        errorDetails.append("工作表'").append(sheetName).append("'中跳过了").append(skippedRows).append("行数据，");
                    }
                }
            }
            
            System.out.println("最终解析结果总数: " + reviewResultsList.size());
            
            // 如果没有找到目标工作表，记录所有工作表名称
            if (!foundTargetSheet) {
                System.out.println("未找到包含'问题汇总清单'的工作表");
                errorDetails.append("未找到名为'问题汇总清单'的工作表，");
            }
        }
        
        // 将错误详情存储到静态变量中，供调用方使用
        if (errorDetails.length() > 0) {
            this.lastParseError = errorDetails.toString();
        }
        
        return reviewResultsList;
    }
    
    // 用于存储解析错误的静态变量
    private String lastParseError = "";

    /**
     * 解析单个工作表并收集验证错误
     * @param sheet 工作表
     * @param validationErrors 验证错误列表
     * @return 评审结果列表
     */
    private List<ReviewResults> parseSheetWithValidation(Sheet sheet, List<String> validationErrors) {
        List<ReviewResults> reviewResultsList = new ArrayList<>();
        
        // 获取表头行（假设第一行是表头）
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            validationErrors.add("Excel文件第一行（表头）为空");
            return reviewResultsList;
        }
        
        // 第一步：先验证表头格式（字段完整性、顺序、列数）
        List<String> missingHeaders = validateHeaders(headerRow);
        if (!missingHeaders.isEmpty()) {
            StringBuilder headerError = new StringBuilder("表头格式不正确：");
            for (int i = 0; i < missingHeaders.size(); i++) {
                headerError.append(missingHeaders.get(i));
                if (i < missingHeaders.size() - 1) {
                    headerError.append("；");
                }
            }
            headerError.append("。请按模板调整");
            validationErrors.add(headerError.toString());
            // 表头格式不正确，直接返回，不进行后续的数据验证
            return reviewResultsList;
        }
        
        // 第二步：表头验证通过，创建列索引映射，开始验证数据行
        ColumnMapping columnMapping = createColumnMapping(headerRow);
        
        // 从第二行开始解析数据
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;
            
            try {
                // 第三步：检查数据行的必填字段（大编码、小编码、项目阶段、供应商、问题点）
                String group = getCellValueAsString(getCellSafely(row, columnMapping.groupIndex));
                String category = getCellValueAsString(getCellSafely(row, columnMapping.categoryIndex));
                String dqeResponsible = getCellValueAsString(getCellSafely(row, columnMapping.dqeResponsibleIndex));
                String majorCode = getCellValueAsString(getCellSafely(row, columnMapping.majorCodeIndex));
                String minorCode = getCellValueAsString(getCellSafely(row, columnMapping.minorCodeIndex));
                String projectPhase = getCellValueAsString(getCellSafely(row, columnMapping.projectPhaseIndex));
                String supplier = getCellValueAsString(getCellSafely(row, columnMapping.supplierIndex));
                String problemPoint = getCellValueAsString(getCellSafely(row, columnMapping.problemPointIndex));
                
                // 若“问题点”为空，直接跳过该数据行
                if (problemPoint == null || problemPoint.trim().isEmpty()) {
                    continue;
                }

                // 检查必填字段，发现第一个为空就立即停止并返回
                // 组别、品类、DQE负责人已改为可选字段，不再进行必填验证
                if (majorCode == null || majorCode.trim().isEmpty()) {
                    validationErrors.add("第" + (rowIndex + 1) + "行大编码不能为空");
                    return reviewResultsList;
                }
                if (minorCode == null || minorCode.trim().isEmpty()) {
                    validationErrors.add("第" + (rowIndex + 1) + "行小编码不能为空");
                    return reviewResultsList;
                }
                if (projectPhase == null || projectPhase.trim().isEmpty()) {
                    validationErrors.add("第" + (rowIndex + 1) + "行项目阶段不能为空");
                    return reviewResultsList;
                }
                if (supplier == null || supplier.trim().isEmpty()) {
                    validationErrors.add("第" + (rowIndex + 1) + "行供应商不能为空");
                    return reviewResultsList;
                }
                // “问题点”在前面已判断为空则跳过，此处不再作为报错拦截条件
                
                // 先检查各个格式字段
                // 检查Delay天数字段
                if (columnMapping.delayDaysIndex >= 0) {
                    String delayDaysStr = getCellValueAsString(getCellSafely(row, columnMapping.delayDaysIndex));
                    if (delayDaysStr != null && !delayDaysStr.trim().isEmpty()) {
                        try {
                            Integer.parseInt(delayDaysStr.trim());
                        } catch (NumberFormatException e) {
                            validationErrors.add("第" + (rowIndex + 1) + "行Delay天数格式错误：" + delayDaysStr + "（应为整数）");
                        }
                    }
                }
                
                // 检查发生日期字段
                if (columnMapping.testDateIndex >= 0) {
                    String testDateStr = getCellValueAsString(getCellSafely(row, columnMapping.testDateIndex));
                    if (testDateStr != null && !testDateStr.trim().isEmpty()) {
                        LocalDate testDate = getCellValueAsDate(getCellSafely(row, columnMapping.testDateIndex));
                        if (testDate == null) {
                            validationErrors.add("第" + (rowIndex + 1) + "行发生日期格式错误：" + testDateStr + "（应为日期格式，如：2025-01-01）");
                        }
                    }
                }
                
                // 检查预计完成时间字段
                if (columnMapping.plannedCompletionTimeIndex >= 0) {
                    String plannedTimeStr = getCellValueAsString(getCellSafely(row, columnMapping.plannedCompletionTimeIndex));
                    if (plannedTimeStr != null && !plannedTimeStr.trim().isEmpty()) {
                        LocalDateTime plannedTime = getCellValueAsDateTime(getCellSafely(row, columnMapping.plannedCompletionTimeIndex));
                        if (plannedTime == null) {
                            validationErrors.add("第" + (rowIndex + 1) + "行预计完成时间格式错误：" + plannedTimeStr + "（应为日期格式，如：2025-01-01）");
                        }
                    }
                }
                
                // 检查实际完成时间字段
                if (columnMapping.actualCompletionTimeIndex >= 0) {
                    String actualTimeStr = getCellValueAsString(getCellSafely(row, columnMapping.actualCompletionTimeIndex));
                    if (actualTimeStr != null && !actualTimeStr.trim().isEmpty()) {
                        LocalDateTime actualTime = getCellValueAsDateTime(getCellSafely(row, columnMapping.actualCompletionTimeIndex));
                        if (actualTime == null) {
                            validationErrors.add("第" + (rowIndex + 1) + "行实际完成时间格式错误：" + actualTimeStr + "（应为日期格式，如：2025-01-01）");
                        }
                    }
                }
                
                ReviewResults reviewResult = parseRow(row, columnMapping);
                if (reviewResult != null) {
                    reviewResultsList.add(reviewResult);
                }
                // 注意：必填字段检查已在上面完成，parseRow返回null时不需要再次检查
            } catch (Exception e) {
                System.err.println("解析第" + (rowIndex + 1) + "行数据时出错: " + e.getMessage());
                e.printStackTrace();
                validationErrors.add("第" + (rowIndex + 1) + "行数据解析失败: " + e.getMessage());
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
                    tempResult.setGroup(getCellValueAsString(getCellSafely(row, columnMapping.groupIndex)));
                    tempResult.setDqeResponsible(getCellValueAsString(getCellSafely(row, columnMapping.dqeResponsibleIndex)));
                    tempResult.setDataSource(getCellValueAsString(getCellSafely(row, columnMapping.dataSourceIndex)));
                    tempResult.setTestDate(getCellValueAsDate(getCellSafely(row, columnMapping.testDateIndex)));
                    tempResult.setMajorCode(getCellValueAsString(getCellSafely(row, columnMapping.majorCodeIndex)));
                    tempResult.setMinorCode(getCellValueAsString(getCellSafely(row, columnMapping.minorCodeIndex)));
                    tempResult.setProjectPhase(getCellValueAsString(getCellSafely(row, columnMapping.projectPhaseIndex)));
                    tempResult.setVersion(getCellValueAsString(getCellSafely(row, columnMapping.versionIndex)));
                    tempResult.setSupplier(getCellValueAsString(getCellSafely(row, columnMapping.supplierIndex)));
                    
                    // 检查具体缺失的字段
                    StringBuilder missingFields = new StringBuilder();
                    // 组别、品类、DQE负责人、数据来源已改为可选字段，不再进行检查
                    if (tempResult.getMinorCode() == null || tempResult.getMinorCode().trim().isEmpty()) {
                        missingFields.append("小编码 ");
                    }
                    if (tempResult.getProjectPhase() == null || tempResult.getProjectPhase().trim().isEmpty()) {
                        missingFields.append("项目阶段 ");
                    }
                    // 版本字段已改为可选，不再检查
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
     * 验证表头是否包含所有必需字段
     * @param headerRow 表头行
     * @return 缺少的字段列表
     */
    private List<String> validateHeaders(Row headerRow) {
        List<String> missingHeaders = new ArrayList<>();
        
        // 定义所有必需的字段及其可能的表头名称（按照指定顺序）
        List<String[]> requiredHeaders = new ArrayList<>();
        requiredHeaders.add(new String[]{"序号", "序号", "编号"});
        requiredHeaders.add(new String[]{"组别", "组别"});
        requiredHeaders.add(new String[]{"品类", "品类"});
        requiredHeaders.add(new String[]{"发生日期", "发生日期", "测试日期"});
        requiredHeaders.add(new String[]{"大编码", "大编码", "大码"});
        requiredHeaders.add(new String[]{"小编码", "小编码", "小码"});
        requiredHeaders.add(new String[]{"项目阶段", "项目阶段", "阶段"});
        requiredHeaders.add(new String[]{"版本", "版本"});
        requiredHeaders.add(new String[]{"问题工序", "问题工序", "工序"});
        requiredHeaders.add(new String[]{"问题等级", "问题等级", "等级"});
        requiredHeaders.add(new String[]{"开发方式", "开发方式", "开发"});
        requiredHeaders.add(new String[]{"供应商", "供应商"});
        requiredHeaders.add(new String[]{"方案商", "方案商"});
        requiredHeaders.add(new String[]{"问题点", "问题点"});
        requiredHeaders.add(new String[]{"问题原因", "问题原因", "原因"});
        requiredHeaders.add(new String[]{"改善对策", "改善对策", "对策"});
        requiredHeaders.add(new String[]{"是否可预防", "是否可预防", "可预防"});
        requiredHeaders.add(new String[]{"责任部门", "责任部门"});
        requiredHeaders.add(new String[]{"DQE责任人", "DQE负责人", "负责人", "DQE", "DQE责任人"});
        requiredHeaders.add(new String[]{"预计完成时间", "预计完成时间", "预计"});
        requiredHeaders.add(new String[]{"实际完成时间", "实际完成时间", "实际"});
        requiredHeaders.add(new String[]{"Delay天数", "Delay天数", "延迟"});
        requiredHeaders.add(new String[]{"问题状态", "问题状态", "状态"});
        requiredHeaders.add(new String[]{"问题打标1", "问题打标1", "打标1"});
        requiredHeaders.add(new String[]{"问题打标2", "问题打标2", "打标2"});
        requiredHeaders.add(new String[]{"预防备注", "预防备注", "备注"});
        requiredHeaders.add(new String[]{"数据来源", "数据来源", "来源"});
        
        // 获取所有表头内容（包括空列）
        List<String> headerValues = new ArrayList<>();
        int totalColumns = headerRow.getLastCellNum(); // 获取总列数，包括空列
        for (int cellIndex = 0; cellIndex < totalColumns; cellIndex++) {
            Cell cell = headerRow.getCell(cellIndex);
            if (cell != null) {
                String headerValue = getCellValueAsString(cell).trim();
                headerValues.add(headerValue); // 即使是空的也添加，保持列位置对应
            } else {
                headerValues.add(""); // 空单元格也要算作一列
            }
        }
        
        // 第一步：先检查每个必需字段是否存在（跳过空列）
        List<String> missingFields = new ArrayList<>();
        for (String[] requiredHeader : requiredHeaders) {
            String fieldName = requiredHeader[0];
            boolean found = false;
            
            for (String headerValue : headerValues) {
                if (headerValue == null || headerValue.trim().isEmpty()) {
                    continue; // 跳过空列
                }
                for (int i = 1; i < requiredHeader.length; i++) {
                    if (headerValue.contains(requiredHeader[i])) {
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }
            
            if (!found) {
                missingFields.add(fieldName);
            }
        }
        
        // 如果有缺失字段，直接返回缺失字段信息
        if (!missingFields.isEmpty()) {
            missingHeaders.add("缺少以下字段：" + String.join("、", missingFields));
            return missingHeaders;
        }
        
        // 第二步：检查字段数量（包括空列）
        String[] expectedOrder = {
            "序号", "组别", "品类", "发生日期", "大编码", "小编码", "项目阶段", "版本",
            "问题工序", "问题等级", "开发方式", "供应商", "方案商", "问题点", "问题原因", "改善对策",
            "是否可预防", "责任部门", "DQE责任人", "预计完成时间", "实际完成时间", "Delay天数",
            "问题状态", "问题打标1", "问题打标2", "预防备注", "数据来源"
        };
        
        // 计算非空列的数量
        int nonEmptyColumns = 0;
        for (String headerValue : headerValues) {
            if (headerValue != null && !headerValue.trim().isEmpty()) {
                nonEmptyColumns++;
            }
        }
        
        // 检查是否有空列（如果总列数超过期望的非空列数，说明有多余的列）
        if (totalColumns > expectedOrder.length) {
            missingHeaders.add(String.format("表头列数过多，期望%d列，实际%d列（包含空列）", expectedOrder.length, totalColumns));
            return missingHeaders;
        }
        
        // 如果非空列数不等于期望列数，说明缺少或多了字段
        if (nonEmptyColumns != expectedOrder.length) {
            missingHeaders.add(String.format("表头列数不正确，期望%d列，实际%d列（非空）", expectedOrder.length, nonEmptyColumns));
            return missingHeaders;
        }
        
        // 第三步：检查字段顺序
        List<String> orderErrors = new ArrayList<>();
        // 过滤空列，只检查非空列的位置
        List<Integer> nonEmptyIndices = new ArrayList<>();
        for (int i = 0; i < headerValues.size(); i++) {
            if (headerValues.get(i) != null && !headerValues.get(i).trim().isEmpty()) {
                nonEmptyIndices.add(i);
            }
        }
        
        // 检查每个非空列是否在正确的位置
        if (nonEmptyIndices.size() != expectedOrder.length) {
            // 这种情况应该已经在第二步被捕获了，但为了安全还是检查一下
            return missingHeaders;
        }
        
        for (int idx = 0; idx < nonEmptyIndices.size(); idx++) {
            int actualIndex = nonEmptyIndices.get(idx);
            String actualHeader = headerValues.get(actualIndex);
            String expectedField = expectedOrder[idx];
            
            boolean matches = false;
            for (String[] headerGroup : requiredHeaders) {
                if (headerGroup[0].equals(expectedField)) {
                    for (int j = 1; j < headerGroup.length; j++) {
                        if (actualHeader.contains(headerGroup[j])) {
                            matches = true;
                            break;
                        }
                    }
                    if (matches) break;
                }
            }
            
            if (!matches) {
                orderErrors.add(String.format("第%d列期望'%s'，实际'%s'", actualIndex + 1, expectedField, actualHeader));
            }
        }
        
        // 检查是否有空列在期望有字段的位置
        for (int i = 0; i < expectedOrder.length; i++) {
            if (i < headerValues.size()) {
                String headerValue = headerValues.get(i);
                if (headerValue == null || headerValue.trim().isEmpty()) {
                    orderErrors.add(String.format("第%d列应为'%s'，但该列为空", i + 1, expectedOrder[i]));
                }
            }
        }
        
        if (!orderErrors.isEmpty()) {
            missingHeaders.add("字段顺序不正确：" + String.join("；", orderErrors));
        }
        
        return missingHeaders;
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
            if (headerValue.contains("组别")) {
                mapping.groupIndex = cellIndex;
            } else if (headerValue.contains("品类")) {
                mapping.categoryIndex = cellIndex;
            } else if (headerValue.contains("DQE负责人") || headerValue.contains("负责人") || headerValue.contains("DQE")) {
                mapping.dqeResponsibleIndex = cellIndex;
            } else if (headerValue.contains("发生日期") || headerValue.contains("测试日期")) {
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
            } else if (headerValue.contains("责任部门")) {
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
            } else if (headerValue.contains("数据来源") || headerValue.contains("来源")) {
                mapping.dataSourceIndex = cellIndex;
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
        reviewResult.setGroup(getCellValueAsString(getCellSafely(row, mapping.groupIndex)));
        reviewResult.setCategory(getCellValueAsString(getCellSafely(row, mapping.categoryIndex)));
        reviewResult.setDqeResponsible(getCellValueAsString(getCellSafely(row, mapping.dqeResponsibleIndex)));
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
        reviewResult.setDataSource(getCellValueAsString(getCellSafely(row, mapping.dataSourceIndex)));
        
        // 检查必填字段（版本字段、组别、品类、DQE负责人、数据来源已改为可选）
        StringBuilder missingFields = new StringBuilder();
        
        // 组别、品类、DQE负责人、数据来源已改为可选字段，不再进行检查
        if (reviewResult.getMajorCode() == null || reviewResult.getMajorCode().trim().isEmpty()) {
            missingFields.append("大编码 ");
        }
        if (reviewResult.getMinorCode() == null || reviewResult.getMinorCode().trim().isEmpty()) {
            missingFields.append("小编码 ");
        }
        if (reviewResult.getProjectPhase() == null || reviewResult.getProjectPhase().trim().isEmpty()) {
            missingFields.append("项目阶段 ");
        }
        // 版本字段已改为可选，不再检查
        if (reviewResult.getSupplier() == null || reviewResult.getSupplier().trim().isEmpty()) {
            missingFields.append("供应商 ");
        }
        if (reviewResult.getProblemPoint() == null || reviewResult.getProblemPoint().trim().isEmpty()) {
            missingFields.append("问题点 ");
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
                    // 尝试多种日期格式（包括月份和日期可以是1位或2位数字的格式）
                    String[] patterns = {
                        "yyyy-MM-dd", "yyyy-M-d", "yyyy/MM/dd", "yyyy/M/d",
                        "MM/dd/yyyy", "M/d/yyyy", "dd/MM/yyyy", "d/M/yyyy",
                        "yyyy年MM月dd日", "yyyy年M月d日", "MM月dd日", "M月d日",
                        "yyyy-MM", "yyyy-M", "yyyy"
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
                    // 尝试多种日期时间格式（包括月份和日期可以是1位或2位数字的格式）
                    String[] patterns = {
                        "yyyy-MM-dd HH:mm:ss", "yyyy-M-d HH:mm:ss",
                        "yyyy/MM/dd HH:mm:ss", "yyyy/M/d HH:mm:ss",
                        "MM/dd/yyyy HH:mm:ss", "M/d/yyyy HH:mm:ss",
                        "yyyy-MM-dd", "yyyy-M-d",
                        "yyyy/MM/dd", "yyyy/M/d",
                        "MM/dd/yyyy", "M/d/yyyy",
                        "yyyy年MM月dd日 HH:mm:ss", "yyyy年M月d日 HH:mm:ss",
                        "yyyy年MM月dd日", "yyyy年M月d日"
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
        int groupIndex = -1;
        int categoryIndex = -1;
        int dqeResponsibleIndex = -1;
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
        int dataSourceIndex = -1;
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

    /**
     * 根据筛选条件删除评审结果
     * @param filters 筛选条件
     * @return 删除的记录数
     */
    public int deleteByFilters(Map<String, Object> filters) {
        try {
            if (filters == null || filters.isEmpty()) {
                return 0;
            }
            
            // 调用DAO层根据条件删除
            int deletedCount = reviewResultsDao.deleteByFilters(filters);
            
            System.out.println("根据筛选条件删除评审结果: 实际删除 " + deletedCount + " 条");
            
            return deletedCount;
        } catch (Exception e) {
            System.err.println("根据筛选条件删除评审结果失败: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 根据筛选条件查询评审结果
     * @param filters 筛选条件
     * @return 评审结果列表
     */
    public List<ReviewResults> findByFilters(Map<String, Object> filters) {
        try {
            if (filters == null || filters.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 调用DAO层根据条件查询
            List<ReviewResults> results = reviewResultsDao.findByFilters(filters);
            
            System.out.println("根据筛选条件查询评审结果: 找到 " + results.size() + " 条");
            
            return results;
        } catch (Exception e) {
            System.err.println("根据筛选条件查询评审结果失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 导出评审结果到Excel
     * @param dataList 要导出的数据列表
     * @return Excel文件的字节数组
     */
    public byte[] exportToExcel(List<ReviewResults> dataList) {
        try {
            if (dataList == null || dataList.isEmpty()) {
                return new byte[0];
            }
            
            // 将ReviewResults对象转换为Map格式，与现有导出逻辑保持一致
            List<Map<String, Object>> data = new ArrayList<>();
            for (ReviewResults reviewResult : dataList) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", reviewResult.getId());
                item.put("testDate", reviewResult.getTestDate());
                item.put("majorCode", reviewResult.getMajorCode());
                item.put("minorCode", reviewResult.getMinorCode());
                item.put("projectPhase", reviewResult.getProjectPhase());
                item.put("version", reviewResult.getVersion());
                item.put("problemProcess", reviewResult.getProblemProcess());
                item.put("problemLevel", reviewResult.getProblemLevel());
                item.put("developmentMethod", reviewResult.getDevelopmentMethod());
                item.put("supplier", reviewResult.getSupplier());
                item.put("solutionProvider", reviewResult.getSolutionProvider());
                item.put("problemPoint", reviewResult.getProblemPoint());
                item.put("problemReason", reviewResult.getProblemReason());
                item.put("improvementMeasures", reviewResult.getImprovementMeasures());
                item.put("isPreventable", reviewResult.getIsPreventable());
                item.put("responsibleDepartment", reviewResult.getResponsibleDepartment());
                item.put("plannedCompletionTime", reviewResult.getPlannedCompletionTime());
                item.put("actualCompletionTime", reviewResult.getActualCompletionTime());
                item.put("delayDays", reviewResult.getDelayDays());
                item.put("problemStatus", reviewResult.getProblemStatus());
                item.put("problemTag1", reviewResult.getProblemTag1());
                item.put("problemTag2", reviewResult.getProblemTag2());
                item.put("preventionNotes", reviewResult.getPreventionNotes());
                data.add(item);
            }
            
            // 调用现有的Excel创建方法
            return createReviewResultsExcelFile(data);
            
        } catch (Exception e) {
            System.err.println("导出评审结果到Excel失败: " + e.getMessage());
            e.printStackTrace();
            return new byte[0];
        }
    }

    /**
     * 创建评审结果Excel文件
     * @param data 数据列表
     * @return Excel文件的字节数组
     */
    private byte[] createReviewResultsExcelFile(List<Map<String, Object>> data) {
        try {
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
                        "序号", "发生日期", "大编码", "小编码", "项目阶段", "版本", "问题工序", "问题等级",
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
                return out.toByteArray();

            } finally {
                try { workbook.close(); } catch (IOException ignored) {}
            }
        } catch (Exception e) {
            System.err.println("创建评审结果Excel文件失败: " + e.getMessage());
            e.printStackTrace();
            return new byte[0];
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
