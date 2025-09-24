package com.lu.ddwyydemo04.Service;

import com.lu.ddwyydemo04.dao.NewProductProgressDao;
import com.lu.ddwyydemo04.pojo.NewProductProgress;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 新品进度管理服务层
 */
@Service
public class NewProductProgressService {

    @Autowired
    private NewProductProgressDao newProductProgressDao;

    @Value("${file.storage.imagepath}")
    private String imagePath;

    /**
     * 获取所有新品进度数据
     * @return 新品进度列表
     */
    public List<NewProductProgress> getAllNewProductProgress() {
        List<NewProductProgress> results = newProductProgressDao.findAll();
        System.out.println("Service层获取到的数据量: " + (results != null ? results.size() : 0));
        if (results != null && !results.isEmpty()) {
            System.out.println("第一条数据: " + results.get(0).getProductName() + " - " + results.get(0).getModel());
        }
        return results;
    }

    /**
     * 根据ID获取新品进度
     * @param id 新品进度ID
     * @return 新品进度对象
     */
    public NewProductProgress getNewProductProgressById(Long id) {
        return newProductProgressDao.findById(id);
    }

    /**
     * 插入新品进度
     * @param newProductProgress 新品进度对象
     * @return 插入结果
     */
    public int insertNewProductProgress(NewProductProgress newProductProgress) {
        return newProductProgressDao.insert(newProductProgress);
    }

    /**
     * 更新新品进度
     * @param newProductProgress 新品进度对象
     * @return 更新结果
     */
    public boolean updateNewProductProgress(NewProductProgress newProductProgress) {
        return newProductProgressDao.update(newProductProgress) > 0;
    }

    /**
     * 根据ID列表批量删除新品进度
     * @param ids ID列表
     * @return 删除数量
     */
    public int deleteNewProductProgressByIds(List<Long> ids) {
        return newProductProgressDao.deleteByIds(ids);
    }

