package com.lu.ddwyydemo04.controller;

import com.lu.ddwyydemo04.Service.NewProductProgressService;
import com.lu.ddwyydemo04.pojo.NewProductProgress;
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

/**
 * 新品进度管理控制器
 * 处理新品进度相关的页面跳转和API请求
 */
@Controller
public class NewProductProgressController {

    @Value("${file.storage.templatespath}")
    private String templatesPath;

    @Autowired
    private NewProductProgressService newProductProgressService;

    /**
     * 新品进度管理页面跳转
     * @return 新品进度管理页面视图
     */
    @GetMapping("/qualityProgress")
    public String qualityProgressPage() {
        return "qualityProgress";
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
            List<NewProductProgress> filteredData = allData.stream()
                .filter(item -> productCategory == null || productCategory.isEmpty() || 
                    (item.getProductCategoryLevel3() != null && item.getProductCategoryLevel3().toLowerCase().contains(productCategory.toLowerCase())))
                .filter(item -> priority == null || priority.isEmpty() || 
                    (item.getPriority() != null && item.getPriority().toLowerCase().contains(priority.toLowerCase())))
                .filter(item -> model == null || model.isEmpty() || 
                    (item.getModel() != null && item.getModel().toLowerCase().contains(model.toLowerCase())))
                .filter(item -> sku == null || sku.isEmpty() || 
                    (item.getSku() != null && item.getSku().toLowerCase().contains(sku.toLowerCase())))
                .filter(item -> stage == null || stage.isEmpty() || 
                    (item.getStage() != null && item.getStage().toLowerCase().contains(stage.toLowerCase())))
                .filter(item -> productName == null || productName.isEmpty() || 
                    (item.getProductName() != null && item.getProductName().toLowerCase().contains(productName.toLowerCase())))
                .filter(item -> leadDqe == null || leadDqe.isEmpty() || 
                    (item.getLeadDqe() != null && item.getLeadDqe().toLowerCase().contains(leadDqe.toLowerCase())))
                .filter(item -> electronicRd == null || electronicRd.isEmpty() || 
                    (item.getElectronicRd() != null && item.getElectronicRd().toLowerCase().contains(electronicRd.toLowerCase())))
                .filter(item -> status == null || status.isEmpty() || 
                    (item.getStatus() != null && item.getStatus().toLowerCase().contains(status.toLowerCase())))
                .filter(item -> testStartDate == null || testStartDate.isEmpty() || 
                    (item.getProjectStartTime() != null && item.getProjectStartTime().compareTo(testStartDate) >= 0))
                .filter(item -> testEndDate == null || testEndDate.isEmpty() || 
                    (item.getProjectStartTime() != null && item.getProjectStartTime().compareTo(testEndDate) <= 0))
                .collect(Collectors.toList());

//            System.out.println("filteredData:"+filteredData);

            // 分页处理
            int total = filteredData.size();
            int totalPages = (int) Math.ceil((double) total / size);
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, total);

            List<NewProductProgress> pageData;
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
            NewProductProgressService.ImportResult importResult = newProductProgressService.importNewProductProgressFromExcel(file);

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
}
