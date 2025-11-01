package com.lu.ddwyydemo04.controller;

import com.lu.ddwyydemo04.Service.NewProductProgressService;
import com.lu.ddwyydemo04.Service.ProjectTableService;
import com.lu.ddwyydemo04.Service.UserAccessLogService;
import com.lu.ddwyydemo04.pojo.NewProductProgress;
import com.lu.ddwyydemo04.pojo.ProjectTable;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
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
import org.springframework.core.io.ByteArrayResource;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 新品进度管理控制器
 * 处理新品进度相关的页面跳转和API请求
 */
@Controller
public class NewProductProgressController {

    @Value("${file.storage.templatespath}")
    private String templatesPath;

    @Value("${file.storage.imagepath}")
    private String imagePath;

    @Autowired
    private NewProductProgressService newProductProgressService;

    @Autowired
    private ProjectTableService projectTableService;

    @Autowired
    private UserAccessLogService userAccessLogService;

    /**
     * 新品进度管理页面跳转
     * @return 新品进度管理页面视图
     */
    @GetMapping("/qualityProgress")
    public String qualityProgressPage(String username, String job, HttpServletRequest request) {
        userAccessLogService.recordUserAccess(username,job,"新品质量进度页面",request);
        return "qualityProgress";
    }

    /**
     * 项目表格页面跳转
     * @return 项目表格页面视图
     */
    @GetMapping("/projectTable")
    public String projectTablePage() {
        return "projectTable";
    }

    /**
     * 获取新品进度列表
     * @param username 用户名
     * @param job 角色
     * @param page 页码（可选，默认为1）
     * @param size 每页大小（可选，默认为20）
     * @param productCategory 产品三级类目筛选（可选）
     * @param priority 优先级筛选（可选）
     * @param model 型号筛选（可选）
     * @param sku SKU筛选（可选）
     * @param stage 阶段筛选（可选）
     * @param productName 产品名称筛选（可选）
     * @param leadDqe 主导DQE筛选（可选）
     * @param electronicRd 电子研发筛选（可选）
     * @param status 状态筛选（可选）
     * @param testStartDate 测试开始日期筛选（可选）
     * @param testEndDate 测试结束日期筛选（可选）
     * @return 新品进度列表
     */
    @GetMapping("/newProductProgress/getNewProductProgress")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getNewProductProgress(
            @RequestParam String username,
            @RequestParam String job,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String productCategory,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String leadDqe,
            @RequestParam(required = false) String electronicRd,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String testStartDate,
            @RequestParam(required = false) String testEndDate) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 调用Service层获取真实数据
            List<NewProductProgress> allData = newProductProgressService.getAllNewProductProgress();
//            System.out.println("allData:"+allData);
            
            // 应用筛选条件
//            List<NewProductProgress> filteredData = allData.stream()
//                .filter(item -> productCategory == null || productCategory.isEmpty() ||
//                    (item.getProductCategoryLevel3() != null && item.getProductCategoryLevel3().toLowerCase().contains(productCategory.toLowerCase())))
//                .filter(item -> priority == null || priority.isEmpty() ||
//                    (item.getPriority() != null && item.getPriority().toLowerCase().contains(priority.toLowerCase())))
//                .filter(item -> model == null || model.isEmpty() ||
//                    (item.getModel() != null && item.getModel().toLowerCase().contains(model.toLowerCase())))
//                .filter(item -> sku == null || sku.isEmpty() ||
//                    (item.getSku() != null && item.getSku().toLowerCase().contains(sku.toLowerCase())))
//                .filter(item -> stage == null || stage.isEmpty() ||
//                    (item.getStage() != null && item.getStage().toLowerCase().contains(stage.toLowerCase())))
//                .filter(item -> productName == null || productName.isEmpty() ||
//                    (item.getProductName() != null && item.getProductName().toLowerCase().contains(productName.toLowerCase())))
//                .filter(item -> leadDqe == null || leadDqe.isEmpty() ||
//                    (item.getLeadDqe() != null && item.getLeadDqe().toLowerCase().contains(leadDqe.toLowerCase())))
//                .filter(item -> electronicRd == null || electronicRd.isEmpty() ||
//                    (item.getElectronicRd() != null && item.getElectronicRd().toLowerCase().contains(electronicRd.toLowerCase())))
//                .filter(item -> status == null || status.isEmpty() ||
//                    (item.getStatus() != null && item.getStatus().toLowerCase().contains(status.toLowerCase())))
//                .filter(item -> testStartDate == null || testStartDate.isEmpty() ||
//                    (item.getProjectStartTime() != null && item.getProjectStartTime().compareTo(testStartDate) >= 0))
//                .filter(item -> testEndDate == null || testEndDate.isEmpty() ||
//                    (item.getProjectStartTime() != null && item.getProjectStartTime().compareTo(testEndDate) <= 0))
//                .collect(Collectors.toList());

//            System.out.println("filteredData:"+filteredData);