    /**
     * 批量插入新品进度数据（支持覆盖更新）
     * @param newProductProgressList 新品进度列表
     * @param username 用户名
     * @return 处理结果（包含插入和更新数量）
     */
    private BatchProcessResult batchInsertNewProductProgress(List<NewProductProgress> newProductProgressList, String username) {
        int insertCount = 0;
        int updateCount = 0;
        
        for (NewProductProgress newProductProgress : newProductProgressList) {
            try {
                // 检查是否存在相同的产品三级类目、型号、SKU
                NewProductProgress existingRecord = newProductProgressDao.findByCategoryModelSku(
                    newProductProgress.getProductCategoryLevel3(),
                    newProductProgress.getModel(),
                    newProductProgress.getSku()
                );
                
                if (existingRecord != null) {
                    // 存在相同记录，进行覆盖更新
                    System.out.println("=== 发现重复记录，进行覆盖更新 ===");
                    System.out.println("产品三级类目: " + newProductProgress.getProductCategoryLevel3());
                    System.out.println("型号: " + newProductProgress.getModel());
                    System.out.println("SKU: " + newProductProgress.getSku());
                    System.out.println("原记录ID: " + existingRecord.getId());
                    System.out.println("原记录创建时间: " + existingRecord.getCreateTime());
                    System.out.println("新记录产品名称: " + newProductProgress.getProductName());
                    System.out.println("新记录阶段: " + newProductProgress.getStage());
                    System.out.println("新记录状态: " + newProductProgress.getStatus());
                    System.out.println("=====================================");
                    
                    // 设置原记录的ID，保持ID不变
                    newProductProgress.setId(existingRecord.getId());
                    // 保持原记录的创建时间和创建人
                    newProductProgress.setCreateTime(existingRecord.getCreateTime());
                    newProductProgress.setCreateBy(existingRecord.getCreateBy());
                    // 更新修改时间和修改人（使用传入的username）
                    newProductProgress.setUpdateTime(java.time.LocalDateTime.now());
                    newProductProgress.setUpdateBy(username);
                    
                    int resultCount = newProductProgressDao.update(newProductProgress);
                    if (resultCount > 0) {
                        updateCount++;
                        System.out.println("成功更新记录，ID: " + existingRecord.getId());
                    }
                } else {
                    // 不存在相同记录，进行插入
                    // 设置创建者和创建时间
                    newProductProgress.setCreateBy(username);
                    newProductProgress.setCreateTime(java.time.LocalDateTime.now());
                    newProductProgress.setUpdateBy(username);
                    newProductProgress.setUpdateTime(java.time.LocalDateTime.now());
                    
                    int resultCount = newProductProgressDao.insert(newProductProgress);
                    if (resultCount > 0) {
                        insertCount++;
                    }
                }
            } catch (Exception e) {
                // 记录错误但继续处理
                System.err.println("处理记录失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return new BatchProcessResult(insertCount, updateCount);
    }
    
    /**
     * 批量处理结果类
     */
    private static class BatchProcessResult {
        private final int insertCount;
        private final int updateCount;
        
        public BatchProcessResult(int insertCount, int updateCount) {
            this.insertCount = insertCount;
            this.updateCount = updateCount;
        }
        
        public int getInsertCount() {
            return insertCount;
        }
        
        public int getUpdateCount() {
            return updateCount;
        }
    }

    /**
     * 导入Excel文件并解析新品进度数据
     * @param file Excel文件
     * @param username 用户名
     * @return 导入结果
     */
    public ImportResult importNewProductProgressFromExcel(MultipartFile file, String username) {
        ImportResult result = new ImportResult();
        Workbook workbook = null;
        
        try {
            // 检查文件大小，限制在50MB以内
            if (file.getSize() > 100 * 1024 * 1024) {
                result.setSuccess(false);
                result.setMessage("文件过大，请上传小于50MB的文件");
                return result;
            }
            
            // 创建工作簿
            workbook = new XSSFWorkbook(file.getInputStream());
            
            // 检查是否有第二个工作表
            if (workbook.getNumberOfSheets() < 2) {
                result.setSuccess(false);
                result.setMessage("Excel文件必须包含至少2个工作表，请检查文件格式");
                return result;
            }
            
            // 获取第二个工作表（索引为1）
            Sheet sheet = workbook.getSheetAt(1);
            
            // 获取绘图对象用于提取图片
            Drawing<?> drawing = sheet.getDrawingPatriarch();
            
            // 检查第七行是否存在（索引为6）
            if (sheet.getLastRowNum() < 6) {
                result.setSuccess(false);
                result.setMessage("第二个工作表的第七行不存在，请检查文件格式");
                return result;
            }
            
            // 获取第七行作为字段标题行（索引为6）
            Row headerRow = sheet.getRow(6);
            if (headerRow == null) {
                result.setSuccess(false);
                result.setMessage("第七行为空，无法解析字段标题");
                return result;
            }
            
            // 解析字段标题，建立列索引映射
            Map<String, Integer> columnMapping = parseHeaderRow(headerRow);
            
            List<NewProductProgress> newProductProgressList = new ArrayList<>();
            int errorCount = 0;
            List<String> errorList = new ArrayList<>();
            int maxErrors = 100; // 限制错误消息数量，避免内存溢出
            Set<String> usedSkus = new HashSet<>(); // 用于跟踪已使用的SKU，避免重复
            
            // 从第八行开始读取数据（索引为7，跳过标题行）
            // 限制处理行数，避免处理时间过长
            int maxRows = 10000; // 最多处理10000行
            int lastRowIndex = Math.min(sheet.getLastRowNum(), 7 + maxRows - 1);
            
            System.out.println("开始解析数据，从第8行到第" + (lastRowIndex + 1) + "行，共" + (lastRowIndex - 7 + 1) + "行");
            
            for (int i = 7; i <= lastRowIndex; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                 try {
                     // 先检查行是否为空（以产品三级类目字段为准）
                     if (isRowEmpty(row, columnMapping)) {
                         if (i <= 20) { // 只打印前20行的调试信息
                             System.out.println("第" + (i + 1) + "行：产品三级类目为空，跳过");
                         }
                         continue; // 跳过空行
                     }
                     
                     NewProductProgress newProductProgress = parseRowToNewProductProgressWithMapping(row, columnMapping, usedSkus, i);
                     if (newProductProgress != null && isValidProductProgress(newProductProgress)) {
                         // 提取图片并保存
                         String imagePath = extractAndSaveImageFromRow(row, drawing, columnMapping, i);
                         if (imagePath != null && !imagePath.isEmpty()) {
                             newProductProgress.setImageUrl(imagePath);
                         }
                         
                         newProductProgressList.add(newProductProgress);
                         usedSkus.add(newProductProgress.getSku()); // 记录已使用的SKU
                         if (i <= 20) { // 只打印前20行的调试信息
                             System.out.println("第" + (i + 1) + "行：成功解析 - 产品三级类目: " + 
                                 newProductProgress.getProductCategoryLevel3() + 
                                 ", 产品名称: " + newProductProgress.getProductName() + 
                                 ", SKU: " + newProductProgress.getSku() +
                                 (imagePath != null ? ", 图片: " + imagePath : ""));
                         }
                     } else {
                         if (i <= 20) { // 只打印前20行的调试信息
                             System.out.println("第" + (i + 1) + "行：数据验证失败，跳过");
                         }
                     }
                 } catch (Exception e) {
                    errorCount++;
                    if (errorList.size() < maxErrors) {
                        errorList.add("第" + (i + 1) + "行解析失败: " + e.getMessage());
                    }
                }
                
                // 每处理1000行输出一次日志，避免长时间无响应
                if ((i - 7) % 1000 == 0 && i > 7) {
                    System.out.println("已处理 " + (i - 7) + " 行数据...");
                }
            }
            
            // 批量处理数据库，分批处理避免内存问题（支持插入和更新）
            int insertCount = 0;
            int updateCount = 0;
            int batchSize = 500; // 增加批量大小到500条记录
            
            System.out.println("开始批量处理数据，共 " + newProductProgressList.size() + " 条记录...");
            
            for (int i = 0; i < newProductProgressList.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, newProductProgressList.size());
                List<NewProductProgress> batch = newProductProgressList.subList(i, endIndex);
                
                try {
                    // 批量处理（插入或更新）
                    BatchProcessResult batchResult = batchInsertNewProductProgress(batch, username);
                    insertCount += batchResult.getInsertCount();
                    updateCount += batchResult.getUpdateCount();
                    
                    System.out.println("已处理 " + (insertCount + updateCount) + "/" + newProductProgressList.size() + " 条记录（插入: " + insertCount + ", 更新: " + updateCount + "）...");
                    
                } catch (Exception e) {
                    // 如果批量处理失败，尝试逐条处理
                    System.out.println("批量处理失败，尝试逐条处理: " + e.getMessage());
                    for (NewProductProgress newProductProgress : batch) {
                        try {
                            // 检查是否存在相同记录
                            NewProductProgress existingRecord = newProductProgressDao.findByCategoryModelSku(
                                newProductProgress.getProductCategoryLevel3(),
                                newProductProgress.getModel(),
                                newProductProgress.getSku()
                            );
                            
                            if (existingRecord != null) {
                                // 更新现有记录
                                newProductProgress.setId(existingRecord.getId());
                                newProductProgress.setCreateTime(existingRecord.getCreateTime());
                                newProductProgress.setCreateBy(existingRecord.getCreateBy());
                                newProductProgress.setUpdateTime(java.time.LocalDateTime.now());
                                newProductProgress.setUpdateBy(username);
                                
                                int resultCount = newProductProgressDao.update(newProductProgress);
                                if (resultCount > 0) {
                                    updateCount++;
                                }
                            } else {
                                // 插入新记录
                                newProductProgress.setCreateBy(username);
                                newProductProgress.setCreateTime(java.time.LocalDateTime.now());
                                newProductProgress.setUpdateBy(username);
                                newProductProgress.setUpdateTime(java.time.LocalDateTime.now());
                                
                                int resultCount = newProductProgressDao.insert(newProductProgress);
                                if (resultCount > 0) {
                                    insertCount++;
                                }
                            }
                        } catch (Exception ex) {
                            errorCount++;
                            if (errorList.size() < maxErrors) {
                                errorList.add("处理失败: " + ex.getMessage());
                            }
                        }
                    }
                }
            }
            
            // 构建错误消息，限制长度
            StringBuilder errorMessage = new StringBuilder();
            if (errorCount > 0) {
                errorMessage.append("导入完成，成功插入").append(insertCount).append("条记录，更新").append(updateCount).append("条记录，失败").append(errorCount).append("条");
                if (!errorList.isEmpty()) {
                    errorMessage.append("。前").append(Math.min(errorList.size(), 10)).append("个错误：");
                    for (int i = 0; i < Math.min(errorList.size(), 10); i++) {
                        errorMessage.append(errorList.get(i)).append("; ");
                    }
                    if (errorList.size() > 10) {
                        errorMessage.append("...还有").append(errorList.size() - 10).append("个错误");
                    }
                }
            } else {
                errorMessage.append("导入完成，成功插入").append(insertCount).append("条记录，更新").append(updateCount).append("条记录");
            }
            
            result.setSuccess(true);
            result.setMessage(errorMessage.toString());
            result.setInsertCount(insertCount);
            result.setUpdateCount(updateCount);
            result.setErrorCount(errorCount);
            
        } catch (IOException e) {
            result.setSuccess(false);
            result.setMessage("文件读取失败: " + e.getMessage());
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("导入失败: " + e.getMessage());
        } finally {
            // 确保workbook被关闭
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    // 忽略关闭时的异常
                }
            }
        }
        
        return result;
    }

