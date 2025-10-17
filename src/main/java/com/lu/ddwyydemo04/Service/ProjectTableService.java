package com.lu.ddwyydemo04.Service;

import com.lu.ddwyydemo04.dao.ProjectTableMapper;
import com.lu.ddwyydemo04.pojo.ProjectTable;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 项目表格服务层
 */
@Service
public class ProjectTableService {

    @Autowired
    private ProjectTableMapper projectTableMapper;

    // 中文列名到数据库字段的映射关系（基于数据库注释）
    private static final Map<String, String> COLUMN_MAPPING = new HashMap<>();
    
    static {
        COLUMN_MAPPING.put("序号", "id");
        COLUMN_MAPPING.put("收样时间", "sampleReceiptTime");
        COLUMN_MAPPING.put("送样周别", "sampleWeek");
        COLUMN_MAPPING.put("供应商", "supplier");
        COLUMN_MAPPING.put("送样次数", "sampleCount");
        COLUMN_MAPPING.put("样品型号", "sampleModel");
        COLUMN_MAPPING.put("小编码", "smallCode");
        COLUMN_MAPPING.put("样品名称", "sampleName");
        COLUMN_MAPPING.put("品类细分", "categoryDetail");
        COLUMN_MAPPING.put("版本号", "versionNumber");
        COLUMN_MAPPING.put("测试编号", "testNumber");
        COLUMN_MAPPING.put("需求耗时", "requiredDuration");
        COLUMN_MAPPING.put("总数", "totalQuantity");
        COLUMN_MAPPING.put("电气兼容性_样品数量", "electricalCompatibilityQuantity");
        COLUMN_MAPPING.put("高频_样品数量", "highFrequencyQuantity");
        COLUMN_MAPPING.put("传导&射频_样品数量", "conductionRfQuantity");
        COLUMN_MAPPING.put("可靠性_样品数量", "reliabilityQuantity");
        COLUMN_MAPPING.put("环保_样品数量", "environmentalQuantity");
        COLUMN_MAPPING.put("样品分类", "sampleCategory");
        COLUMN_MAPPING.put("样品性质", "sampleNature");
        COLUMN_MAPPING.put("项目属性", "projectAttribute");
        COLUMN_MAPPING.put("电气性能测试_计划开始时间", "electricalTestPlanStart");
        COLUMN_MAPPING.put("电气性能测试_计划完成时间", "electricalTestPlanEnd");
        COLUMN_MAPPING.put("电气性能测试_实际开始时间", "electricalTestActualStart");
        COLUMN_MAPPING.put("电气性能测试_实际完成时间", "electricalTestActualEnd");
        COLUMN_MAPPING.put("实际完成周", "actualCompletionWeek");
        COLUMN_MAPPING.put("电子工程师&DQE确认_开始时间", "engineerDqeConfirmStart");
        COLUMN_MAPPING.put("电子工程师&DQE确认_完成时间", "engineerDqeConfirmEnd");
        COLUMN_MAPPING.put("测试时长(天)", "testDurationDays");
        COLUMN_MAPPING.put("实际开始时间与收样时间之差(天)", "actualStartVsReceiptDays");
        COLUMN_MAPPING.put("实际开始时间与计划开始时间之差(天)", "actualStartVsPlanStartDays");
        COLUMN_MAPPING.put("计划总耗时(天)", "planTotalDurationDays");
        COLUMN_MAPPING.put("实际总耗时(天)", "actualTotalDurationDays");
        COLUMN_MAPPING.put("实际加班时数(天)", "actualOvertimeDays");
        COLUMN_MAPPING.put("问题确认总耗时(天)", "problemConfirmTotalDurationDays");
        COLUMN_MAPPING.put("问题确认加班时数(天)", "problemConfirmOvertimeDays");
        COLUMN_MAPPING.put("验证次数", "verificationCount");
        COLUMN_MAPPING.put("测试人", "tester");
        COLUMN_MAPPING.put("电子工程师", "electronicEngineer");
        COLUMN_MAPPING.put("项目工程师", "projectEngineer");
        COLUMN_MAPPING.put("DQE", "dqe");
        COLUMN_MAPPING.put("DQE品类细分", "dqeCategoryDetail");
        COLUMN_MAPPING.put("新分类", "newCategory");
        COLUMN_MAPPING.put("加急/同步", "urgentSync");
        COLUMN_MAPPING.put("是否在计划内", "isInPlan");
        COLUMN_MAPPING.put("送样周期", "sampleCycle");
        COLUMN_MAPPING.put("测试周期", "testCycle");
        COLUMN_MAPPING.put("排队周期", "queueCycle");
        COLUMN_MAPPING.put("判定结果", "judgmentResult");
        COLUMN_MAPPING.put("是否签样", "isSigned");
        COLUMN_MAPPING.put("内外贸区分", "domesticForeign");
        COLUMN_MAPPING.put("供应商前置", "supplierPreposition");
        COLUMN_MAPPING.put("送样备注", "sampleRemarks");
        COLUMN_MAPPING.put("问题定义", "problemDefinition");
        COLUMN_MAPPING.put("S类_问题数量", "sClassProblemCount");
        COLUMN_MAPPING.put("A类_问题数量", "aClassProblemCount");
        COLUMN_MAPPING.put("B类_问题数量", "bClassProblemCount");
        COLUMN_MAPPING.put("C类_问题数量", "cClassProblemCount");
        COLUMN_MAPPING.put("D类_问题数量", "dClassProblemCount");
        COLUMN_MAPPING.put("问题总数", "totalProblemCount");
        COLUMN_MAPPING.put("OPEN数量", "openCount");
        COLUMN_MAPPING.put("CLOSE数量", "closeCount");
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

        public ImportResult(boolean success, String message, int insertCount, int updateCount, int errorCount) {
            this.success = success;
            this.message = message;
            this.insertCount = insertCount;
            this.updateCount = updateCount;
            this.errorCount = errorCount;
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public int getInsertCount() { return insertCount; }
        public void setInsertCount(int insertCount) { this.insertCount = insertCount; }
        public int getUpdateCount() { return updateCount; }
        public void setUpdateCount(int updateCount) { this.updateCount = updateCount; }
        public int getErrorCount() { return errorCount; }
        public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
    }

    /**
     * 获取所有项目表格数据
     * @return 项目表格数据列表
     */
    public List<ProjectTable> getAllProjectTables() {
        return projectTableMapper.selectAll();
    }

    /**
     * 根据ID获取项目表格数据
     * @param id 主键ID
     * @return 项目表格数据
     */
    public ProjectTable getProjectTableById(Long id) {
        return projectTableMapper.selectById(id);
    }

    /**
     * 分页获取项目表格数据
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    public Map<String, Object> getProjectTablesByPage(int page, int size) {
        int offset = (page - 1) * size;
        List<ProjectTable> data = projectTableMapper.selectByPage(offset, size);
        int total = projectTableMapper.countAll();
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        
        return result;
    }

    /**
     * 根据条件分页获取项目表格数据
     * @param supplier 供应商
     * @param sampleModel 样品型号
     * @param sampleName 样品名称
     * @param categoryDetail 品类细分
     * @param tester 测试人
     * @param dqe DQE
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    public Map<String, Object> getProjectTablesByCondition(String supplier, String sampleModel, 
                                                          String sampleName, String categoryDetail,
                                                          String tester, String dqe, 
                                                          int page, int size) {
        int offset = (page - 1) * size;
        List<ProjectTable> data = projectTableMapper.selectByCondition(
                supplier, sampleModel, sampleName, categoryDetail, tester, dqe, offset, size);
        int total = projectTableMapper.countByCondition(
                supplier, sampleModel, sampleName, categoryDetail, tester, dqe);
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        
        return result;
    }

    /**
     * 保存项目表格数据
     * @param projectTable 项目表格数据
     * @return 保存结果
     */
    public boolean saveProjectTable(ProjectTable projectTable) {
        try {
            if (projectTable.getId() == null) {
                // 新增
                return projectTableMapper.insert(projectTable) > 0;
            } else {
                // 更新
                return projectTableMapper.update(projectTable) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除项目表格数据
     * @param id 主键ID
     * @return 删除结果
     */
    public boolean deleteProjectTable(Long id) {
        try {
            return projectTableMapper.deleteById(id) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 批量保存项目表格数据
     * @param projectTables 项目表格数据列表
     * @return 保存结果
     */
    public boolean batchSaveProjectTables(List<ProjectTable> projectTables) {
        try {
            for (ProjectTable projectTable : projectTables) {
                if (projectTable.getId() == null) {
                    projectTableMapper.insert(projectTable);
                } else {
                    projectTableMapper.update(projectTable);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 从Excel文件导入项目表格数据
     * @param file Excel文件
     * @param username 用户名
     * @return 导入结果
     */
    public ImportResult importProjectTableFromExcel(MultipartFile file, String username) {
        int insertCount = 0;
        int updateCount = 0;
        int errorCount = 0;
        List<String> errorMessages = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            if (sheet.getLastRowNum() < 1) {
                return new ImportResult(false, "Excel文件为空或没有数据行", 0, 0, 0);
            }

            // 读取第一行作为列名
            Row headerRow = sheet.getRow(0);
            Map<Integer, String> columnIndexToField = new HashMap<>();
            
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String columnName = getCellValueAsString(cell).trim();
                    String fieldName = COLUMN_MAPPING.get(columnName);
                    if (fieldName != null) {
                        columnIndexToField.put(i, fieldName);
                        System.out.println("映射列: " + columnName + " -> " + fieldName);
                    } else {
                        System.out.println("未找到映射的列: " + columnName);
                    }
                }
            }

            if (columnIndexToField.isEmpty()) {
                return new ImportResult(false, "未找到有效的列名映射，请检查Excel文件的第一行是否包含正确的中文列名", 0, 0, 0);
            }

            System.out.println("找到 " + columnIndexToField.size() + " 个有效列映射");

            // 处理数据行
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                try {
                    ProjectTable projectTable = new ProjectTable();
                    
                    // 根据列名映射设置字段值
                    for (Map.Entry<Integer, String> entry : columnIndexToField.entrySet()) {
                        int cellIndex = entry.getKey();
                        String fieldName = entry.getValue();
                        Cell cell = row.getCell(cellIndex);
                        
                        String cellValue = getCellValueAsString(cell);
                        System.out.println("第" + (rowIndex + 1) + "行，列" + cellIndex + "，字段" + fieldName + "，原始值: '" + cellValue + "'");
                        
                        setFieldValue(projectTable, fieldName, cell);
                        
                        // 特别检查整数字段
                        if (isIntegerField(fieldName)) {
                            Object fieldValue = getFieldValue(projectTable, fieldName);
                            System.out.println("  -> 解析后整数值: " + fieldValue);
                        }
                        
                        // 特别检查周期字段
                        if (isCycleField(fieldName)) {
                            Object fieldValue = getFieldValue(projectTable, fieldName);
                            System.out.println("  -> 解析后周期值: " + fieldValue);
                        }
                    }

                    // 设置创建时间
                    projectTable.setCreateTime(LocalDateTime.now());
                    projectTable.setUpdateTime(LocalDateTime.now());

                    // 直接插入数据，所有字段都可以为空
                    projectTableMapper.insert(projectTable);
                    insertCount++;
                    System.out.println("成功插入第" + (rowIndex + 1) + "行数据");

                } catch (Exception e) {
                    errorCount++;
                    errorMessages.add("第" + (rowIndex + 1) + "行处理失败：" + e.getMessage());
                    System.err.println("处理第" + (rowIndex + 1) + "行时出错: " + e.getMessage());
                }
            }

            String message = String.format("导入完成！成功插入%d条，更新%d条，失败%d条", insertCount, updateCount, errorCount);
            if (!errorMessages.isEmpty()) {
                message += "\n错误详情：" + String.join("; ", errorMessages);
            }

            return new ImportResult(true, message, insertCount, updateCount, errorCount);

        } catch (IOException e) {
            return new ImportResult(false, "读取Excel文件失败：" + e.getMessage(), 0, 0, 0);
        } catch (Exception e) {
            return new ImportResult(false, "导入过程中发生错误：" + e.getMessage(), 0, 0, 0);
        }
    }

    /**
     * 获取单元格值作为字符串
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * 根据字段名设置ProjectTable对象的字段值
     * 原封不动地存储Excel中的值，不进行格式转换
     */
    private void setFieldValue(ProjectTable projectTable, String fieldName, Cell cell) {
        String cellValue = getCellValueAsString(cell);
        
        // 如果单元格为空，直接返回，保持字段为null
        if (cellValue == null || cellValue.trim().isEmpty()) {
            return;
        }

        // 原封不动地存储所有值，根据字段类型进行简单解析
        switch (fieldName) {
            // 整数字段
            case "sampleCount":
                projectTable.setSampleCount(parseInteger(cellValue));
                break;
            case "totalQuantity":
                projectTable.setTotalQuantity(parseInteger(cellValue));
                break;
            case "electricalCompatibilityQuantity":
                projectTable.setElectricalCompatibilityQuantity(parseInteger(cellValue));
                break;
            case "highFrequencyQuantity":
                projectTable.setHighFrequencyQuantity(parseInteger(cellValue));
                break;
            case "conductionRfQuantity":
                projectTable.setConductionRfQuantity(parseInteger(cellValue));
                break;
            case "reliabilityQuantity":
                projectTable.setReliabilityQuantity(parseInteger(cellValue));
                break;
            case "environmentalQuantity":
                projectTable.setEnvironmentalQuantity(parseInteger(cellValue));
                break;
            case "verificationCount":
                projectTable.setVerificationCount(parseInteger(cellValue));
                break;
            case "sClassProblemCount":
                projectTable.setsClassProblemCount(parseInteger(cellValue));
                break;
            case "aClassProblemCount":
                projectTable.setaClassProblemCount(parseInteger(cellValue));
                break;
            case "bClassProblemCount":
                projectTable.setbClassProblemCount(parseInteger(cellValue));
                break;
            case "cClassProblemCount":
                projectTable.setcClassProblemCount(parseInteger(cellValue));
                break;
            case "dClassProblemCount":
                projectTable.setdClassProblemCount(parseInteger(cellValue));
                break;
            case "totalProblemCount":
                projectTable.setTotalProblemCount(parseInteger(cellValue));
                break;
            case "openCount":
                projectTable.setOpenCount(parseInteger(cellValue));
                break;
            case "closeCount":
                projectTable.setCloseCount(parseInteger(cellValue));
                break;
            
            // 小数字段
            case "requiredDuration":
                projectTable.setRequiredDuration(parseBigDecimal(cellValue));
                break;
            case "testDurationDays":
                projectTable.setTestDurationDays(parseBigDecimal(cellValue));
                break;
            case "actualStartVsReceiptDays":
                projectTable.setActualStartVsReceiptDays(parseBigDecimal(cellValue));
                break;
            case "actualStartVsPlanStartDays":
                projectTable.setActualStartVsPlanStartDays(parseBigDecimal(cellValue));
                break;
            case "planTotalDurationDays":
                projectTable.setPlanTotalDurationDays(parseBigDecimal(cellValue));
                break;
            case "actualTotalDurationDays":
                projectTable.setActualTotalDurationDays(parseBigDecimal(cellValue));
                break;
            case "actualOvertimeDays":
                projectTable.setActualOvertimeDays(parseBigDecimal(cellValue));
                break;
            case "problemConfirmTotalDurationDays":
                projectTable.setProblemConfirmTotalDurationDays(parseBigDecimal(cellValue));
                break;
            case "problemConfirmOvertimeDays":
                projectTable.setProblemConfirmOvertimeDays(parseBigDecimal(cellValue));
                break;
            case "sampleCycle":
                projectTable.setSampleCycle(parseBigDecimal(cellValue));
                break;
            case "testCycle":
                projectTable.setTestCycle(parseBigDecimal(cellValue));
                break;
            case "queueCycle":
                projectTable.setQueueCycle(parseBigDecimal(cellValue));
                break;
            
            // 日期时间字段 - 优先使用Excel的日期格式，否则尝试解析字符串
            case "sampleReceiptTime":
                if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                    projectTable.setSampleReceiptTime(cell.getDateCellValue().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
                } else {
                    projectTable.setSampleReceiptTime(parseDateTime(cellValue));
                }
                break;
            case "electricalTestPlanStart":
                if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                    projectTable.setElectricalTestPlanStart(cell.getDateCellValue().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
                } else {
                    projectTable.setElectricalTestPlanStart(parseDateTime(cellValue));
                }
                break;
            case "electricalTestPlanEnd":
                if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                    projectTable.setElectricalTestPlanEnd(cell.getDateCellValue().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
                } else {
                    projectTable.setElectricalTestPlanEnd(parseDateTime(cellValue));
                }
                break;
            case "electricalTestActualStart":
                if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                    projectTable.setElectricalTestActualStart(cell.getDateCellValue().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
                } else {
                    projectTable.setElectricalTestActualStart(parseDateTime(cellValue));
                }
                break;
            case "electricalTestActualEnd":
                if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                    projectTable.setElectricalTestActualEnd(cell.getDateCellValue().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
                } else {
                    projectTable.setElectricalTestActualEnd(parseDateTime(cellValue));
                }
                break;
            case "engineerDqeConfirmStart":
                if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                    projectTable.setEngineerDqeConfirmStart(cell.getDateCellValue().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
                } else {
                    projectTable.setEngineerDqeConfirmStart(parseDateTime(cellValue));
                }
                break;
            case "engineerDqeConfirmEnd":
                if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                    projectTable.setEngineerDqeConfirmEnd(cell.getDateCellValue().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
                } else {
                    projectTable.setEngineerDqeConfirmEnd(parseDateTime(cellValue));
                }
                break;
            
            // 文本字段 - 直接存储
            case "sampleWeek":
                projectTable.setSampleWeek(cellValue);
                break;
            case "supplier":
                projectTable.setSupplier(cellValue);
                break;
            case "sampleModel":
                projectTable.setSampleModel(cellValue);
                break;
            case "smallCode":
                projectTable.setSmallCode(cellValue);
                break;
            case "sampleName":
                projectTable.setSampleName(cellValue);
                break;
            case "categoryDetail":
                projectTable.setCategoryDetail(cellValue);
                break;
            case "versionNumber":
                projectTable.setVersionNumber(cellValue);
                break;
            case "testNumber":
                projectTable.setTestNumber(cellValue);
                break;
            case "actualCompletionWeek":
                projectTable.setActualCompletionWeek(cellValue);
                break;
            case "sampleCategory":
                projectTable.setSampleCategory(cellValue);
                break;
            case "sampleNature":
                projectTable.setSampleNature(cellValue);
                break;
            case "projectAttribute":
                projectTable.setProjectAttribute(cellValue);
                break;
            case "tester":
                projectTable.setTester(cellValue);
                break;
            case "electronicEngineer":
                projectTable.setElectronicEngineer(cellValue);
                break;
            case "projectEngineer":
                projectTable.setProjectEngineer(cellValue);
                break;
            case "dqe":
                projectTable.setDqe(cellValue);
                break;
            case "dqeCategoryDetail":
                projectTable.setDqeCategoryDetail(cellValue);
                break;
            case "newCategory":
                projectTable.setNewCategory(cellValue);
                break;
            case "urgentSync":
                projectTable.setUrgentSync(cellValue);
                break;
            case "isInPlan":
                projectTable.setIsInPlan(cellValue);
                break;
            case "judgmentResult":
                projectTable.setJudgmentResult(cellValue);
                break;
            case "isSigned":
                projectTable.setIsSigned(cellValue);
                break;
            case "domesticForeign":
                projectTable.setDomesticForeign(cellValue);
                break;
            case "supplierPreposition":
                projectTable.setSupplierPreposition(cellValue);
                break;
            case "sampleRemarks":
                projectTable.setSampleRemarks(cellValue);
                break;
            case "problemDefinition":
                projectTable.setProblemDefinition(cellValue);
                break;
        }
    }

    /**
     * 解析整数，失败时返回null
     */
    private Integer parseInteger(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            
            // 去除所有空格和特殊字符，只保留数字
            String cleanValue = value.trim().replaceAll("[^0-9.-]", "");
            
            // 如果清理后为空，返回null
            if (cleanValue.isEmpty()) {
                return null;
            }
            
            // 如果是小数，转换为整数
            if (cleanValue.contains(".")) {
                double doubleValue = Double.parseDouble(cleanValue);
                return (int) Math.round(doubleValue);
            }
            
            return Integer.parseInt(cleanValue);
        } catch (NumberFormatException e) {
            System.err.println("解析整数失败: " + value + ", 错误: " + e.getMessage());
            return null;
        }
    }

    /**
     * 解析小数，失败时返回null
     */
    private BigDecimal parseBigDecimal(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            
            // 去除所有空格和特殊字符，只保留数字和小数点
            String cleanValue = value.trim().replaceAll("[^0-9.-]", "");
            
            // 如果清理后为空，返回null
            if (cleanValue.isEmpty()) {
                return null;
            }
            
            return new BigDecimal(cleanValue);
        } catch (NumberFormatException e) {
            System.err.println("解析小数失败: " + value + ", 错误: " + e.getMessage());
            return null;
        }
    }

    /**
     * 检查是否为整数字段
     */
    private boolean isIntegerField(String fieldName) {
        return fieldName.equals("id") ||
               fieldName.equals("sampleCount") ||
               fieldName.equals("totalQuantity") ||
               fieldName.equals("electricalCompatibilityQuantity") ||
               fieldName.equals("highFrequencyQuantity") ||
               fieldName.equals("conductionRfQuantity") ||
               fieldName.equals("reliabilityQuantity") ||
               fieldName.equals("environmentalQuantity") ||
               fieldName.equals("verificationCount") ||
               fieldName.equals("sClassProblemCount") ||
               fieldName.equals("aClassProblemCount") ||
               fieldName.equals("bClassProblemCount") ||
               fieldName.equals("cClassProblemCount") ||
               fieldName.equals("dClassProblemCount") ||
               fieldName.equals("totalProblemCount") ||
               fieldName.equals("openCount") ||
               fieldName.equals("closeCount");
    }

    /**
     * 检查是否为周期字段
     */
    private boolean isCycleField(String fieldName) {
        return fieldName.equals("sampleCycle") ||
               fieldName.equals("testCycle") ||
               fieldName.equals("queueCycle");
    }

    /**
     * 获取字段值（用于调试）
     */
    private Object getFieldValue(ProjectTable projectTable, String fieldName) {
        switch (fieldName) {
            case "id": return projectTable.getId();
            case "sampleCount": return projectTable.getSampleCount();
            case "totalQuantity": return projectTable.getTotalQuantity();
            case "electricalCompatibilityQuantity": return projectTable.getElectricalCompatibilityQuantity();
            case "highFrequencyQuantity": return projectTable.getHighFrequencyQuantity();
            case "conductionRfQuantity": return projectTable.getConductionRfQuantity();
            case "reliabilityQuantity": return projectTable.getReliabilityQuantity();
            case "environmentalQuantity": return projectTable.getEnvironmentalQuantity();
            case "verificationCount": return projectTable.getVerificationCount();
            case "sClassProblemCount": return projectTable.getsClassProblemCount();
            case "aClassProblemCount": return projectTable.getaClassProblemCount();
            case "bClassProblemCount": return projectTable.getbClassProblemCount();
            case "cClassProblemCount": return projectTable.getcClassProblemCount();
            case "dClassProblemCount": return projectTable.getdClassProblemCount();
            case "totalProblemCount": return projectTable.getTotalProblemCount();
            case "openCount": return projectTable.getOpenCount();
            case "closeCount": return projectTable.getCloseCount();
            case "sampleCycle": return projectTable.getSampleCycle();
            case "testCycle": return projectTable.getTestCycle();
            case "queueCycle": return projectTable.getQueueCycle();
            default: return null;
        }
    }

    /**
     * 解析日期时间，失败时返回null
     */
    private LocalDateTime parseDateTime(String value) {
        try {
            String trimmedValue = value.trim();
            
            // 格式：2025/1/15 13:36:00
            if (trimmedValue.contains("/") && trimmedValue.contains(":")) {
                // 将 2025/1/15 13:36:00 转换为 2025-01-15T13:36:00
                String[] parts = trimmedValue.split(" ");
                if (parts.length == 2) {
                    String datePart = parts[0]; // 2025/1/15
                    String timePart = parts[1]; // 13:36:00
                    
                    // 处理日期部分，确保月份和日期是两位数
                    String[] dateParts = datePart.split("/");
                    if (dateParts.length == 3) {
                        String year = dateParts[0];
                        String month = dateParts[1].length() == 1 ? "0" + dateParts[1] : dateParts[1];
                        String day = dateParts[2].length() == 1 ? "0" + dateParts[2] : dateParts[2];
                        
                        String formattedDate = year + "-" + month + "-" + day + "T" + timePart;
                        return LocalDateTime.parse(formattedDate);
                    }
                }
            }
            // 格式：2024-01-15 09:00:00
            else if (trimmedValue.contains("-") && trimmedValue.contains(":")) {
                return LocalDateTime.parse(trimmedValue.replace(" ", "T"));
            }
        } catch (Exception e) {
            System.err.println("解析日期时间失败: " + value + ", 错误: " + e.getMessage());
        }
        return null;
    }
}