            // 分页处理
            int total = allData.size();
            int totalPages = (int) Math.ceil((double) total / size);
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, total);

            List<NewProductProgress> pageData;
            if (total == 0) {
                pageData = new ArrayList<>();
            } else {
                pageData = allData.subList(startIndex, endIndex);
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
            result.put("error", "获取新品进度数据失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 获取新品进度详情
     * @param id 新品进度ID
     * @param username 用户名
     * @return 新品进度详情
     */
    @GetMapping("/newProductProgress/getNewProductDetail")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getNewProductDetail(
            @RequestParam Long id,
            @RequestParam String username) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 调用Service层获取真实数据
            NewProductProgress productDetail = newProductProgressService.getNewProductProgressById(id);
            
            if (productDetail != null) {
                result.put("success", true);
                result.put("data", productDetail);
            } else {
                result.put("success", false);
                result.put("error", "未找到指定的新品进度记录");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "获取新品进度详情失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 新增新品进度
     * @param requestData 新品进度数据
     * @return 新增结果
     */
    @PostMapping("/newProductProgress/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addNewProductProgress(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 创建新的新品进度对象
            NewProductProgress newProduct = new NewProductProgress();
            
            // 设置创建时间
            newProduct.setCreateTime(LocalDateTime.now());
            newProduct.setUpdateTime(LocalDateTime.now());
            
            // 设置创建者和更新者
            String username = (String) requestData.get("username");
            if (username != null && !username.trim().isEmpty()) {
                newProduct.setCreateBy(username);
                newProduct.setUpdateBy(username);
            }
            
            
            // 更新字段
            updateNewProductFields(newProduct, requestData);

            // 调用Service层插入数据
            int insertResult = newProductProgressService.insertNewProductProgress(newProduct);
            
            if (insertResult > 0) {
                result.put("success", true);
                result.put("message", "新品进度新增成功");
                result.put("data", newProduct);
            } else {
                result.put("success", false);
                result.put("message", "数据库插入失败");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "新增新品进度失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 更新新品进度
     * @param requestData 新品进度数据
     * @return 更新结果
     */
    @PostMapping("/newProductProgress/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateNewProductProgress(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取请求参数
            Long id = Long.valueOf(requestData.get("id").toString());
            
            // 参数验证
            if (id == null) {
                result.put("success", false);
                result.put("message", "新品进度ID不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            // 先查询现有记录
            NewProductProgress existingProduct = newProductProgressService.getNewProductProgressById(id);
            if (existingProduct == null) {
                result.put("success", false);
                result.put("message", "未找到指定的新品进度记录");
                return ResponseEntity.badRequest().body(result);
            }

            // 更新字段
            updateNewProductFields(existingProduct, requestData);
            existingProduct.setUpdateTime(LocalDateTime.now());
            
            // 设置更新者
            String username = (String) requestData.get("username");
            if (username != null && !username.trim().isEmpty()) {
                existingProduct.setUpdateBy(username);
            }
//            System.out.println("existingProduct:"+existingProduct);

            // 调用Service层更新数据库
            boolean updateSuccess = newProductProgressService.updateNewProductProgress(existingProduct);
            
            if (updateSuccess) {
                result.put("success", true);
                result.put("message", "新品进度更新成功");
                result.put("data", existingProduct);
            } else {
                result.put("success", false);
                result.put("message", "数据库更新失败");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新新品进度失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 更新新品进度字段
     * @param product 新品进度对象
     * @param requestData 请求数据
     */
    private void updateNewProductFields(NewProductProgress product, Map<String, Object> requestData) {
        // 更新基本信息
        if (requestData.get("groupName") != null) {
            product.setGroupName(requestData.get("groupName").toString());
        }
        if (requestData.get("productCategoryLevel3") != null) {
            product.setProductCategoryLevel3(requestData.get("productCategoryLevel3").toString());
        }
        if (requestData.get("priority") != null) {
            product.setPriority(requestData.get("priority").toString());
        }
        if (requestData.get("model") != null) {
            product.setModel(requestData.get("model").toString());
        }
        if (requestData.get("sku") != null) {
            product.setSku(requestData.get("sku").toString());
        }
        if (requestData.get("stage") != null) {
            product.setStage(requestData.get("stage").toString());
        }
        if (requestData.get("productName") != null) {
            product.setProductName(requestData.get("productName").toString());
        }
        if (requestData.get("imageUrl") != null) {
            product.setImageUrl(requestData.get("imageUrl").toString());
        }
        if (requestData.get("projectStartTime") != null) {
            product.setProjectStartTime(requestData.get("projectStartTime").toString());
        }
        if (requestData.get("targetLaunchTime") != null) {
            product.setTargetLaunchTime(requestData.get("targetLaunchTime").toString());
        }
        if (requestData.get("displayType") != null) {
            product.setDisplayType(requestData.get("displayType").toString());
        }
        if (requestData.get("productLevel") != null) {
            product.setProductLevel(requestData.get("productLevel").toString());
        }
        if (requestData.get("primarySupplier") != null) {
            product.setPrimarySupplier(requestData.get("primarySupplier").toString());
        }
        if (requestData.get("leadDqe") != null) {
            product.setLeadDqe(requestData.get("leadDqe").toString());
        }
        if (requestData.get("electronicRd") != null) {
            product.setElectronicRd(requestData.get("electronicRd").toString());
        }
        if (requestData.get("status") != null) {
            product.setStatus(requestData.get("status").toString());
        }
        if (requestData.get("designReviewProblem") != null) {
            product.setDesignReviewProblem(requestData.get("designReviewProblem").toString());
        }
        if (requestData.get("evtProblem") != null) {
            product.setEvtProblem(requestData.get("evtProblem").toString());
        }
        if (requestData.get("dvtProblem") != null) {
            product.setDvtProblem(requestData.get("dvtProblem").toString());
        }
        if (requestData.get("mainProjectProgress") != null) {
            product.setMainProjectProgress(requestData.get("mainProjectProgress").toString());
        }
        if (requestData.get("mainQualityRisks") != null) {
            product.setMainQualityRisks(requestData.get("mainQualityRisks").toString());
        }
    }

    /**
     * 批量删除选中的新品进度
     * @param requestData 包含要删除的ID列表
     * @return 删除结果
     */
    @PostMapping("/newProductProgress/deleteSelected")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteSelectedNewProductProgress(@RequestBody Map<String, Object> requestData) {
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
            int deletedCount = newProductProgressService.deleteNewProductProgressByIds(ids);
            
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
     * 导入新品进度数据
     * @param file Excel文件
     * @param username 用户名
     * @return 导入结果
     */
    @PostMapping("/newProductProgress/importReviewResults")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> importNewProductProgress(
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

            // 检查文件大小
            long fileSize = file.getSize();
            if (fileSize > 100 * 1024 * 1024) { // 100MB
                result.put("success", false);
                result.put("message", "文件过大，请上传小于100MB的文件");
                return ResponseEntity.badRequest().body(result);
            }

            System.out.println("开始处理文件: " + fileName + ", 大小: " + (fileSize / 1024 / 1024) + "MB");

            // 调用Service层处理导入
            NewProductProgressService.ImportResult importResult = newProductProgressService.importNewProductProgressFromExcel(file, username);

            System.out.println("文件处理完成: " + importResult.getMessage());

            result.put("success", importResult.isSuccess());
            result.put("message", importResult.getMessage());
            result.put("insertCount", importResult.getInsertCount());
            result.put("updateCount", importResult.getUpdateCount());
            result.put("errorCount", importResult.getErrorCount());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("导入失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "导入失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 下载新品进度导入模板
     * @return 模板文件信息
     */
    @GetMapping("/newProductProgress/downloadTemplate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> downloadTemplate() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 从application.yml中获取模板路径
            String templatePath = templatesPath + "/新品进度导入模板.xlsx";
            Path file = Paths.get(templatePath);
            
            System.out.println("尝试下载模板文件，路径: " + templatePath);
            System.out.println("文件是否存在: " + Files.exists(file));
            
            if (!Files.exists(file)) {
                // 如果模板文件不存在，返回提示信息
                result.put("success", false);
                result.put("message", "模板文件不存在，请先创建模板文件");
                result.put("templatePath", templatePath);
                result.put("instructions", "请将新品进度导入模板.xlsx文件放到以下路径：" + templatePath);
                return ResponseEntity.ok(result);
            }
            
            // 如果文件存在，返回文件信息
            result.put("success", true);
            result.put("message", "模板文件存在，可以下载");
            result.put("templatePath", templatePath);
            result.put("fileSize", Files.size(file));
            result.put("downloadUrl", "/newProductProgress/downloadTemplateFile");
            
            return ResponseEntity.ok(result);
                    
        } catch (Exception e) {
            System.err.println("检查模板文件时发生错误: " + e.getMessage());
            e.printStackTrace();
            
            result.put("success", false);
            result.put("message", "检查模板文件时发生错误: " + e.getMessage());
            result.put("templatePath", templatesPath + "/新品进度导入模板.xlsx");
            return ResponseEntity.ok(result);
        }
    }
    
    /**
     * 实际下载模板文件
     * @return 模板文件
     */
    @GetMapping("/newProductProgress/downloadTemplateFile")
    public ResponseEntity<Resource> downloadTemplateFile() {
        try {
            String templatePath = templatesPath + "/新品进度导入模板.xlsx";
            Path file = Paths.get(templatePath);
            
            if (!Files.exists(file)) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new UrlResource(file.toUri());
            
            // 对中文文件名进行URL编码
            String filename = "新品进度导入模板.xlsx";
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
     * 上传产品图片
     * @param file 图片文件
     * @return 上传结果
     */
    @PostMapping("/newProductProgress/uploadImage")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "请选择要上传的图片文件");
                return ResponseEntity.badRequest().body(result);
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                result.put("success", false);
                result.put("message", "文件类型不正确，请上传图片文件");
                return ResponseEntity.badRequest().body(result);
            }

            // 检查文件大小（限制为5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                result.put("success", false);
                result.put("message", "图片文件过大，请上传小于5MB的图片");
                return ResponseEntity.badRequest().body(result);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = "product_" + System.currentTimeMillis() + extension;

            // 检查并创建图片目录
            Path imageDir = Paths.get(imagePath);
            if (!Files.exists(imageDir)) {
                Files.createDirectories(imageDir);
            }

            // 保存文件
            Path filePath = imageDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 返回相对路径
            String imagePath = "/imageDirectory/" + fileName;

            result.put("success", true);
            result.put("message", "图片上传成功");
            result.put("imagePath", imagePath);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("图片上传失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "图片上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 导出新品进度数据
     */
    @PostMapping("/newProductProgress/exportQualityProgressData")
    public ResponseEntity<byte[]> exportQualityProgressData(@RequestBody Map<String, Object> requestData) {
        try {
            System.out.println("开始导出新品进度数据...");
            System.out.println("导出参数: " + requestData);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) requestData.get("data");
            String exportType = (String) requestData.get("exportType");
            String fileName = (String) requestData.get("fileName");
            Integer totalCount = (Integer) requestData.get("totalCount");
            String username = (String) requestData.get("username");
            String job = (String) requestData.get("job");
            
            System.out.println("导出数据量: " + (dataList != null ? dataList.size() : 0));
            System.out.println("导出类型: " + exportType);
            System.out.println("文件名: " + fileName);
            System.out.println("用户名: " + username);
            System.out.println("角色: " + job);
            
            // 打印数据字段信息
            if (!dataList.isEmpty()) {
                System.out.println("数据字段: " + dataList.get(0).keySet());
                System.out.println("数据样本: " + dataList.get(0));
            }
            
            if (dataList == null || dataList.isEmpty()) {
                System.err.println("导出数据为空");
                return ResponseEntity.badRequest().build();
            }
            
            // 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("新品进度数据");
            
            // 动态获取所有字段名（基于第一行数据的所有键）
            Set<String> allFields = new LinkedHashSet<>();
            if (!dataList.isEmpty()) {
                allFields.addAll(dataList.get(0).keySet());
            }
            
            // 过滤掉不需要的字段
            Set<String> excludedFields = new HashSet<>();
            excludedFields.add("createTime");
            excludedFields.add("updateTime");
            excludedFields.add("createBy");
            excludedFields.add("updateBy");
            excludedFields.add("isDeleted");
            
            allFields.removeAll(excludedFields);
            
            // 定义字段显示顺序和中文名称映射
            Map<String, String> fieldDisplayNames = new LinkedHashMap<>();
            fieldDisplayNames.put("id", "序号");
            fieldDisplayNames.put("groupName", "组别");
            fieldDisplayNames.put("productCategoryLevel3", "产品三级类目");
            fieldDisplayNames.put("priority", "优先级");
            fieldDisplayNames.put("model", "型号");
            fieldDisplayNames.put("sku", "SKU");
            fieldDisplayNames.put("stage", "阶段");
            fieldDisplayNames.put("productName", "产品名称（接口/功能信息）");
            fieldDisplayNames.put("imageUrl", "图片");
            fieldDisplayNames.put("projectStartTime", "立项时间");
            fieldDisplayNames.put("targetLaunchTime", "目标上市时间");
            fieldDisplayNames.put("displayType", "单显/双显/三显/四显");
            fieldDisplayNames.put("productLevel", "产品等级");
            fieldDisplayNames.put("primarySupplier", "一级供方");
            fieldDisplayNames.put("leadDqe", "主导DQE");
            fieldDisplayNames.put("electronicRd", "电子研发");
            fieldDisplayNames.put("status", "状态");
            fieldDisplayNames.put("designReviewProblem", "设计评审主要问题");
            fieldDisplayNames.put("evtProblem", "EVT主要问题");
            fieldDisplayNames.put("dvtProblem", "DVT主要问题");
            fieldDisplayNames.put("mainProjectProgress", "主要项目进度（只填重要内容或一句话）");
            fieldDisplayNames.put("mainQualityRisks", "质量主要风险（只填重要内容或一句话）");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            List<String> orderedHeaders = new ArrayList<>();
            List<String> orderedFieldNames = new ArrayList<>();
            
            // 按照预定义顺序添加字段
            for (Map.Entry<String, String> entry : fieldDisplayNames.entrySet()) {
                if (allFields.contains(entry.getKey())) {
                    orderedHeaders.add(entry.getValue());
                    orderedFieldNames.add(entry.getKey());
                }
            }
            
            // 添加其他未预定义的字段
            for (String field : allFields) {
                if (!orderedFieldNames.contains(field)) {
                    orderedHeaders.add(field);
                    orderedFieldNames.add(field);
                }
            }
            
            System.out.println("导出字段顺序: " + orderedFieldNames);
            System.out.println("导出字段标题: " + orderedHeaders);
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.MEDIUM);
            headerStyle.setBorderTop(BorderStyle.MEDIUM);
            headerStyle.setBorderRight(BorderStyle.MEDIUM);
            headerStyle.setBorderLeft(BorderStyle.MEDIUM);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            
            // 设置标题行高度
            headerRow.setHeightInPoints(25);
            
            for (int i = 0; i < orderedHeaders.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(orderedHeaders.get(i));
                cell.setCellStyle(headerStyle);
            }
            
            // 填充数据行
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            dataStyle.setWrapText(true); // 自动换行
            
            // 设置数据行字体
            Font dataFont = workbook.createFont();
            dataFont.setFontHeightInPoints((short) 12);
            dataStyle.setFont(dataFont);
            
            // 找到图片字段的列索引
            int imageColumnIndex = -1;
            for (int j = 0; j < orderedFieldNames.size(); j++) {
                if ("imageUrl".equals(orderedFieldNames.get(j))) {
                    imageColumnIndex = j;
                    break;
                }
            }
            
            for (int i = 0; i < dataList.size(); i++) {
                Map<String, Object> rowData = dataList.get(i);
                Row dataRow = sheet.createRow(i + 1);
                
                // 设置数据行高度，如果有图片列则增加高度
                if (imageColumnIndex >= 0) {
                    dataRow.setHeightInPoints(60); // 图片行需要更高
                } else {
                    dataRow.setHeightInPoints(20);
                }
                
                // 根据字段顺序动态填充数据
                for (int j = 0; j < orderedFieldNames.size(); j++) {
                    String fieldName = orderedFieldNames.get(j);
                    Cell cell = dataRow.createCell(j);
                    
                    if (j == imageColumnIndex) {
                        // 处理图片字段
                        String imagePath = getStringValue(rowData, fieldName);
                        if (imagePath != null && !imagePath.trim().isEmpty()) {
                            try {
                                // 尝试加载图片并插入到Excel中
                                insertImageToCell(workbook, sheet, cell, imagePath, i + 1, j);
                            } catch (Exception e) {
                                System.err.println("插入图片失败: " + imagePath + ", 错误: " + e.getMessage());
                                cell.setCellValue("图片加载失败");
                                cell.setCellStyle(dataStyle);
                            }
                        } else {
                            cell.setCellValue("无图片");
                            cell.setCellStyle(dataStyle);
                        }
                    } else {
                        // 普通字段
                        String cellValue = getStringValue(rowData, fieldName);
                        cell.setCellValue(cellValue);
                        cell.setCellStyle(dataStyle);
                    }
                }
            }
            
            // 自动调整列宽
            for (int i = 0; i < orderedHeaders.size(); i++) {
                if (i == imageColumnIndex) {
                    // 图片列设置固定宽度
                    sheet.setColumnWidth(i, 6000); // 图片列更宽
                } else {
                    sheet.autoSizeColumn(i);
                    // 设置最小列宽，让列更宽一些
                    int currentWidth = sheet.getColumnWidth(i);
                    int minWidth = 4000; // 增加最小列宽
                    if (currentWidth < minWidth) {
                        sheet.setColumnWidth(i, minWidth);
                    } else {
                        // 如果自动调整的宽度合适，再增加一些
                        sheet.setColumnWidth(i, (int)(currentWidth * 1.2));
                    }
                }
            }
            
            // 转换为字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            byte[] excelBytes = outputStream.toByteArray();
            outputStream.close();
            
            System.out.println("Excel文件生成成功，大小: " + excelBytes.length + " bytes");
            
            // 设置响应头
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            responseHeaders.setContentDispositionFormData("attachment", fileName);
            responseHeaders.setContentLength(excelBytes.length);
            
            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(excelBytes);
                    
        } catch (Exception e) {
            System.err.println("导出新品进度数据失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 获取字符串值，支持多个字段名
     */
    private String getStringValue(Map<String, Object> data, String... fieldNames) {
        for (String fieldName : fieldNames) {
            Object value = data.get(fieldName);
            if (value != null) {
                return value.toString();
            }
        }
        return "";
    }
    
    /**
     * 读取InputStream的所有字节（Java 8兼容）
     */
    private byte[] readAllBytes(InputStream inputStream) throws Exception {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        
        return buffer.toByteArray();
    }
    
    /**
     * 将图片插入到Excel单元格中
     */
    private void insertImageToCell(Workbook workbook, Sheet sheet, Cell cell, String imagePath, int rowIndex, int colIndex) {
        try {
            // 处理相对路径，转换为绝对路径
            String fullImagePath;
            if (imagePath.startsWith("/")) {
                // 如果是绝对路径，直接使用
                fullImagePath = imagePath;
            } else if (imagePath.startsWith("http")) {
                // 如果是URL，直接使用
                fullImagePath = imagePath;
            } else {
                // 如果是相对路径，拼接基础路径
                fullImagePath = imagePath.startsWith("/") ? imagePath : "/" + imagePath;
            }
            
            System.out.println("尝试加载图片: " + fullImagePath);
            System.out.println("配置的图片路径: " + this.imagePath);
            
            // 创建图片输入流
            InputStream imageStream;
            if (fullImagePath.startsWith("http")) {
                // 网络图片
                java.net.URL url = new java.net.URL(fullImagePath);
                imageStream = url.openStream();
            } else {
                // 本地文件 - 尝试多个可能的路径
                Path path = null;
                
                // 1. 尝试直接使用原始路径
                path = Paths.get(imagePath);
                if (!Files.exists(path)) {
                    // 2. 尝试在项目根目录下查找
                    path = Paths.get(System.getProperty("user.dir"), imagePath.replaceFirst("^/", ""));
                }
                if (!Files.exists(path)) {
                    // 3. 尝试在static目录下查找
                    path = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "static", imagePath.replaceFirst("^/", ""));
                }
                if (!Files.exists(path)) {
                    // 4. 尝试在target目录下查找（编译后的路径）
                    path = Paths.get(System.getProperty("user.dir"), "target", "classes", "static", imagePath.replaceFirst("^/", ""));
                }
                if (!Files.exists(path)) {
                    // 5. 尝试使用配置的图片路径
                    path = Paths.get(this.imagePath, imagePath.replaceFirst("^/imageDirectory/", ""));
                }
                if (!Files.exists(path)) {
                    // 6. 尝试直接拼接配置路径和文件名
                    String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
                    path = Paths.get(this.imagePath, fileName);
                }
                
                if (path == null || !Files.exists(path)) {
                    System.err.println("尝试的图片路径:");
                    System.err.println("1. 原始路径: " + imagePath);
                    System.err.println("2. 项目根目录: " + Paths.get(System.getProperty("user.dir"), imagePath.replaceFirst("^/", "")));
                    System.err.println("3. static目录: " + Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "static", imagePath.replaceFirst("^/", "")));
                    System.err.println("4. target目录: " + Paths.get(System.getProperty("user.dir"), "target", "classes", "static", imagePath.replaceFirst("^/", "")));
                    System.err.println("5. 配置路径: " + Paths.get(this.imagePath, imagePath.replaceFirst("^/imageDirectory/", "")));
                    System.err.println("6. 配置路径+文件名: " + Paths.get(this.imagePath, imagePath.substring(imagePath.lastIndexOf("/") + 1)));
                    System.err.println("配置的图片路径: " + this.imagePath);
                    throw new FileNotFoundException("图片文件不存在: " + imagePath);
                }
                
                System.out.println("找到图片文件: " + path.toString());
                imageStream = Files.newInputStream(path);
            }
            
            // 读取图片数据
            byte[] imageBytes = readAllBytes(imageStream);
            imageStream.close();
            
            // 获取图片格式
            int pictureType;
            if (imagePath.toLowerCase().endsWith(".png")) {
                pictureType = Workbook.PICTURE_TYPE_PNG;
            } else if (imagePath.toLowerCase().endsWith(".jpg") || imagePath.toLowerCase().endsWith(".jpeg")) {
                pictureType = Workbook.PICTURE_TYPE_JPEG;
            } else if (imagePath.toLowerCase().endsWith(".gif")) {
                pictureType = Workbook.PICTURE_TYPE_JPEG; // GIF转换为JPEG
            } else {
                pictureType = Workbook.PICTURE_TYPE_JPEG; // 默认JPEG
            }
            
            // 添加图片到工作簿
            int pictureIdx = workbook.addPicture(imageBytes, pictureType);
            
            // 创建绘图对象
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            
            // 创建锚点
            ClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
            anchor.setCol1(colIndex);
            anchor.setRow1(rowIndex);
            anchor.setCol2(colIndex + 1);
            anchor.setRow2(rowIndex + 1);
            
            // 插入图片
            Picture picture = drawing.createPicture(anchor, pictureIdx);
            
            // 设置图片大小（可选）
            picture.resize(0.8); // 缩放到80%
            
            System.out.println("图片插入成功: " + imagePath);
            
        } catch (Exception e) {
            System.err.println("插入图片失败: " + imagePath + ", 错误: " + e.getMessage());
            e.printStackTrace();
            // 如果图片插入失败，显示错误信息
            cell.setCellValue("图片加载失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目表格数据
     * @param page 页码（可选，默认为1）
     * @param size 每页大小（可选，默认为20）
     * @param supplier 供应商筛选（可选）
     * @param sampleModel 样品型号筛选（可选）
     * @param sampleName 样品名称筛选（可选）
     * @param categoryDetail 品类细分筛选（可选）
     * @param tester 测试人筛选（可选）
     * @param dqe DQE筛选（可选）
     * @return 项目表格数据
     */
    @GetMapping("/projectTable/getProjectTableData")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProjectTableData(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false) String sampleModel,
            @RequestParam(required = false) String sampleName,
            @RequestParam(required = false) String categoryDetail,
            @RequestParam(required = false) String tester,
            @RequestParam(required = false) String dqe) {

        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, Object> data;
            
            // 如果有筛选条件，使用条件查询
            if (supplier != null || sampleModel != null || sampleName != null || 
                categoryDetail != null || tester != null || dqe != null) {
                data = projectTableService.getProjectTablesByCondition(
                        supplier, sampleModel, sampleName, categoryDetail, tester, dqe, page, size);
            } else {
                // 否则使用普通分页查询
                data = projectTableService.getProjectTablesByPage(page, size);
            }

            result.put("status", "success");
            result.put("data", data.get("data"));
            result.put("total", data.get("total"));
            result.put("page", data.get("page"));
            result.put("size", data.get("size"));
            result.put("totalPages", data.get("totalPages"));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "获取项目表格数据失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 保存项目表格数据
     * @param projectTable 项目表格数据
     * @return 保存结果
     */
    @PostMapping("/projectTable/saveProjectTable")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveProjectTable(@RequestBody ProjectTable projectTable) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = projectTableService.saveProjectTable(projectTable);
            if (success) {
                result.put("status", "success");
                result.put("message", "保存成功");
            } else {
                result.put("status", "error");
                result.put("message", "保存失败");
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "保存失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 批量保存项目表格数据
     * @param projectTables 项目表格数据列表
     * @return 保存结果
     */
    @PostMapping("/projectTable/batchSaveProjectTables")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> batchSaveProjectTables(@RequestBody List<ProjectTable> projectTables) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = projectTableService.batchSaveProjectTables(projectTables);
            if (success) {
                result.put("status", "success");
                result.put("message", "批量保存成功");
            } else {
                result.put("status", "error");
                result.put("message", "批量保存失败");
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "批量保存失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 删除项目表格数据
     * @param id 主键ID
     * @return 删除结果
     */
    @DeleteMapping("/projectTable/deleteProjectTable/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteProjectTable(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = projectTableService.deleteProjectTable(id);
            if (success) {
                result.put("status", "success");
                result.put("message", "删除成功");
            } else {
                result.put("status", "error");
                result.put("message", "删除失败");
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 导入项目表格数据
     * @param file Excel文件
     * @param username 用户名
     * @return 导入结果
     */
    @PostMapping("/projectTable/importProjectTableData")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> importProjectTableData(
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

            // 检查文件大小
            long fileSize = file.getSize();
            if (fileSize > 100 * 1024 * 1024) { // 100MB
                result.put("success", false);
                result.put("message", "文件过大，请上传小于100MB的文件");
                return ResponseEntity.badRequest().body(result);
            }

            System.out.println("开始处理项目表格文件: " + fileName + ", 大小: " + (fileSize / 1024 / 1024) + "MB");

            // 调用Service层处理导入
            ProjectTableService.ImportResult importResult = projectTableService.importProjectTableFromExcel(file, username);

            System.out.println("项目表格文件处理完成: " + importResult.getMessage());

            result.put("success", importResult.isSuccess());
            result.put("message", importResult.getMessage());
            result.put("insertCount", importResult.getInsertCount());
            result.put("updateCount", importResult.getUpdateCount());
            result.put("errorCount", importResult.getErrorCount());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("项目表格导入失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "导入失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 下载项目表格模板
     * @return 模板文件
     */
    @GetMapping("/projectTable/downloadTemplate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> downloadProjectTableTemplate() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            result.put("success", true);
            result.put("message", "模板文件准备就绪");
            result.put("downloadUrl", "/projectTable/downloadTemplateFile");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "模板文件生成失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 下载项目表格模板文件
     * @return 模板文件
     */
    @GetMapping("/projectTable/downloadTemplateFile")
    public ResponseEntity<Resource> downloadProjectTableTemplateFile() {
        try {
            // 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("项目表格模板");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            
            // 定义所有中文列名（按照数据库注释顺序）
            String[] columnNames = {
                "序号", "收样时间", "送样周别", "供应商", "送样次数", "样品型号", "小编码", "样品名称", "品类细分",
                "版本号", "测试编号", "需求耗时", "总数", "电气兼容性_样品数量", "高频_样品数量", "传导&射频_样品数量",
                "可靠性_样品数量", "环保_样品数量", "样品分类", "样品性质", "项目属性", "电气性能测试_计划开始时间",
                "电气性能测试_计划完成时间", "电气性能测试_实际开始时间", "电气性能测试_实际完成时间", "实际完成周",
                "电子工程师&DQE确认_开始时间", "电子工程师&DQE确认_完成时间", "测试时长(天)", "实际开始时间与收样时间之差(天)",
                "实际开始时间与计划开始时间之差(天)", "计划总耗时(天)", "实际总耗时(天)", "实际加班时数(天)",
                "问题确认总耗时(天)", "问题确认加班时数(天)", "验证次数", "测试人", "电子工程师", "项目工程师", "DQE",
                "DQE品类细分", "新分类", "加急/同步", "是否在计划内", "送样周期", "测试周期", "排队周期", "判定结果",
                "是否签样", "内外贸区分", "供应商前置", "送样备注", "问题定义", "S类_问题数量", "A类_问题数量",
                "B类_问题数量", "C类_问题数量", "D类_问题数量", "问题总数", "OPEN数量", "CLOSE数量"
            };
            
            // 设置列标题
            for (int i = 0; i < columnNames.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnNames[i]);
                
                // 设置标题样式
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerFont.setFontHeightInPoints((short) 12);
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setBorderBottom(BorderStyle.THIN);
                headerStyle.setBorderTop(BorderStyle.THIN);
                headerStyle.setBorderRight(BorderStyle.THIN);
                headerStyle.setBorderLeft(BorderStyle.THIN);
                cell.setCellStyle(headerStyle);
            }
            
            // 创建示例数据行
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("1");
            exampleRow.createCell(1).setCellValue("2024-01-15 09:00:00");
            exampleRow.createCell(2).setCellValue("2024W03");
            exampleRow.createCell(3).setCellValue("示例供应商");
            exampleRow.createCell(4).setCellValue("1");
            exampleRow.createCell(5).setCellValue("SAMPLE-001");
            exampleRow.createCell(6).setCellValue("SC001");
            exampleRow.createCell(7).setCellValue("示例样品");
            exampleRow.createCell(8).setCellValue("电子设备");
            exampleRow.createCell(9).setCellValue("V1.0");
            exampleRow.createCell(10).setCellValue("TEST-001");
            exampleRow.createCell(11).setCellValue("5.5");
            exampleRow.createCell(12).setCellValue("10");
            exampleRow.createCell(13).setCellValue("2");
            exampleRow.createCell(14).setCellValue("2");
            exampleRow.createCell(15).setCellValue("2");
            exampleRow.createCell(16).setCellValue("2");
            exampleRow.createCell(17).setCellValue("2");
            exampleRow.createCell(18).setCellValue("测试样品");
            exampleRow.createCell(19).setCellValue("正式样品");
            exampleRow.createCell(20).setCellValue("新项目");
            exampleRow.createCell(21).setCellValue("2024-01-16 09:00:00");
            exampleRow.createCell(22).setCellValue("2024-01-20 18:00:00");
            exampleRow.createCell(23).setCellValue("2024-01-16 09:30:00");
            exampleRow.createCell(24).setCellValue("2024-01-20 17:30:00");
            exampleRow.createCell(25).setCellValue("2024W03");
            exampleRow.createCell(26).setCellValue("2024-01-21 09:00:00");
            exampleRow.createCell(27).setCellValue("2024-01-21 18:00:00");
            exampleRow.createCell(28).setCellValue("4.5");
            exampleRow.createCell(29).setCellValue("0.5");
            exampleRow.createCell(30).setCellValue("0.5");
            exampleRow.createCell(31).setCellValue("5.0");
            exampleRow.createCell(32).setCellValue("4.5");
            exampleRow.createCell(33).setCellValue("0.0");
            exampleRow.createCell(34).setCellValue("1.0");
            exampleRow.createCell(35).setCellValue("0.0");
            exampleRow.createCell(36).setCellValue("1");
            exampleRow.createCell(37).setCellValue("张三");
            exampleRow.createCell(38).setCellValue("李四");
            exampleRow.createCell(39).setCellValue("王五");
            exampleRow.createCell(40).setCellValue("赵六");
            exampleRow.createCell(41).setCellValue("电子设备");
            exampleRow.createCell(42).setCellValue("新品类");
            exampleRow.createCell(43).setCellValue("加急");
            exampleRow.createCell(44).setCellValue("是");
            exampleRow.createCell(45).setCellValue("1.0");
            exampleRow.createCell(46).setCellValue("4.5");
            exampleRow.createCell(47).setCellValue("0.5");
            exampleRow.createCell(48).setCellValue("合格");
            exampleRow.createCell(49).setCellValue("是");
            exampleRow.createCell(50).setCellValue("内贸");
            exampleRow.createCell(51).setCellValue("前置");
            exampleRow.createCell(52).setCellValue("无");
            exampleRow.createCell(53).setCellValue("无");
            exampleRow.createCell(54).setCellValue("0");
            exampleRow.createCell(55).setCellValue("0");
            exampleRow.createCell(56).setCellValue("0");
            exampleRow.createCell(57).setCellValue("0");
            exampleRow.createCell(58).setCellValue("0");
            exampleRow.createCell(59).setCellValue("0");
            exampleRow.createCell(60).setCellValue("0");
            exampleRow.createCell(61).setCellValue("0");
            
            // 自动调整列宽
            for (int i = 0; i < columnNames.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 将工作簿转换为字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            byte[] bytes = outputStream.toByteArray();
            
            // 创建资源
            ByteArrayResource resource = new ByteArrayResource(bytes);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=项目表格导入模板.xlsx");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(bytes.length)
                    .body(resource);
                    
        } catch (Exception e) {
            System.err.println("生成项目表格模板失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 导出项目表格数据
     * @param request 导出请求
     * @return Excel文件
     */
    @PostMapping("/projectTable/exportProjectTableData")
    public ResponseEntity<Resource> exportProjectTableData(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) request.get("data");
            String fileName = (String) request.get("fileName");
            
            if (data == null || data.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // 创建工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("项目表格数据");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] columnHeaders = {
                "序号", "收样时间", "送样周别", "供应商", "送样次数", "样品型号", "小编码", "样品名称", "品类细分", "版本号", "测试编号",
                "需求耗时", "总数", "电气兼容性", "高频", "传导&射频", "可靠性", "环保", "样品分类", "样品性质", "项目属性",
                "电气测试_计划开始", "电气测试_计划完成", "电气测试_实际开始", "电气测试_实际完成", "实际完成周",
                "工程师确认_开始", "工程师确认_完成", "测试时长(天)", "实际开始vs收样(天)", "实际开始vs计划(天)",
                "计划总耗时(天)", "实际总耗时(天)", "实际加班(天)", "问题确认总耗时(天)", "问题确认加班(天)",
                "验证次数", "测试人", "电子工程师", "项目工程师", "DQE", "DQE品类细分", "新分类", "加急/同步", "是否在计划内",
                "送样周期", "测试周期", "排队周期", "判定结果", "是否签样", "内外贸区分", "供应商前置", "送样备注",
                "问题定义", "S类问题", "A类问题", "B类问题", "C类问题", "D类问题", "问题总数", "OPEN数量", "CLOSE数量"
            };

            // 设置标题样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < columnHeaders.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowNum = 1;
            for (Map<String, Object> item : data) {
                Row row = sheet.createRow(rowNum++);
                
                // 根据字段映射填充数据
                int colIndex = 0;
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("id")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("sampleReceiptTime")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("sampleWeek")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("supplier")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("sampleCount")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("sampleModel")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("smallCode")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("sampleName")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("categoryDetail")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("versionNumber")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("testNumber")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("requiredDuration")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("totalQuantity")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("electricalCompatibilityQuantity")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("highFrequencyQuantity")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("conductionRfQuantity")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("reliabilityQuantity")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("environmentalQuantity")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("sampleCategory")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("sampleNature")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("projectAttribute")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("electricalTestPlanStart")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("electricalTestPlanEnd")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("electricalTestActualStart")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("electricalTestActualEnd")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("actualCompletionWeek")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("engineerDqeConfirmStart")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("engineerDqeConfirmEnd")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("testDurationDays")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("actualStartVsReceiptDays")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("actualStartVsPlanStartDays")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("planTotalDurationDays")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("actualTotalDurationDays")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("actualOvertimeDays")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("problemConfirmTotalDurationDays")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("problemConfirmOvertimeDays")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("verificationCount")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("tester")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("electronicEngineer")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("projectEngineer")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("dqe")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("dqeCategoryDetail")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("newCategory")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("urgentSync")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("isInPlan")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("sampleCycle")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("testCycle")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("queueCycle")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("judgmentResult")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("isSigned")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("domesticForeign")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("supplierPreposition")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("sampleRemarks")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("problemDefinition")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("sClassProblemCount")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("aClassProblemCount")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("bClassProblemCount")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("cClassProblemCount")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("dClassProblemCount")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("totalProblemCount")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("openCount")));
                row.createCell(colIndex++).setCellValue(cleanValue(item.get("closeCount")));
            }

            // 自动调整列宽
            for (int i = 0; i < columnHeaders.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 转换为字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            byte[] bytes = outputStream.toByteArray();

            // 创建资源
            ByteArrayResource resource = new ByteArrayResource(bytes);
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(bytes.length)
                    .body(resource);
                    
        } catch (Exception e) {
            System.err.println("导出项目表格数据失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 清理字段值，将"未知"、"unknown"等特殊值转换为空字符串
     * @param value 原始值
     * @return 清理后的值
     */
    private String cleanValue(Object value) {
        if (value == null) {
            return "";
        }
        
        String stringValue = value.toString().trim();
        
        // 检查是否为特殊值
        if (stringValue.isEmpty() || 
            "未知".equals(stringValue) || 
            "unknown".equalsIgnoreCase(stringValue) ||
            "null".equalsIgnoreCase(stringValue) ||
            "undefined".equalsIgnoreCase(stringValue)) {
            return "";
        }
        
        return stringValue;
    }
}