    /**
     * 解析第七行字段标题，建立列索引映射
     * @param headerRow 第七行（字段标题行）
     * @return 字段名到列索引的映射
     */
    private Map<String, Integer> parseHeaderRow(Row headerRow) {
        Map<String, Integer> columnMapping = new HashMap<>();
        
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String headerValue = getCellValueAsString(cell).trim();
                if (!headerValue.isEmpty()) {
                    columnMapping.put(headerValue, i);
                }
            }
        }
        
        return columnMapping;
    }

    /**
     * 根据字段映射解析Excel行数据为新品进度对象
     * @param row Excel行
     * @param columnMapping 字段名到列索引的映射
     * @param usedSkus 已使用的SKU集合
     * @param rowNum 行号
     * @return 新品进度对象
     */
    private NewProductProgress parseRowToNewProductProgressWithMapping(Row row, Map<String, Integer> columnMapping, Set<String> usedSkus, int rowNum) {
        NewProductProgress newProductProgress = new NewProductProgress();
        
        try {
            // 预获取所有需要的列索引，避免重复查找
            Integer productCategoryIndex = columnMapping.get("产品三级类目");
            Integer priorityIndex = columnMapping.get("优先级");
            Integer modelIndex = columnMapping.get("型号");
            Integer skuIndex = columnMapping.get("SKU");
            Integer stageIndex = columnMapping.get("阶段\n" +
                    "（节点管理）");
            Integer productNameIndex = columnMapping.get("产品名称\n" +
                    "（接口/功能信息）");
            Integer imageUrlIndex = columnMapping.get("图片");
            Integer projectStartTimeIndex = columnMapping.get("立项时间");
            Integer targetLaunchTimeIndex = columnMapping.get("目标上市时间");
            Integer displayTypeIndex = columnMapping.get("单显/双显/三显/四显");
            Integer productLevelIndex = columnMapping.get("产品等级");
            Integer primarySupplierIndex = columnMapping.get("一级供方");
            Integer leadDqeIndex = columnMapping.get("主导DQE");
            Integer electronicRdIndex = columnMapping.get("电子研发");
            Integer statusIndex = columnMapping.get("状态\n" +
                    "（只填ID/结构/电子设计中或功能样或大货样）");
            Integer designReviewProblemIndex = columnMapping.get("设计评审\n" +
                    "主要问题");
            Integer evtProblemIndex = columnMapping.get("EVT\n" +
                    "主要问题");
            Integer dvtProblemIndex = columnMapping.get("DVT\n" +
                    "主要问题");
            Integer mainProjectProgressIndex = columnMapping.get("项目进度主要事项\n" +
                    "（只填重要内容或一句话）");
            Integer mainQualityRisksIndex = columnMapping.get("质量主要风险\n" +
                    "（只填重要内容或一句话）");
            
             // 根据字段标题动态获取数据
             newProductProgress.setProductCategoryLevel3(getCellValueByIndex(row, productCategoryIndex));
             newProductProgress.setPriority(getCellValueByIndex(row, priorityIndex));
             newProductProgress.setModel(getCellValueByIndex(row, modelIndex));
             newProductProgress.setSku(validateAndGenerateSku(getCellValueByIndex(row, skuIndex), usedSkus, rowNum));
             newProductProgress.setStage(getCellValueByIndex(row, stageIndex));
            newProductProgress.setProductName(getCellValueByIndex(row, productNameIndex));
            newProductProgress.setImageUrl(getCellValueByIndex(row, imageUrlIndex));
            newProductProgress.setProjectStartTime(getCellValueByIndex(row, projectStartTimeIndex));
            newProductProgress.setTargetLaunchTime(getCellValueByIndex(row, targetLaunchTimeIndex));
             newProductProgress.setDisplayType(validateDisplayType(getCellValueByIndex(row, displayTypeIndex)));
            newProductProgress.setProductLevel(getCellValueByIndex(row, productLevelIndex));
            newProductProgress.setPrimarySupplier(getCellValueByIndex(row, primarySupplierIndex));
            newProductProgress.setLeadDqe(getCellValueByIndex(row, leadDqeIndex));
            newProductProgress.setElectronicRd(getCellValueByIndex(row, electronicRdIndex));
            newProductProgress.setStatus(getCellValueByIndex(row, statusIndex));
            newProductProgress.setDesignReviewProblem(getCellValueByIndex(row, designReviewProblemIndex));
            newProductProgress.setEvtProblem(getCellValueByIndex(row, evtProblemIndex));
            newProductProgress.setDvtProblem(getCellValueByIndex(row, dvtProblemIndex));
            newProductProgress.setMainProjectProgress(getCellValueByIndex(row, mainProjectProgressIndex));
            newProductProgress.setMainQualityRisks(getCellValueByIndex(row, mainQualityRisksIndex));
            
            // 设置创建时间和更新时间
            newProductProgress.setCreateTime(LocalDateTime.now());
            newProductProgress.setUpdateTime(LocalDateTime.now());
            newProductProgress.setIsDeleted(0);
            
            return newProductProgress;
            
        } catch (Exception e) {
            throw new RuntimeException("解析行数据失败: " + e.getMessage());
        }
    }

    /**
     * 根据字段标题获取单元格值
     * @param row Excel行
     * @param columnMapping 字段名到列索引的映射
     * @param headerName 字段标题名
     * @return 单元格值
     */
    private String getCellValueByHeader(Row row, Map<String, Integer> columnMapping, String headerName) {
        Integer columnIndex = columnMapping.get(headerName);
        if (columnIndex != null) {
            Cell cell = row.getCell(columnIndex);
            return getCellValueAsString(cell);
        }
        return "";
    }

    /**
     * 根据列索引获取单元格值
     * @param row Excel行
     * @param columnIndex 列索引
     * @return 单元格值
     */
    private String getCellValueByIndex(Row row, Integer columnIndex) {
        if (columnIndex != null) {
            Cell cell = row.getCell(columnIndex);
            return getCellValueAsString(cell);
        }
        return "";
    }

    /**
     * 解析Excel行数据为新品进度对象（旧方法，保留兼容性）
     * @param row Excel行
     * @return 新品进度对象
     */
    private NewProductProgress parseRowToNewProductProgress(Row row) {
        NewProductProgress newProductProgress = new NewProductProgress();
        
        try {
            // 根据列索引获取数据
            newProductProgress.setProductCategoryLevel3(getCellValueAsString(row.getCell(1)));
            newProductProgress.setPriority(getCellValueAsString(row.getCell(2)));
            newProductProgress.setModel(getCellValueAsString(row.getCell(3)));
            newProductProgress.setSku(getCellValueAsString(row.getCell(4)));
            newProductProgress.setStage(getCellValueAsString(row.getCell(5)));
            newProductProgress.setProductName(getCellValueAsString(row.getCell(6)));
            newProductProgress.setImageUrl(getCellValueAsString(row.getCell(7)));
            newProductProgress.setProjectStartTime(getCellValueAsString(row.getCell(8)));
            newProductProgress.setTargetLaunchTime(getCellValueAsString(row.getCell(9)));
            newProductProgress.setDisplayType(getCellValueAsString(row.getCell(10)));
            newProductProgress.setProductLevel(getCellValueAsString(row.getCell(11)));
            newProductProgress.setPrimarySupplier(getCellValueAsString(row.getCell(12)));
            newProductProgress.setLeadDqe(getCellValueAsString(row.getCell(13)));
            newProductProgress.setElectronicRd(getCellValueAsString(row.getCell(14)));
            newProductProgress.setStatus(getCellValueAsString(row.getCell(15)));
            newProductProgress.setDesignReviewProblem(getCellValueAsString(row.getCell(16)));
            newProductProgress.setEvtProblem(getCellValueAsString(row.getCell(17)));
            newProductProgress.setDvtProblem(getCellValueAsString(row.getCell(18)));
            newProductProgress.setMainProjectProgress(getCellValueAsString(row.getCell(19)));
            newProductProgress.setMainQualityRisks(getCellValueAsString(row.getCell(20)));
            
            // 设置创建时间和更新时间
            newProductProgress.setCreateTime(LocalDateTime.now());
            newProductProgress.setUpdateTime(LocalDateTime.now());
            newProductProgress.setIsDeleted(0);
            
            return newProductProgress;
            
        } catch (Exception e) {
            throw new RuntimeException("解析行数据失败: " + e.getMessage());
        }
    }

    /**
     * 检查行是否为空
     * @param row Excel行
     * @param columnMapping 字段映射
     * @return 是否为空行
     */
    private boolean isRowEmpty(Row row, Map<String, Integer> columnMapping) {
        if (row == null) {
            return true;
        }
        
        // 检查"产品三级类目"字段是否有值
        Integer productCategoryIndex = columnMapping.get("产品三级类目");
        if (productCategoryIndex != null) {
            Cell cell = row.getCell(productCategoryIndex);
            if (cell != null) {
                String value = getCellValueAsString(cell).trim();
                if (!value.isEmpty()) {
                    return false; // 如果产品三级类目有值，就不是空行
                }
            }
        }
        
        return true; // 产品三级类目为空，视为空行
    }

    /**
     * 验证产品进度数据是否有效
     * @param newProductProgress 产品进度对象
     * @return 是否有效
     */
    private boolean isValidProductProgress(NewProductProgress newProductProgress) {
        if (newProductProgress == null) {
            return false;
        }
        
        // 主要判断标准：产品三级类目必须有值
        if (newProductProgress.getProductCategoryLevel3() == null || 
            newProductProgress.getProductCategoryLevel3().trim().isEmpty()) {
            return false;
        }
        
        // 其他字段可以为空，但SKU必须生成
        if (newProductProgress.getSku() == null || newProductProgress.getSku().trim().isEmpty()) {
            return false;
        }
        
        return true;
    }

    /**
     * 验证并清理SKU值
     * @param sku SKU值
     * @param usedSkus 已使用的SKU集合（用于记录，不再用于重复检查）
     * @param rowNum 行号
     * @return 有效的SKU值
     */
    private String validateAndGenerateSku(String sku, Set<String> usedSkus, int rowNum) {
        if (sku == null || sku.trim().isEmpty()) {
            // 如果SKU为空，生成一个唯一的SKU
            return generateUniqueSku("AUTO_SKU", usedSkus, rowNum);
        }
        
        String trimmed = sku.trim();
        
        if (trimmed.isEmpty()) {
            // 如果SKU为空字符串，生成一个唯一的SKU
            return generateUniqueSku("AUTO_SKU", usedSkus, rowNum);
        }
        
        // 记录使用的SKU（用于统计，不再用于重复检查）
        usedSkus.add(trimmed);
        
        // 直接返回原始SKU，重复检查在批量处理时进行
        return trimmed;
    }

    /**
     * 生成唯一的SKU值
     * @param baseSku 基础SKU
     * @param usedSkus 已使用的SKU集合
     * @param rowNum 行号
     * @return 唯一的SKU值
     */
    private String generateUniqueSku(String baseSku, Set<String> usedSkus, int rowNum) {
        String candidateSku;
        int counter = 1;
        
        do {
            candidateSku = baseSku + "_" + System.currentTimeMillis() + "_" + rowNum + "_" + counter;
            counter++;
        } while (usedSkus.contains(candidateSku));
        
        return candidateSku;
    }

    /**
     * 验证并清理displayType字段值
     * @param displayType 显示类型值
     * @return 清理后的显示类型值
     */
    private String validateDisplayType(String displayType) {
        if (displayType == null || displayType.trim().isEmpty()) {
            return "";
        }
        
        String trimmed = displayType.trim();
        
        // 如果值包含ENUM中定义的值，直接返回
        if (trimmed.equals("单显") || trimmed.equals("双显") || trimmed.equals("三显") || trimmed.equals("四显")) {
            return trimmed;
        }
        
        // 如果值不匹配ENUM，返回原值（因为现在是TEXT类型）
        return trimmed;
    }

    /**
     * 获取单元格值作为字符串
     * @param cell 单元格
     * @return 字符串值
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // 格式化日期为 YYYY-M-D 格式
                    return formatDateToString(cell.getDateCellValue());
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
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
     * 格式化日期为 YYYY-M-D 格式
     * @param date 日期对象
     * @return 格式化后的日期字符串
     */
    private String formatDateToString(java.util.Date date) {
        if (date == null) {
            return "";
        }
        
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(date);
        
        int year = calendar.get(java.util.Calendar.YEAR);
        int month = calendar.get(java.util.Calendar.MONTH) + 1; // 月份从0开始，需要+1
        int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);
        
        return year + "-" + month + "-" + day;
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
     * 从Excel行中提取图片并保存到指定目录
     * @param row Excel行
     * @param drawing 绘图对象
     * @param columnMapping 列映射
     * @param rowIndex 行索引
     * @return 保存的图片路径
     */
    private String extractAndSaveImageFromRow(Row row, Drawing<?> drawing, Map<String, Integer> columnMapping, int rowIndex) {
        try {
            if (drawing == null) {
                return null;
            }

            // 查找图片列的索引
            Integer imageColumnIndex = columnMapping.get("图片");
            if (imageColumnIndex == null) {
                return null;
            }

            // 检查该行该列是否有图片
            for (Shape shape : drawing) {
                if (shape instanceof Picture) {
                    Picture picture = (Picture) shape;
                    ClientAnchor anchor = picture.getClientAnchor();

                    if (anchor != null && 
                        anchor.getCol1() == imageColumnIndex && 
                        anchor.getRow1() == row.getRowNum()) {
                        
                        if (picture.getPictureData() != null) {
                            byte[] pictureData = picture.getPictureData().getData();
                            
                            // 生成图片文件名
                            String fileName = "product_" + System.currentTimeMillis() + "_" + rowIndex + ".png";
                            
                            // 检查并创建图片目录
                            Path imageDir = Paths.get(imagePath);
                            if (!Files.exists(imageDir)) {
                                Files.createDirectories(imageDir);
                            }
                            
                            // 保存图片
                            Path imageFile = imageDir.resolve(fileName);
                            Files.write(imageFile, pictureData);
                            
                            // 返回相对路径
                            return "/imageDirectory/" + fileName;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("提取图片失败，行 " + (rowIndex + 1) + ": " + e.getMessage());
        }
        
        return null;
    }
}

