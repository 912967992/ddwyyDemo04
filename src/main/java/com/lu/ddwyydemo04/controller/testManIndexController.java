package com.lu.ddwyydemo04.controller;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.*;
import com.dingtalk.api.response.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lu.ddwyydemo04.Service.AccessTokenService;
import com.lu.ddwyydemo04.Service.DQE.DQEproblemMoudleService;
import com.lu.ddwyydemo04.Service.ExcelShowService;
import com.lu.ddwyydemo04.Service.TestManIndexService;
import com.lu.ddwyydemo04.Service.UserAccessLogService;
import com.lu.ddwyydemo04.exceptions.SessionTimeoutException;
import com.lu.ddwyydemo04.pojo.*;

import com.lu.ddwyydemo04.pojo.ElectricalTestItem;
import com.taobao.api.ApiException;
import com.taobao.api.internal.util.WebUtils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.*;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.*;

import java.io.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.*;

@Controller
public class testManIndexController {
    @Autowired
    private TestManIndexService testManIndexService;
    @Autowired
    private ExcelShowService excelShowService;

    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private DQEproblemMoudleService dqEproblemMoudleService;

    @Value("${dingtalk.agentid}")
    private String agentid;

    @Value("${file.storage.templatespath}")
    private String templatesPath;

    @Value("${file.storage.savepath}")
    private String savepath;

    @Value("${file.storage.jsonpath}")
    private String jsonpath;

    private static final Logger logger = LoggerFactory.getLogger(testManIndexController.class);

    // 上班时间和下班时间定义
    private static final LocalTime MORNING_START = LocalTime.of(9, 0);
    private static final LocalTime MORNING_END = LocalTime.of(12, 0);
    private static final LocalTime AFTERNOON_START = LocalTime.of(13, 30);
    private static final LocalTime AFTERNOON_END = LocalTime.of(18, 30);
    private static final double WORK_HOURS_PER_DAY = 8.5;


    @GetMapping("/testManIndex") // 处理页面跳转请求
    public String loginTestManIndex() {
        // 返回跳转页面的视图名称
        return "testManIndex";
    }

    @GetMapping("/returnBtn")
    public String returbBtn(String role){
        if(role.equals("tester")){
            return "testManIndex";
        }else{
            return "DQEIndex";
        }
    }

    @GetMapping("/labModuleTester") // 处理页面跳转请求
    public String loginlabModuleTester() {
        // 返回跳转页面的视图名称
        return "labModuleTester";
    }


    @GetMapping("/data") // 定义一个GET请求的接口，路径为 /data, 此方法是获取total逾期相关的
    @ResponseBody
    public Map<String, Integer> getData(@RequestParam(required = false) String username) {
        // 创建一个Map对象，存储测试中、待审核、已完成、总数、逾期和失责的数量
        Map<String, Integer> data = new HashMap<>();
        if(username == null){
            logger.info("searchSamples用户信息不存在");
            // 会话超时，抛出自定义异常
            throw new SessionTimeoutException("会话已超时，请重新登录。");
        }else{
            //如果未创建过报告文件，则需要在这里创建用户进total表，才有数据展示
            int countTotal = testManIndexService.queryCountTotal(username);
            if(countTotal==0){
                TotalData totalData = new TotalData(username,0,0,0,0,0,0);
                testManIndexService.createTotal(totalData);
                logger.info("createTotal创建用户进total成功");

            }
            // 更新 total 表中的数据
            testManIndexService.updateTotal(username);
        }
        //获取用户的total信息并返回到前端展示
        Map<String, Integer> data1 = testManIndexService.getindexPanel(username);
//        System.out.println(data1);

        data.put("testing", data1.get("testing")); // 测试中数量
        data.put("pending", data1.get("pending")); // 待审核1数量
        data.put("completed", data1.get("completed")); // 已完成数量
        data.put("total", data1.get("total")); // 总数
        data.put("overdue", data1.get("overdue")); // 逾期数量
        data.put("danger", data1.get("danger")); // 失责数量
        return data;
    }


    @GetMapping("/home/getFileCategories")
    @ResponseBody
    public List<String> getCategories() {
        File directory = new File(templatesPath);
        File[] files = directory.listFiles(File::isDirectory);
        System.out.println(templatesPath);

        List<String> categories = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                categories.add(file.getName());
            }
        }
        System.out.println(categories);
        return categories;
    }

    @GetMapping("/home/getFiles")
    @ResponseBody
    public List<FileData> getFiles(@RequestParam String category) {
        System.out.println("category:"+category);
        List<FileData> fileList = new ArrayList<>();

        // 拼接完整的文件夹路径
        String directoryPath = templatesPath + "/" + category;

        // 获取文件夹下的所有文件和文件夹
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();

        // 遍历文件夹下的所有文件和文件夹，并将它们添加到 fileList 中
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() || file.getName().endsWith(".xlsx")) {
                    fileList.add(new FileData(file.getName(), file.isDirectory() ? "directory" : "file"));
                }
            }
        }
        // 遍历并打印 fileList 中每个元素的文件名
        for (FileData data : fileList) {
            System.out.println("文件名: " + data.getName());
        }
        return fileList;
    }

    @GetMapping("/getTestManPanel")
    @ResponseBody
    public List<Samples> getTestManPanel(@RequestParam(required = false) String username){
        // 检查 username 参数是否传递
        if (username == null || username.isEmpty()) {
            // 处理用户信息不存在的情况，比如返回一个错误响应或重定向到登录页面
            return Collections.emptyList(); // 或者返回适当的错误响应
        } else {
            // 使用 username 查询数据
            logger.info(username+"getTestManPanel查询成功：");
//            System.out.println(testManIndexService.getTestManPanel(username));

            return testManIndexService.getTestManPanel(username);

        }

    }

    @PostMapping("/searchSamples")
    @ResponseBody
    public List<Samples> searchSamples(@RequestBody Map<String, Object> data){
        // 从会话中获取用户信息
        String username = (String) data.get("username");
        if (username == null) {
            // 处理用户信息不存在的情况，比如返回一个错误响应或重定向到登录页面
            logger.info("searchSamples用户信息不存在");
            // 会话超时，抛出自定义异常
            throw new SessionTimeoutException("会话已超时，请重新登录。");
        }

        String keyword = (String) data.get("keyword");
        String sort = (String) data.get("sortFilter");

        if(sort.equals("asc")){
            return testManIndexService.searchSamplesByAsc(username,keyword);
        }else if (sort.equals("desc")){
            return testManIndexService.searchSamplesByDesc(username,keyword);
        }else{
//            return testManIndexService.searchSamples(username,keyword);
            return testManIndexService.searchSamplesByDesc(username,keyword);
        }

    }

    @PostMapping("/testManIndex/getjumpFilepath")
    @ResponseBody
    public String getjumpFilepath(@RequestBody Map<String, Object> data){
        String sample_id = (String) data.get("sample_id");
        logger.info("获取的getjumpFilepath的sample_id为:"+sample_id);


//        String model = (String) data.get("model");
//        String coding = (String) data.get("coding");
//        String category = (String) data.get("category");
//        String version = (String) data.get("version");
//        String big_species = (String) data.get("big_species");
//        String small_species = (String) data.get("small_species");
//        String high_frequency = (String) data.get("high_frequency");
//        String questStats = (String) data.get("questStats");
//        String sample_frequencyStr = (String) data.get("sample_frequency");
//        Samples sample = new Samples();
//        sample.setSample_model(model);
//        sample.setSample_coding(coding);
//        sample.setSample_category(category);
//        sample.setVersion(version);
//        sample.setBig_species(big_species);
//        sample.setSmall_species(small_species);
//        sample.setHigh_frequency(high_frequency);
//        sample.setQuestStats(questStats);
//
//        int sample_frequency =  Integer.parseInt(sample_frequencyStr.trim());
//        sample.setSample_frequency(sample_frequency);

        return testManIndexService.queryFilepath(sample_id);
    }

    //替换报告
    @PostMapping("/testManIndex/replaceFilepath")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> replaceFile(@RequestParam("file") MultipartFile file,
                                         @RequestParam("filepath") String filePath) {
        Map<String, Object> response = new HashMap<>();
        System.out.println("filePath:"+filePath);
        try {
            // 创建目标文件对象
            File targetFile = new File(filePath);

            // 检查目标文件是否存在，如果存在则删除
            if (targetFile.exists()) {
                if (!targetFile.delete()) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            }

            // 将上传的文件保存到目标路径
            file.transferTo(targetFile);

            String fileName = targetFile.getName();
//            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            int dotIndex = fileName.lastIndexOf('.');
            String baseName;
            if (dotIndex > 0) { // 有扩展名
                baseName = fileName.substring(0, dotIndex);
            } else { // 没有扩展名
                baseName = fileName;
            }

            String jsonFilePath = jsonpath+ File.separator + baseName + ".json";
            System.out.println("json文件是："+jsonFilePath);

            try {
                File jsonFile = new File(jsonFilePath);
                if (jsonFile.exists()) {
                    if (jsonFile.delete()) {
                        logger.info("删除json文件了: " + jsonFilePath);
                    } else {
                        logger.error("删除json文件失败 " + jsonFilePath);
                    }
                } else {
                    logger.info("json文件没有找到 " + jsonFilePath);
                }
            } catch (Exception e) {
                logger.error("在替换报告进行到删除json文件的时候出错：.", e);
            }

            // 添加旧文件路径到响应中
            response.put("oldFilePath", filePath);
            response.put("message", "文件替换成功");
            logger.error("成功替换文件:"+filePath);
            // 返回成功响应
            return ResponseEntity.ok().body(response);

        } catch (IOException e) {
            // 捕获IO异常并返回错误响应
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/testManIndex/updateSamples")
    @ResponseBody
    public Map<String, Object> updateSamples(
            @RequestParam("edit_sample_id") String sample_id,
            @RequestParam("edit_model") String editModelStr,
            @RequestParam("edit_coding") String editCodingStr,
            @RequestParam("edit_category") String editCategory,
            @RequestParam("edit_questStats") String questStats,
            @RequestParam("edit_big_species") String big_species,
            @RequestParam("edit_small_species") String small_species,
            @RequestParam("edit_high_frequency") String high_frequency,
            @RequestParam("edit_sample_frequency") String editsample_frequency,

            @RequestParam("edit_version") String editVersion,
            @RequestParam("edit_sample_name") String editSampleName,
//            @RequestParam("edit_planfinish_time") String editPlanfinishTime,
//            @RequestParam("edit_scheduleStartTime") String edit_scheduleStartTime,
//            @RequestParam("edit_scheduleEndTime") String edit_scheduleEndTime,
            @RequestParam("edit_scheduleTestCycle") String edit_scheduleTestCycle,

            @RequestParam("edit_chip_control") String editChipControl,
            @RequestParam("edit_version_software") String editVersionSoftware,
            @RequestParam("edit_version_hardware") String editVersionHardware,
            @RequestParam("edit_supplier") String editsupplier,
            @RequestParam("edit_test_Overseas") String editTestOverseas,
            @RequestParam("edit_priority") String editpriority,
            @RequestParam("edit_sample_remark") String editsample_remark,
            @RequestParam("edit_sample_DQE") String editSampleDQE,
            @RequestParam("edit_sample_Developer") String editSampleDeveloper,
            @RequestParam("edit_sample_leader") String editSampleleader,
            @RequestParam("tester") String tester,
            @RequestParam("edit_electric_id") String edit_electric_id,
            @RequestParam(value = "passbackConfirm", required = false) String passbackConfirm,
            @RequestParam(value = "edit_cc_recipients", required = false) String edit_cc_recipients,
            @RequestParam(value = "edit_tester_teamwork", required = false) String edit_tester_teamwork
            ) {
        Map<String, Object> response = new HashMap<>();
        try {
            //查询数据库里的electric_sample_id值
            String originElectric_sample_id = testManIndexService.queryElectric_sample_id(sample_id);

            Samples sample = new Samples();

//            System.out.println("edit_electric_id:"+edit_electric_id);
            //先查询这个sample_id是否有electric_info表的真实id
            String testNumber = testManIndexService.queryElectricIdByActualId(sample_id);
            int isExist = testManIndexService.queryElectricalCode(edit_electric_id);

//            System.out.println("testNumber:"+testNumber);
//            System.out.println("isExist:"+isExist);
            if(isExist>0){
                sample.setElectric_sample_id(edit_electric_id);
                if(testNumber!=null){
                    //不为空，则
                    if(!testNumber.equals(edit_electric_id)){

                        //先删除旧的，再插入到新的
                        int delete = testManIndexService.removeTargetIdFromAllSampleActualIds(Integer.parseInt(sample_id));
                        if(delete>0){
                            logger.info("删除成功:"+sample_id);

                            // 假设 createTime 是 LocalDateTime
                            LocalDateTime createTime = testManIndexService.queryCreateTime(sample_id);

                            // 转成字符串
                            String createTimeStr = createTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                            int insert = testManIndexService.updateActualSampleId(edit_electric_id, sample_id,createTimeStr);
                            if(insert >0){

                                logger.info("先删后插入成功："+ edit_electric_id);
                            }else{
                                logger.info("先删插入失败"+edit_electric_id);
                            }
                        }
                    }
                }else{
                    //为空则直接插入
                    if (edit_electric_id != null && !edit_electric_id.trim().isEmpty()) {

                        // 假设 createTime 是 LocalDateTime
                        LocalDateTime createTime = testManIndexService.queryCreateTime(sample_id);

                        // 转成字符串
                        String createTimeStr = createTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));


                        int insert = testManIndexService.updateActualSampleId(edit_electric_id, sample_id,createTimeStr);
                        if(insert >0){
                            sample.setElectric_sample_id(edit_electric_id);
                            logger.info("修改信息这里的插入电气测试编号成功："+ edit_electric_id);
                        }else{
                            logger.info("插入失败"+edit_electric_id);
                        }
                    }
                }
            }else{
                if(testNumber!= null && !testNumber.trim().isEmpty()){
                    if (Objects.equals(edit_electric_id, "")) {
                        int delete = testManIndexService.removeTargetIdFromAllSampleActualIds(Integer.parseInt(sample_id));

                        logger.info("用户输入的电气编号为空！");

                        response.put("warning", "电气编号已经清空");
                        sample.setElectric_sample_id(null);
                    }else if(Objects.equals(edit_electric_id,originElectric_sample_id)){
                        sample.setElectric_sample_id(originElectric_sample_id);
                    }else{

                        logger.info("用户输入的电气编号不存在！"+edit_electric_id);

                        response.put("warning", "电气编号不存在，请仔细核对!");
                        sample.setElectric_sample_id(originElectric_sample_id);
                    }


                }else{
                    if (edit_electric_id != null && !edit_electric_id.trim().isEmpty()) {
                        logger.info("用户输入的电气编号不存在！"+edit_electric_id);

                        response.put("warning", "电气编号不存在，请仔细核对!");

                        sample.setElectric_sample_id(testNumber);
                    }
                }


            }


            // 调用服务类的方法来更新样品信息
            //这里加这两个是因为之前有bug，接收IT那边的时候有携带特殊符号
            String editModel = sanitizeFileName(editModelStr.trim());
            String editCoding = sanitizeFileName(editCodingStr.trim());

            sample.setSample_id(sample_id);
            sample.setSample_model(editModel);
            sample.setSample_coding(editCoding);
            sample.setSample_category(editCategory);
            sample.setVersion(editVersion);
            sample.setSample_name(editSampleName);
//            sample.setPlanfinish_time(editPlanfinishTime);
            sample.setChip_control(editChipControl);
            sample.setVersion_software(editVersionSoftware);
            sample.setVersion_hardware(editVersionHardware);
            sample.setSupplier(editsupplier);
            sample.setTest_Overseas(editTestOverseas); // 确保参数名一致
            sample.setPriority(editpriority);
            sample.setSample_remark(editsample_remark);
            sample.setSample_DQE(editSampleDQE.trim());
            sample.setSample_Developer(editSampleDeveloper.trim());
            sample.setSample_leader(editSampleleader.trim());
            sample.setTester(tester);
            
            // 处理合作测试人
            if (edit_tester_teamwork != null && !edit_tester_teamwork.trim().isEmpty()) {
                sample.setTester_teamwork(edit_tester_teamwork.trim());
            } else {
                sample.setTester_teamwork(null);
            }
            
            // 处理抄送者
            if (edit_cc_recipients != null && !edit_cc_recipients.trim().isEmpty()) {
                sample.setCc_recipients(edit_cc_recipients.trim());
            } else {
                sample.setCc_recipients(null);
            }

//            sample.setScheduleStartTime(edit_scheduleStartTime);
//            sample.setScheduleEndTime(edit_scheduleEndTime);
//            sample.setScheduleTestCycle(edit_scheduleTestCycle);

            int sample_frequency =  Integer.parseInt(editsample_frequency.trim());
            sample.setSample_frequency(sample_frequency);

            sample.setBig_species(big_species);
            sample.setSmall_species(small_species);
            sample.setHigh_frequency(high_frequency);

            sample.setQuestStats(questStats);
            
            // 处理问题点确认状态
            if (passbackConfirm != null && !passbackConfirm.trim().isEmpty()) {
                // 将字符串转换为布尔值
                boolean isConfirmed = "true".equalsIgnoreCase(passbackConfirm.trim());
                sample.setPassbackConfirm(isConfirmed ? "已回传" : "未回传");
                logger.info("设置问题点确认状态: 样品ID={}, 状态={}", sample_id, sample.getPassbackConfirm());
            }

//            LocalDateTime createTime =  testManIndexService.queryCreateTime(sample_id);

            // 使用ISO_LOCAL_DATE_TIME来解析带T的格式
//            LocalDateTime planfinishTime = LocalDateTime.parse(editPlanfinishTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
//            double planWorkDays = calculateWorkDays(createTime,planfinishTime,0);
            // 将其转换为 0.5 的倍数，向上取整,只算0.5的倍数
//            double adjustedWorkDays = Math.ceil(planWorkDays * 2) / 2;
//            System.out.println("adjustedWorkDays:"+adjustedWorkDays);;
//            sample.setPlanTestDuration(adjustedWorkDays);

            if (edit_scheduleTestCycle != null && !edit_scheduleTestCycle.isEmpty()) {
                try {
                    double planTestDuration = Double.parseDouble(edit_scheduleTestCycle);
                    if (planTestDuration != 0.0) {
                        sample.setPlanTestDuration(planTestDuration);
                    }
                    // 如果为 0.0，不执行任何操作
                } catch (NumberFormatException e) {
                    // 处理转换失败的情况，例如记录日志
                    logger.info("edit_scheduleTestCycle的值为空");
                }
            }


            //如果有更新产品名称则需要更新文件名
            String old_name = testManIndexService.querySample_name(sample_id);
            String oldFilePath = testManIndexService.queryFilepath(sample_id);
            String oldtester = testManIndexService.queryTester(sample_id); //已经添加questStats,20240709

//            System.out.println("old_name："+old_name);
//            System.out.println("oldtester："+oldtester);
//            System.out.println("oldFilePath："+oldFilePath);

            if(!Objects.equals(oldtester, tester)){
                response.put("message", "更换当前测试人");
            }

            String high_sign = "";
            sample.setFilepath(oldFilePath);
            if(!Objects.equals(old_name, editSampleName)){
                File oldFile = new File(oldFilePath);

                if(high_frequency.equals("是")){
                    high_sign = "高频_";
                }

                // 生成新的文件名字符串
                String newFileName = savepath.replace("/","\\") + "\\" +editModel + " " + editCoding + "_" + editCategory + "_" + editVersion + "_第" + editsample_frequency + "次送样_" + high_sign + questStats + "_" + editSampleName + ".xlsx";
                File newFile = new File(newFileName);
                logger.info("尝试重命名文件：oldFilePath=" + oldFilePath + ", newFileName=" + newFileName);

                try {
                    if (oldFile.renameTo(newFile)) {
                        sample.setFilepath(newFileName);
                        logger.info("updateSamples文件重命名成功" + oldFile + " -> " + newFileName);
                        response.put("rename","重命名成功");
                    } else {
                        throw new IOException("文件重命名失败" + oldFile + " -> " + newFileName);
                    }
                } catch (IOException e) {
                    logger.error("重命名文件时出错: " + e.getMessage());
                    response.put("status", "error");
                    response.put("message", "文件重命名失败，请确保文件未被占用并且路径正确: " + e.getMessage());
                    return response;
                }

            }

            //如果有更改当前测试人，则需要添加共同测试人
            // 如果用户手动设置了合作测试人，直接使用用户设置的值
            if (edit_tester_teamwork != null && !edit_tester_teamwork.trim().isEmpty()) {
                // 用户手动设置了合作测试人，直接使用 updateSample 保存
                testManIndexService.updateSample(sample);
            } else {
                // 用户没有手动设置，使用原有逻辑：如果当前测试者不在合作测试人列表中，则自动追加
                String tester_teamwork = testManIndexService.queryTester_teamwork(sample_id);
                boolean containsName = tester_teamwork != null && tester_teamwork.contains(tester);
                if(containsName){
                    testManIndexService.updateSample(sample);
                }else{
                    testManIndexService.updateSampleTeamWork(sample);
                    logger.info("添加共同测试人:"+tester);
                }
            }
            response.put("oldFilePath", oldFilePath);
            response.put("status", "success");
            response.put("message", "样品信息修改成功");
            logger.info("updateSamples样品信息更新成功:"+tester);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "样品信息更新失败: " + e.getMessage());
            logger.info("updateSamples样品信息更新失败:"+e.getMessage());
        }

        return response;
    }


    @PostMapping("/testManIndex/updatePassbackConfirm")
    @ResponseBody
    public Map<String, Object> updatePassbackConfirm(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String sampleId = (String) request.get("sample_id");
            Boolean passbackConfirm = (Boolean) request.get("passbackConfirm");
            if (sampleId == null || passbackConfirm == null) {
                response.put("status", "error");
                response.put("message", "参数不完整");
                return response;
            }

            String passbackConfirmStr = "未回传";
            if(passbackConfirm){
                passbackConfirmStr = "已回传";
            }
            
            // 验证sampleId是否为有效数字
            int sampleIdInt;
            try {
                sampleIdInt = Integer.parseInt(sampleId);
            } catch (NumberFormatException e) {
                response.put("status", "error");
                response.put("message", "样品ID格式无效: " + sampleId);
                logger.error("样品ID格式无效: {}", sampleId, e);
                return response;
            }
            
            // 调用服务层方法更新数据库
            int updateSamplePassbackConfirm = testManIndexService.updateSamplePassbackConfirm(sampleIdInt,passbackConfirmStr);
            
            if (updateSamplePassbackConfirm > 0) {
                // 如果选择已回传，发送通知给DQE和研发
                if (passbackConfirm) {
                    try {
                        // 查询样品详细信息
                        Samples sample = testManIndexService.getSampleById(sampleIdInt);
                        if (sample != null) {
                            // 构建通知消息
                            StringBuilder message = new StringBuilder();
                            message.append("【样品回传通知】\n");
                            message.append("样品ID: ").append(sample.getSample_id()).append("\n");
                            message.append("SKU: ").append(sample.getSample_model()).append(" ").append(sample.getSample_coding()).append("\n");
                            message.append("电气编号: ").append(sample.getElectric_sample_id() != null ? sample.getElectric_sample_id() : "无").append("\n");
                            message.append("产品名称: ").append(sample.getSample_name()).append("\n");
                            message.append("版本: ").append(sample.getVersion()).append("\n");
                            message.append("该样品已回传，请及时处理！");
                            
                            // 发送通知给DQE
                            if (sample.getSample_DQE() != null && !sample.getSample_DQE().trim().isEmpty()) {
                                String dqeUserId = accessTokenService.getUserIdByName(sample.getSample_DQE().trim());
                                if (dqeUserId != null && !dqeUserId.isEmpty()) {
                                    accessTokenService.sendTextNotification(dqeUserId, message.toString());
                                    logger.info("已发送样品回传通知给DQE: {}", sample.getSample_DQE());
                                } else {
                                    logger.warn("未找到DQE用户 {} 的userId", sample.getSample_DQE());
                                }
                            }
                            
                            // 发送通知给研发
                            if (sample.getSample_Developer() != null && !sample.getSample_Developer().trim().isEmpty()) {
                                String rdUserId = accessTokenService.getUserIdByName(sample.getSample_Developer().trim());
                                if (rdUserId != null && !rdUserId.isEmpty()) {
                                    accessTokenService.sendTextNotification(rdUserId, message.toString());
                                    logger.info("已发送样品回传通知给研发: {}", sample.getSample_Developer());
                                } else {
                                    logger.warn("未找到研发用户 {} 的userId", sample.getSample_Developer());
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("发送样品回传通知时出错: {}", e.getMessage(), e);
                        // 通知发送失败不影响主流程，继续执行
                    }
                }
                
                response.put("status", "success");
                response.put("message", "问题点确认状态更新成功");
//                logger.info("问题点确认状态更新成功，样品ID: {}, 状态: {}", sampleIdInt, passbackConfirm);
            } else {
                response.put("status", "error");
                response.put("message", "问题点确认状态更新失败");
                logger.warn("问题点确认状态更新失败，样品ID: {}", sampleIdInt);
            }
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "更新失败: " + e.getMessage());
            logger.error("更新问题点确认状态时出错: {}", e.getMessage(), e);
        }
        
        return response;
    }

    @PostMapping("/testManIndex/finishTest")
    @ResponseBody
    public Map<String, Object> finishTest(@RequestBody Map<String, String> request){

        Map<String, Object> response = new HashMap<>();
        String filepath = request.get("filepath");
        String model = request.get("model");
        String coding = request.get("coding");

        String schedule = request.get("schedule");
        String userInput = request.get("restDays");
        String sample_id = request.get("sample_id");

        String tuiOrJp = request.get("tuiOrJp");


        // 判断是否存在 restDays 参数
        double restDays = 0.0; // 默认值为 0
        if (userInput != null && !userInput.trim().isEmpty()) {
            // 存在输入值，尝试解析
            restDays = parseRestDays(userInput);
        } else {
            logger.info("未提供 restDays 参数，使用默认值 0.0");
        }

        if (schedule.equals("0")){
            if(tuiOrJp != null){
                if(tuiOrJp.equals("tui")){
                    schedule = "9";
                }else if(tuiOrJp.equals("jp")){
                    schedule = "10";
                }
            }else{
                schedule = "1";
            }


            // 设置完成时间为当前的北京时间
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = now.format(formatter);

            // 这里是提交报告时候抓取问题点的方法
            String problem = getProblemFromJson(filepath,model,coding);

            // 检查返回结果，如果不是"getProblem"则表示有错误
            if (!problem.equals("getProblem")) {
                response.put("status", "error");
                response.put("message", problem); // 返回错误信息（已汇总）
                logger.error("提交文件失败：" + filepath + "，" + problem);
                return response;
            }

            testManIndexService.finishTestWithoutTime(schedule,formattedDateTime,sample_id);

            LocalDateTime createTime =  testManIndexService.queryCreateTime(sample_id);
            String actual_time = String.valueOf(createTime);
//            System.out.println("createTime:"+actual_time);
//            LocalDateTime planFinishTime =  testManIndexService.queryPlanFinishTime(sample_id);

//            double planWorkDays = calculateWorkDays(createTime,planFinishTime,restDays);

            // 将其转换为 0.5 的倍数，向上取整,只算0.5的倍数
//            double adjustedWorkDays = Math.ceil(planWorkDays * 2) / 2;

            //因为预计完成时间字段现在去掉了，有可能为null。所以这里要用
            BigDecimal planTestDuration = testManIndexService.queryPlanFinishTime(sample_id);
            double adjustedWorkDays = (planTestDuration != null) ? planTestDuration.doubleValue() : 0.0;

//            double workDays = calculateWorkDays(createTime,now,restDays);
            double workDays =  calculateWorkHours(createTime, now, restDays);
            System.out.println("workDays:"+workDays);

            int setDuration = testManIndexService.setDuration(adjustedWorkDays,workDays,sample_id);
            if(setDuration==1){
                logger.info("提交文件："+filepath + ",测试时长："+workDays + ",预计完成时长："+adjustedWorkDays);
            }

            if (tuiOrJp != null && !tuiOrJp.trim().isEmpty()) {
                if (tuiOrJp.equals("tui")) {
                    response.put("message", "文件退样成功！您的报告预计测试时长为：" + adjustedWorkDays + " 天。实际测试时长为：" + workDays + "小时。");
                } else if (tuiOrJp.equals("jp")) {
                    response.put("message", "竞品文件完成，已通知发送对应角色！您的报告预计测试时长为：" + adjustedWorkDays + " 天。实际测试时长为：" + workDays + "小时。");
                } else {
                    response.put("message", "文件提交成功，接下来请审核！您的报告预计测试时长为：" + adjustedWorkDays + " 天。实际测试时长为：" + workDays + "小时。");
                }
            } else {
                // 如果 tuiOrJp 为空或 null
                response.put("message", "文件提交成功，接下来请审核！您的报告预计测试时长为：" + adjustedWorkDays + " 天。实际测试时长为：" + workDays + "小时。");
            }


            //20241111新增一个保存的时候统计好问题点数量并传到samples表里的problemNumber
            List<Map<String, Object>> countDefectLevel = dqEproblemMoudleService.countDefectLevelsBySampleId(sample_id);
            String problemCounts = dqEproblemMoudleService.formatDefectLevels(countDefectLevel);
            // problemCounts打印出来: S:2 A:1 B:0 C:1 待确定:1

            int updateProblemCounts = dqEproblemMoudleService.updatepProblemCounts(sample_id,problemCounts);
            if(updateProblemCounts>0){
                logger.info("updateProblemCounts更新成功:"+problemCounts);
                String testNumber = testManIndexService.queryElectricIdByActualId(sample_id);

                String workDaysStr = String.valueOf(workDays);

                // 提交之前先发送一次开始测试时间接口给IT，这里是为了让IT那边接口状态先改成测试中！
                postStartTestTime(testNumber,actual_time);

                Map<String, Object> remoteResult = testManIndexService.pushToRemoteElectricalFinish(testNumber, workDaysStr);
                int updateActualEndTime = testManIndexService.updateElectricActualEndTime(testNumber);
                logger.info("推送远程成功: {}", remoteResult.get("remoteBody"));
                if(tuiOrJp!=null){
                    if(tuiOrJp.equals("tui")){
                        Map<String, Object> processTestElectricalTest = testManIndexService.processTestElectricalTest(testNumber,"退样",filepath);
                        logger.info("推送退样远程成功: {}", processTestElectricalTest.get("remoteBody"));
                    }else if(tuiOrJp.equals("jp")){
                        Map<String, Object> processTestElectricalTest = testManIndexService.processTestElectricalTest(testNumber,"竞品完成",filepath);
                        logger.info("推送竞品完成远程成功: {}", processTestElectricalTest.get("remoteBody"));
                    }
                }


            }

        }else if(schedule.equals("1")){
            schedule = "0";
            //撤回的时候把之前提交的节点信息丢掉，不然超时还是会通知
            int deleteTaskNodeBefore = accessTokenService.deleteTaskNodeBefore(sample_id);
            if(deleteTaskNodeBefore>0){
                logger.info("删除"+sample_id+"之前的节点成功");
            }
            testManIndexService.finishTest(schedule,sample_id);
            response.put("message", "文件撤回成功，请重新提交！");
            logger.info("撤回审核成功："+filepath);
        }

        response.put("status", "success");


        return response;
    }


    private String getProblemFromJson(String filepath,String model,String coding){
        // 检查Excel文件是否存在
        File excelFile = new File(filepath);
        if (!excelFile.exists()) {
            return "需要进入页面查看问题点是否存在才可提交"; // 返回错误信息
        }

        try (InputStream inputStream = new FileInputStream(excelFile); 
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            // 查找包含"测试问题点汇总"的工作表（支持前后空格）
            Sheet sheet = null;
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                String sheetName = workbook.getSheetName(i);
                if (sheetName != null && sheetName.trim().equals("测试问题点汇总")) {
                    sheet = workbook.getSheetAt(i);
                    break;
                }
            }
            
            // 工作表不存在时立即返回
            if (sheet == null) {
                return "工作表'测试问题点汇总'不存在！提交失败，请检查。"; // 返回错误信息
            }

            // 读取表头
            List<String> headers = new ArrayList<>();
            Row headerRow = sheet.getRow(1); // 假设第二行是表头
            if (headerRow != null) {
                for (Cell cell : headerRow) {
                    headers.add(cell.toString().replaceAll("\\s+", "").replaceAll("\n", ""));
                }
            }

            // 定义需要的列名
            List<String> requiredColumns = Arrays.asList(
                    "样品型号", "样品阶段", "版本", "芯片方案", "日期", "测试人员", "测试平台",
                    "显示设备", "其他设备", "问题点", "问题类别", "问题视频或图片", "复现手法", "恢复方法",
                    "复现概率", "缺陷等级", "当前状态", "对比上一版或竞品",
                    "分析责任人", "改善后风险", "下一版回归测试", "备注",
                    "SKU","责任单位","DQE确认回复原因与对策（每个版本的回复请勿删除）","研发确认回复原因与对策（每个版本的回复请勿删除）","方案商","供应商","评审结论"
            );

            // 定义不需要判断的字段名
            Set<String> skipColumns = new HashSet<>(Arrays.asList("方案商"));

            // 检查缺少的列名
            Set<String> missingColumns = new HashSet<>();
            for (String column : requiredColumns) {
                if (!skipColumns.contains(column)) {
                    boolean found = false;
                    for (String header : headers) {
                        if (header.equals(column)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        missingColumns.add(column);
                    }
                }
            }

            // 收集所有验证错误
            List<String> validationErrors = new ArrayList<>();
            
            // 如果有缺少的列名，添加到错误列表
            if (!missingColumns.isEmpty()) {
                validationErrors.add("缺少列: " + String.join(", ", missingColumns));
            }

            // 读取数据行并进行验证
            List<Map<String, String>> filteredRows = new ArrayList<>();
            Iterator<Row> rowIterator = sheet.iterator();
            
            if (rowIterator.hasNext()) rowIterator.next(); // 跳过第一行
            if (rowIterator.hasNext()) rowIterator.next(); // 跳过表头行

            int rowNumber = 3; // 从第3行开始（跳过第一行和第二行表头）
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Map<String, String> rowMap = new LinkedHashMap<>();

                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    cell.setCellType(CellType.STRING);
                    rowMap.put(headers.get(i), cell.getStringCellValue().trim());
                }

                // 跳过空行
                boolean isEmpty = rowMap.values().stream().allMatch(String::isEmpty);
                if (isEmpty) {
                    rowNumber++;
                    continue;
                }

                // 检查问题点字段是否为空，如果为空则跳过该行
                if (isOnlySequenceNumberRow(rowMap)) {
                    rowNumber++;
                    continue; // 跳过问题点字段为空的行
                }

                // 检查问题类别的值是否为空或是否包含"-"符号
                String problemCategory = rowMap.get("问题类别");
                if (problemCategory == null || problemCategory.trim().isEmpty()) {
                    validationErrors.add("第" + rowNumber + "行：问题类别不能为空，请填写问题类别，问题类别的值同时必须包含'-'");
                } else if (!problemCategory.contains("-")) {
                    validationErrors.add("第" + rowNumber + "行：问题类别的值请按标准库有\"-\"来写，当前值：" + problemCategory);
                }

                // 检查复现概率不能为空
                String reproductionProbability = rowMap.get("复现概率");
                if (reproductionProbability == null || reproductionProbability.trim().isEmpty()) {
                    validationErrors.add("第" + rowNumber + "行：复现概率不能为空，请填写复现概率");
                }

                // 检查当前状态必须为open、close、follow up三个其中一个，兼容大小写和close、closed
                String currentStatus = rowMap.get("当前状态");
                if (currentStatus == null || currentStatus.trim().isEmpty()) {
                    validationErrors.add("第" + rowNumber + "行：当前状态不能为空，请填写当前状态");
                } else {
                    String normalizedStatus = currentStatus.trim().toLowerCase();
                    if (!normalizedStatus.equals("open") && 
                        !normalizedStatus.equals("close") && 
                        !normalizedStatus.equals("closed") && 
                        !normalizedStatus.equals("following up") &&
                        !normalizedStatus.equals("follow up")) {
                        validationErrors.add("第" + rowNumber + "行：当前状态必须为open、close、closed或follow up中的一个，当前值：" + currentStatus);
                    }
                }

                // 检查缺陷等级的值必须为S、A、B、C、D之一，要求必须为大写
                String defectLevel = rowMap.get("缺陷等级");
                if (defectLevel == null || defectLevel.trim().isEmpty()) {
                    validationErrors.add("第" + rowNumber + "行：缺陷等级不能为空，请填写缺陷等级");
                } else {
                    String normalizedDefectLevel = defectLevel.trim().toUpperCase();
                    if (!normalizedDefectLevel.equals("S") && 
                        !normalizedDefectLevel.equals("A") && 
                        !normalizedDefectLevel.equals("B") && 
                        !normalizedDefectLevel.equals("C") && 
                        !normalizedDefectLevel.equals("D")) {
                        validationErrors.add("第" + rowNumber + "行：缺陷等级必须为S、A、B、C、D中的一个（大写），当前值：" + defectLevel);
                    }
                }

                filteredRows.add(rowMap);
                rowNumber++;
            }
            
            // 如果有验证错误，汇总返回
            if (!validationErrors.isEmpty()) {
                return "验证失败，发现以下问题：\n" + String.join("\n", validationErrors);
            }

            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();
            // 定义格式化器，将 LocalDateTime 格式化为 yyyy-MM-dd HH:mm:ss
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            // 格式化当前时间为字符串
            String created_at = now.format(formatter);
            // 使用相同的格式化器解析字符串
            LocalDateTime parsedDateTime = LocalDateTime.parse(created_at, formatter);
            
            //要先获取samples的id主键，然后搜索历史版本id，没有的话就是0，有就加1
            int sample_id = testManIndexService.querySampleId(filepath);
            int history_id = testManIndexService.queryHistoryid(sample_id);

            for (Map<String, String> rowMap : filteredRows) {
                TestIssues testIssues = new TestIssues();

                String full_model = rowMap.get("样品型号");
                String sample_stage = rowMap.get("样品阶段");
                String issue_version = rowMap.get("版本");
                String chip_solution = rowMap.get("芯片方案");
                String problem_time = rowMap.get("日期"); //此数据是问题点工作表里的只填日期的数据，所以我设置数据库里字段是varchar(8)
                String tester = rowMap.get("测试人员");
                String test_platform = rowMap.get("测试平台");
                String test_device = rowMap.get("显示设备");
                String other_device = rowMap.get("其他设备");
                String problem = rowMap.get("问题点");
                String problemCategory = rowMap.get("问题类别");
                String problem_image_or_video = rowMap.get("问题视频或图片");
                String reproduction_method = rowMap.get("复现手法");
                String recovery_method = rowMap.get("恢复方法");
                String reproduction_probability = rowMap.get("复现概率");
                String defect_level = rowMap.get("缺陷等级");
                String current_status = rowMap.get("当前状态");
                String comparison_with_previous = rowMap.get("对比上一版或竞品");
                String responsible_person = rowMap.get("分析责任人");
                String post_improvement_risk = rowMap.get("改善后风险");
                String next_version_regression_test = rowMap.get("下一版回归测试");
                String remark = rowMap.get("备注");

                String sku = rowMap.get("SKU");
                String responsibleDepartment = rowMap.get("责任单位");
                String green_union_dqe = rowMap.get("DQE确认回复原因与对策（每个版本的回复请勿删除）");
                String green_union_rd = rowMap.get("研发确认回复原因与对策（每个版本的回复请勿删除）");
                String solution_provider = rowMap.get("方案商");
                String supplier = rowMap.get("供应商");
                String review_conclusion = rowMap.get("评审结论");

                String real_full_model = model + " "+ coding;
                testIssues.setFull_model(real_full_model); //因为完整编码测试人员填写的可能不一致，所以这里强制用数据库的编码就保证一致
                testIssues.setSample_stage(sample_stage);
                testIssues.setVersion(issue_version);
                testIssues.setChip_solution(chip_solution);
                testIssues.setProblem_time(problem_time);
                testIssues.setTester(tester);
                testIssues.setTest_platform(test_platform);
                testIssues.setTest_device(test_device);
                testIssues.setOther_device(other_device);
                testIssues.setProblem(problem);
                testIssues.setProblemCategory(problemCategory);
                testIssues.setProblem_image_or_video(problem_image_or_video);
                testIssues.setReproduction_method(reproduction_method);
                testIssues.setRecovery_method(recovery_method);
                testIssues.setReproduction_probability(reproduction_probability);
                testIssues.setDefect_level(defect_level);
                testIssues.setCurrent_status(current_status);
                testIssues.setComparison_with_previous(comparison_with_previous);
                testIssues.setResponsible_person(responsible_person);
                testIssues.setPost_improvement_risk(post_improvement_risk);
                testIssues.setNext_version_regression_test(next_version_regression_test);
                testIssues.setRemark(remark);

                // 20241230 按新模板新增6个字段:SKU ,责任单位，绿联DQE，绿联电子，方案商，供应商，评审结论
                testIssues.setSku(sku);
                testIssues.setResponsibleDepartment(responsibleDepartment);
                testIssues.setGreen_union_dqe(green_union_dqe);
                testIssues.setGreen_union_rd(green_union_rd);
                testIssues.setSolution_provider(solution_provider);
                testIssues.setSupplier(supplier);
                testIssues.setReview_conclusion(review_conclusion);

                String sampleidStr = String.valueOf(sample_id);
                testIssues.setCreated_at(parsedDateTime);
                testIssues.setSample_id(sampleidStr);
                testIssues.setHistory_id(history_id);

                int insertProblem = testManIndexService.insertTestIssues(testIssues);

                if(insertProblem > 0){
                    logger.info("问题点上传成功："+filepath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "文件读取失败: " + e.getMessage();
        }

        return "getProblem";
    }





    @PostMapping("/testManIndex/deleteSampleAndIssues")
    @ResponseBody
    public Map<String, Object> deleteFilepath(@RequestBody Map<String, String> request){
        Map<String, Object> response = new HashMap<>();
        String filepath = request.get("filepath");
        String sample_id = request.get("sample_id");
        int sampleId = Integer.parseInt(sample_id);

        logger.info("删除json文件sample_id："+sampleId);
        logger.info("删除json文件："+filepath);
        //删除文件夹里的文件,后续要备份则需要在这里增加备份到别的路径的处理
        try {
            // 创建文件路径的Path对象
            Path fileToDeletePath = Paths.get(filepath);
            // 删除文件
            Files.deleteIfExists(fileToDeletePath);

            // 如果需要备份，可以在这里添加备份的代码
            // backupFile(fileToDeletePath);
            int deleteJudge = testManIndexService.deleteFromTestIssues(sampleId);
            int deleteJudge2 = testManIndexService.deleteFromSamples(sampleId);
            int removeTargetId = testManIndexService.removeTargetIdFromAllSampleActualIds(sampleId);
            System.out.println("removeTargetIdL:"+removeTargetId);
            if(deleteJudge2==1){
                // 返回成功响应
                response.put("oldFilePath",filepath);
                response.put("status", "success");
                response.put("message", "删除文件成功");
                logger.info("deleteFilepath successfully filepath:"+filepath);
            }else{
                // 返回成功响应
                response.put("status", "error");
                response.put("message", "数据库删除文件失败");
                logger.info("数据库删除文件失败:"+filepath);
            }


        } catch (IOException e) {
            // IO异常处理
            e.printStackTrace();
            // 返回失败响应
            response.put("status", "error");
            response.put("message", "删除异常: " + e.getMessage());
            logger.info("deleteFilepath fail filepath:"+filepath+"e.getMessage():"+e.getMessage());
        }

        return response;
    }



    @PostMapping("/testManIndex/uploadFileToDingtalk")
    @ResponseBody
    public Map<String, String> uploadFileToDingtalk(@RequestParam("filepath") String filepath,
                                          @RequestParam("dirId") String dirId,
                                          @RequestParam("spaceId") String spaceId,
                                          @RequestParam("receiverId") String receiverId,
                                          @RequestParam("authCode") String authCode,
                                                    @RequestParam("username") String username) {
        Map<String, String> response = new HashMap<>();
        logger.info("Received request to uploadFileToDingtalk. filepath: {}, dirId: {}, spaceId: {}, receiverId: {}, authCode: {}",
                filepath, dirId, spaceId, receiverId, authCode);
        try {
            String accessToken = accessTokenService.getAccessToken();
            File file = new File(filepath);
            String filename = file.getName();

            String media_id = testManIndexService.getMediaId(filepath,accessToken,agentid);

            //上传文件到钉盘
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/cspace/add");
            OapiCspaceAddRequest req = new OapiCspaceAddRequest();
            req.setAgentId(agentid);
            req.setCode(authCode);
            req.setFolderId(dirId);
            req.setMediaId(media_id);
            req.setSpaceId(spaceId);
            req.setName(filename);
            req.setOverwrite(true);
            req.setHttpMethod("GET");
            OapiCspaceAddResponse rsp = client.execute(req, accessToken);
            //发送钉盘文件给用户
            sendDingFileToUser(accessToken,filename,media_id,receiverId);

            response.put("status", "发送成功");
            logger.info(filename+"文件导出成功，导出人："+username);
        } catch (ApiException e) {
            e.printStackTrace();
            response.put("status", "发送失败");
            logger.info("uploadFileToDingtalk fail.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }


    public void sendDingFileToUser(String accesstoken,String filename,String media_id,String userid) throws ApiException, IOException {
        OapiCspaceAddToSingleChatRequest request = new OapiCspaceAddToSingleChatRequest();
        request.setAgentId(agentid);
        request.setUserid(userid);
        request.setMediaId(media_id);
        request.setFileName(filename);
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/cspace/add_to_single_chat?"+WebUtils.buildQuery(request.getTextParams(),"utf-8"));
        OapiCspaceAddToSingleChatResponse response = client.execute(request, accesstoken);
        logger.info("sendDingFileToUser successfully."+response.getBody());
    }


    @PostMapping("/log/debug")
    @ResponseBody
    public String logDebug(@RequestBody Map<String, String> log) {
        String message = log.get("message");
        logger.info(message);
        return message;
    }

    @PostMapping("/testManIndex/clearJson")
    @ResponseBody
    public String clearJson(@RequestBody Map<String, String> data) {
        String oldFilePath = data.get("oldFilePath");
        if (oldFilePath == null || oldFilePath.trim().isEmpty()) {
            logger.error("oldFilePath is null or empty, cannot delete JSON file.");
            return "oldFilePath 为空，无法删除JSON文件";
        }

        // 获取文件名并去除扩展名
        File file = new File(oldFilePath);
        String fileName = file.getName();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        String jsonFilePath = jsonpath+ File.separator + baseName + ".json";

        try {
            File jsonFile = new File(jsonFilePath);
            if (jsonFile.exists()) {
                if (jsonFile.delete()) {
                    logger.info("删除json文件了: " + jsonFilePath);
                    return "json文件成功删除";
                } else {
                    logger.error("删除json文件失败 " + jsonFilePath);
                    return "删除json文件失败";
                }
            } else {
                logger.info("json文件没有找到 " + jsonFilePath);
                return "json文件没有找到";
            }
        } catch (Exception e) {
            logger.error("An error occurred while deleting the JSON file.", e);
            return "An error occurred while deleting the JSON file.";
        }
    }


    //计算测试时长testDuration
    // 计算上班时间内的小时数，并换算成工作天数
    // 计算工作天数，只计算工作时间段（早9到12点，下午1:30到6:30）
    // 计算工作天数，只计算工作时间段（早9到12点，下午1:30到6:30）
    public static double calculateWorkDays(LocalDateTime createTime, LocalDateTime finishTime, double restDays) {
        // 计算两个日期之间的总工作小时数
        double totalWorkHours = 0;
        LocalDateTime currentDateTime = createTime;

        while (currentDateTime.toLocalDate().isBefore(finishTime.toLocalDate()) || !currentDateTime.isAfter(finishTime)) {
            // 计算当天工作时间
            LocalDateTime endOfDay = currentDateTime.toLocalDate().atTime(18, 30);
            LocalDateTime startOfDay = currentDateTime.toLocalDate().atTime(9, 0);

            // 如果当天已经超过了完成时间，则调整结束时间
            if (finishTime.isBefore(endOfDay)) {
                endOfDay = finishTime;
            }

            // 计算早上时间段
            LocalDateTime endMorning = currentDateTime.toLocalDate().atTime(12, 0);
            LocalDateTime effectiveEndMorning = (finishTime.isBefore(endMorning) ? finishTime : endMorning);

            if (currentDateTime.isBefore(effectiveEndMorning)) {
                totalWorkHours += calculateTimeInPeriod(currentDateTime, effectiveEndMorning);
            }

            // 计算下午时间段
            LocalDateTime startAfternoon = currentDateTime.toLocalDate().atTime(13, 30);
            LocalDateTime effectiveStartAfternoon = (currentDateTime.isBefore(startAfternoon) ? startAfternoon : currentDateTime);
            LocalDateTime endAfternoon = currentDateTime.toLocalDate().atTime(18, 30);

            if (finishTime.isBefore(endAfternoon)) {
                endAfternoon = finishTime;
            }

            if (effectiveStartAfternoon.isBefore(endAfternoon)) {
                totalWorkHours += calculateTimeInPeriod(effectiveStartAfternoon, endAfternoon);
            }

            // 移动到第二天
            currentDateTime = currentDateTime.toLocalDate().plusDays(1).atStartOfDay().plusHours(9);
        }

        // 将总工作时间转换为工作天数
        double workDays = totalWorkHours / 8;

        // 减去休息天数
        workDays -= restDays;

        // 保留一位小数
        return Math.max(Math.round(workDays * 10) / 10.0, 0); // 确保返回值不小于0
    }


    //改成小时计算的上班工作制计算工时
    public static double calculateWorkHours(LocalDateTime createTime, LocalDateTime finishTime, double restDays) {
        double totalWorkHours = 0;
        LocalDateTime currentDateTime = createTime;

        while (currentDateTime.toLocalDate().isBefore(finishTime.toLocalDate()) || !currentDateTime.isAfter(finishTime)) {
            // 设置当天的上班时间段
            LocalDateTime startMorning = currentDateTime.toLocalDate().atTime(9, 0);
            LocalDateTime endMorning = currentDateTime.toLocalDate().atTime(12, 0);
            LocalDateTime startAfternoon = currentDateTime.toLocalDate().atTime(13, 30);
            LocalDateTime endAfternoon = currentDateTime.toLocalDate().atTime(18, 30);

            // 上午时间段计算
            if (currentDateTime.isBefore(endMorning) && finishTime.isAfter(startMorning)) {
                LocalDateTime start = currentDateTime.isAfter(startMorning) ? currentDateTime : startMorning;
                LocalDateTime end = finishTime.isBefore(endMorning) ? finishTime : endMorning;
                totalWorkHours += calculateTimeInPeriod(start, end);
            }

            // 下午时间段计算
            if (currentDateTime.isBefore(endAfternoon) && finishTime.isAfter(startAfternoon)) {
                LocalDateTime start = currentDateTime.isAfter(startAfternoon) ? currentDateTime : startAfternoon;
                LocalDateTime end = finishTime.isBefore(endAfternoon) ? finishTime : endAfternoon;
                totalWorkHours += calculateTimeInPeriod(start, end);
            }

            // 下一天的开始时间
            currentDateTime = currentDateTime.toLocalDate().plusDays(1).atTime(9, 0);
        }

        // 从小时数中减去休息天对应的小时数
        totalWorkHours -= restDays * 8;

        // 保留一位小数
        return Math.max(Math.round(totalWorkHours * 10) / 10.0, 0);
    }


    private static double calculateTimeInPeriod(LocalDateTime startTime, LocalDateTime endTime) {
        // 计算两个时间点之间的小时数
        long minutes = ChronoUnit.MINUTES.between(startTime, endTime);
        return Math.max(0, minutes / 60.0);
    }

    //将字符串转换为double类型
    public static double parseRestDays(String restDays) throws IllegalArgumentException {
        // 使用正则表达式来验证输入是否是有效的数字（整数或 0.5 的倍数）
        String regex = "^(0|[1-9][0-9]*)(\\.5)?$";
        if (!Pattern.matches(regex, restDays)) {
            throw new IllegalArgumentException("无效的休息天数输入！");
        }

        // 将输入字符串转换为 double
        return Double.parseDouble(restDays);
    }

    @GetMapping("/testManProfile") // 处理页面跳转请求
    public String loginTestManProfile(String role) {
        // 如果 role 为 null 或为空字符串，默认跳转到 "testManIndex"
        if (role == null || role.trim().isEmpty()) {
            return "testManProfile";
        }

        // 根据 role 的值返回不同的视图名称
        if (role.equals("tester")) {
            return "testManProfile";
        } else {
            return "DQE/DQEprofile";
        }

    }



    @PostMapping("/saveInterface")
    @ResponseBody
    public String saveInterface(@RequestParam String id, @RequestBody List<List<String>> interfaceData) {
        // 创建一个 StringBuilder 来构建最终的输出字符串
        StringBuilder interfaceInfo = new StringBuilder();
        System.out.println("id:" + id);

        // 遍历每一项数据并合并为 USB-3.0-2 的格式
        for (List<String> entry : interfaceData) {
            // 过滤掉全是空字符串的行
            if (entry.stream().allMatch(String::isEmpty)) {
                continue; // 跳过该行
            }

            // 将每行的数据通过 "-" 连接
            String formattedRow = String.join("-", entry);

            // 将每行添加到 StringBuilder，并添加换行符
            interfaceInfo.append(formattedRow).append("\n");
        }

        // 打印最终的格式化数据
        System.out.println("formattedData:\n" + interfaceInfo);
        testManIndexService.updateInterface(id, interfaceInfo.toString());

        return "{\"success\": true, \"message\": \"数据已接收\"}";
    }




    @GetMapping("/getInterfaceData")
    @ResponseBody
    public List<List<String>> getInterfaceData(int id) {
        String rawData = testManIndexService.getInterfaceData(id);

        if (rawData == null) {
            return new ArrayList<>();
        }

        // 替换特殊字段，避免被误拆分
        rawData = rawData.replace("USB-C", "USB_C");
        rawData = rawData.replace("USB-A", "USB_A");

        // 按行分割数据
        String[] rows = rawData.split("\n");

        List<List<String>> result = new ArrayList<>();

        for (String row : rows) {
            String[] parts = row.split("-");

            if (parts.length == 3) {
                // 把替换回原本的 USB-C 和 USB-A
                List<String> fixedParts = Arrays.stream(parts)
                        .map(s -> s.replace("USB_C", "USB-C").replace("USB_A", "USB-A"))
                        .collect(Collectors.toList()); // 注意这里改成了 .collect(Collectors.toList())

                result.add(fixedParts);
            }
        }

        return result;
    }

    @PostMapping("/labModuleTester/saveChanges")
    @ResponseBody
    public String saveSystemInfoChange(@RequestBody List<SystemInfo> updatedDevices) {
        try {
            System.out.println("updatedDevices: " + updatedDevices);
            testManIndexService.saveSystemInfoChange(updatedDevices);
            return "保存成功";
        } catch (Exception e) {
            e.printStackTrace();
            return "保存失败";
        }
    }

    @PostMapping("/labModuleTester/exportSystemInfo")
    @ResponseBody
    public ResponseEntity<byte[]> exportSystemInfo(@RequestBody List<Map<String, Object>> data) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("实验室数据");

            // 定义列标题 - 匹配新的字段结构
            String[] columns = {
                    "序号", "设备分类", "存放区域", "品牌", "设备名称", "型号（铭牌型号）", 
                    "接口类型&数量", "显卡接口类型&数量", "最高输出规格", "内建屏幕尺寸", 
                    "屏幕比例", "上市日期", "购买日期", "维修记录", "来源", "显卡来源"
            };

            // 创建样式（居中）
            CellStyle style = workbook.createCellStyle();
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            // 设置自动换行
            style.setWrapText(true);

            // 第一行添加说明
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("实验室设备信息数据表");
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            titleCell.setCellStyle(titleStyle);
            
            // 合并第一行的所有列
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, columns.length - 1));

            // 表头行 - 放在第二行（索引1）
            Row headerRow = sheet.createRow(1);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(style);
                
                // 设置列宽
                if (i == 0) { // 序号
                    sheet.setColumnWidth(i, 8 * 256);
                } else if (i == 1 || i == 2 || i == 4) { // 设备分类、存放区域、实验室名称
                    sheet.setColumnWidth(i, 15 * 256);
                } else if (i == 5) { // 型号（铭牌型号）
                    sheet.setColumnWidth(i, 20 * 256);
                } else if (i == 6 || i == 7) { // 接口类型&数量、显卡接口类型&数量
                    sheet.setColumnWidth(i, 25 * 256);
                } else {
                    sheet.setColumnWidth(i, 18 * 256);
                }
            }

            // 写入数据 - 从第三行开始（索引2）
            for (int i = 0; i < data.size(); i++) {
                Map<String, Object> item = data.get(i);
                Row row = sheet.createRow(i + 2);

                for (int j = 0; j < columns.length; j++) {
                    Cell cell = row.createCell(j);
                    Object value = null;

                    switch (j) {
                        case 0: value = item.get("id"); break;
                        case 1: value = item.get("deviceCategory"); break;
                        case 2: value = item.get("storageArea"); break;
                        case 3: value = item.get("brand"); break;
                        case 4: value = item.get("deviceName"); break;
                        case 5: value = item.get("modelNumber"); break;
                        case 6: value = item.get("interfaceTypeAndQuantity"); break;
                        case 7: value = item.get("graphicsInterfaceTypeAndQuantity"); break;
                        case 8: value = item.get("maxOutputSpec"); break;
                        case 9: value = item.get("screenSize"); break;
                        case 10: value = item.get("screenRatio"); break;
                        case 11: value = item.get("releaseDate"); break;
                        case 12: value = item.get("devicePurchaseDate"); break;
                        case 13: value = item.get("deviceRepairHistory"); break;
                        case 14: value = item.get("source"); break;
                        case 15: value = item.get("graphicsSource"); break;
                    }

                    // 处理换行符，确保在Excel中正确显示
                    String cellValue = value != null ? value.toString() : "";
                    if (cellValue.contains("\n")) {
                        cellValue = cellValue.replace("\n", "\n"); // 保持换行符
                    }
                    
                    cell.setCellValue(cellValue);
                    cell.setCellStyle(style);
                }
            }

            // 写入到输出流
            workbook.write(outputStream);
            byte[] bytes = outputStream.toByteArray();

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=实验室设备信息导出.xlsx");
            headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/labModuleTester/importSystemInfo")
    @ResponseBody
    public ResponseEntity<String> importSystemInfo(@RequestParam("file") MultipartFile file) {
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            // 获取"实验室数据"工作表
            Sheet sheet = workbook.getSheet("实验室数据");
            if (sheet == null) {
                return ResponseEntity.badRequest().body("未找到'实验室数据'工作表");
            }

            // 获取第二行作为列名（索引为1）
            Row headerRow = sheet.getRow(1);
            if (headerRow == null) {
                return ResponseEntity.badRequest().body("未找到列名行（第二行）");
            }

            // 创建列名到数据库字段的映射
            Map<String, String> columnMapping = new HashMap<>();
            columnMapping.put("序号", "id");
            columnMapping.put("设备分类", "deviceCategory");
            columnMapping.put("存放区域", "storageArea");
            columnMapping.put("品牌", "brand");
                    columnMapping.put("设备名称", "deviceName");
            columnMapping.put("型号（铭牌型号）", "modelNumber");
            columnMapping.put("接口类型&数量", "interfaceTypeAndQuantity");
            columnMapping.put("显卡接口类型&数量", "graphicsInterfaceTypeAndQuantity");
            columnMapping.put("最高输出规格", "maxOutputSpec");
            columnMapping.put("内建屏幕尺寸", "screenSize");
            columnMapping.put("屏幕比例", "screenRatio");
            columnMapping.put("上市日期", "releaseDate");
            columnMapping.put("购买日期", "devicePurchaseDate");
            columnMapping.put("维修记录", "deviceRepairHistory");
            columnMapping.put("来源", "source");
            columnMapping.put("显卡来源", "graphicsSource");

            // 读取列名并建立索引映射
            Map<Integer, String> columnIndexToField = new HashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String columnName = getCellStringValue(cell).trim();
                    if (columnMapping.containsKey(columnName)) {
                        columnIndexToField.put(i, columnMapping.get(columnName));
                    }
                }
            }

            // 从第三行开始读取数据（索引为2）
            List<SystemInfo> systemInfoList = new ArrayList<>();
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                SystemInfo info = new SystemInfo();
                boolean hasData = false;

                // 根据列索引映射读取数据
                for (Map.Entry<Integer, String> entry : columnIndexToField.entrySet()) {
                    int columnIndex = entry.getKey();
                    String fieldName = entry.getValue();
                    String cellValue = getCellStringValue(row.getCell(columnIndex));
                    
                    if (cellValue != null && !cellValue.trim().isEmpty()) {
                        hasData = true;
                    }

                    // 根据字段名设置对应的属性
                    switch (fieldName) {
                        case "id":
                            if (cellValue != null && !cellValue.trim().isEmpty()) {
                                try {
                                    info.setId(Integer.parseInt(cellValue.trim()));
                                } catch (NumberFormatException e) {
                                    // 忽略无效的ID
                                }
                            }
                            break;
                        case "deviceCategory":
                            info.setDeviceCategory(cellValue);
                            break;
                        case "storageArea":
                            info.setStorageArea(cellValue);
                            break;
                        case "brand":
                            info.setBrand(cellValue);
                            break;
                        case "deviceName":
                            info.setDeviceName(cellValue);
                            break;
                        case "modelNumber":
                            info.setModelNumber(cellValue);
                            break;
                        case "interfaceTypeAndQuantity":
                            info.setInterfaceTypeAndQuantity(cellValue);
                            break;
                        case "graphicsInterfaceTypeAndQuantity":
                            info.setGraphicsInterfaceTypeAndQuantity(cellValue);
                            break;
                        case "maxOutputSpec":
                            info.setMaxOutputSpec(cellValue);
                            break;
                        case "screenSize":
                            // 对于屏幕尺寸，直接获取原始单元格内容，不进行日期转换
                            info.setScreenSize(getCellRawValue(row.getCell(columnIndex)));
                            break;
                        case "screenRatio":
                            // 对于屏幕比例，直接获取原始单元格内容，不进行日期转换
                            info.setScreenRatio(getCellRawValue(row.getCell(columnIndex)));
                            break;
                        case "releaseDate":
                            info.setReleaseDate(cellValue);
                            break;
                        case "devicePurchaseDate":
                            info.setDevicePurchaseDate(cellValue);
                            break;
                        case "deviceRepairHistory":
                            info.setDeviceRepairHistory(cellValue);
                            break;
                        case "source":
                            info.setSource(cellValue);
                            break;
                        case "graphicsSource":
                            info.setGraphicsSource(cellValue);
                            break;
                    }
                }

                // 只有当行中有数据时才添加到列表
                if (hasData) {
                    systemInfoList.add(info);
                }
            }

            // 处理解析到的数据
            int successCount = 0;
            int updateCount = 0;
            int insertCount = 0;

            System.out.println("开始处理导入数据，共 " + systemInfoList.size() + " 条记录");

            for (SystemInfo info : systemInfoList) {
                        String deviceName = info.getDeviceName();
                        if (deviceName == null || deviceName.trim().isEmpty()) {
                            System.out.println("跳过空设备名称的记录");
                            continue;
                        }

                try {
                    int existing = testManIndexService.findByComputerName(deviceName);

                    if (existing > 0) {
                        testManIndexService.updateSystemInfoByXlsx(info);
                        updateCount++;
                        System.out.println("更新记录: " + deviceName);
                    } else {
                        testManIndexService.insertSystemInfoByXlsx(info);
                        insertCount++;
                        System.out.println("插入记录: " + deviceName);
                    }
                    successCount++;
                } catch (Exception e) {
                    System.err.println("处理记录失败: " + deviceName + ", 错误: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("导入完成 - 成功: " + successCount + ", 更新: " + updateCount + ", 新增: " + insertCount);

            return ResponseEntity.ok("文件导入成功！共处理 " + successCount + " 条记录（更新: " + updateCount + ", 新增: " + insertCount + "）");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("文件解析失败: " + e.getMessage());
        }
    }

//    private String getCellStringValue(Cell cell) {
//        if (cell == null) return "";
//        switch (cell.getCellType()) {
//            case STRING:
//                return cell.getStringCellValue().trim();
//            case NUMERIC:
//                if (DateUtil.isCellDateFormatted(cell)) {
//                    return cell.getLocalDateTimeCellValue().toString().replace("T", " ");
//                } else {
//                    return String.valueOf(cell.getNumericCellValue());
//                }
//            case BOOLEAN:
//                return String.valueOf(cell.getBooleanCellValue());
//            case FORMULA:
//                return cell.getCellFormula();
//            default:
//                return "";
//        }
//    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // 自定义日期格式：yyyy/M/d
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d");
                    return sdf.format(cell.getDateCellValue());
                } else {
                    // 普通数字
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d");
                        return sdf.format(cell.getDateCellValue());
                    } else {
                        return cell.getStringCellValue();
                    }
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    /**
     * 获取单元格的原始值，原封不动读取内容
     * 用于处理可能被Excel误识别为日期或时间的文本内容
     * 保留换行符等特殊字符
     */
    private String getCellRawValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                // 保留换行符，只去除首尾空白字符
                String stringValue = cell.getStringCellValue();
                // 只去除首尾空白，保留中间的换行符和空格
                return stringValue.replaceAll("^\\s+", "").replaceAll("\\s+$", "");
            case NUMERIC:
                // 检查是否被Excel识别为时间格式
                if (DateUtil.isCellDateFormatted(cell)) {
                    // 如果是时间格式，尝试按时间格式解析并保持原始格式
                    try {
                        // 获取时间值（以天为单位的小数）
                        double timeValue = cell.getNumericCellValue();
                        
                        // 将时间值转换回 HH:mm:ss 格式
                        int totalSeconds = (int) (timeValue * 24 * 60 * 60);
                        int hours = totalSeconds / 3600;
                        int minutes = (totalSeconds % 3600) / 60;
                        int seconds = totalSeconds % 60;
                        
                        return String.format("%d:%02d:%02d", hours, minutes, seconds);
                    } catch (Exception e) {
                        // 如果转换失败，返回原始数字
                        return String.valueOf(cell.getNumericCellValue());
                    }
                } else {
                    // 普通数字，直接转换为字符串
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
                try {
                    // 尝试获取公式计算结果，保留换行符
                    String formulaResult = cell.getStringCellValue();
                    return formulaResult.replaceAll("^\\s+", "").replaceAll("\\s+$", "");
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
            default:
                return "";
        }
    }


    @RequestMapping(value = "/labModuleTester/deleteSystemInfoByIds")
    @ResponseBody
    public ResponseEntity<String> deleteDataByIds(@RequestBody Map<String, List<Integer>> request) {
        List<Integer> ids = request.get("idRange");

        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().body("ID范围不能为空");
        }

        // 打印 ID 列表
        System.out.println("删除的 ID 列表: " + ids);

        try {
            // 删除操作
            testManIndexService.deleteSystemInfoById(ids);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("删除失败");
        }
    }

    @GetMapping("/api/getScheduleSampleIdByName")
    @ResponseBody
    public ResponseEntity<List<String>> getScheduleSampleIdByName(@RequestParam String username) {
        List<String> sampleIds = testManIndexService.getScheduleSampleIdByName(username);
//        System.out.println("username: " + username);
//        System.out.println("sampleIds: " + sampleIds);
        return ResponseEntity.ok(sampleIds);
    }


    @GetMapping("/api/project-details/{projectId}")
    public ResponseEntity<PassbackData> getElectricInfo(@PathVariable String projectId) {
        List<PassbackData> electricInfoList = testManIndexService.getElectricInfo(projectId);

        // 获取物料编码并拼接成字符串
        List<String> materialItems = testManIndexService.getMaterialCodes(projectId);
        String materialItemsStr = String.join("，", materialItems); // 可用逗号分隔也行

//        System.out.println("materialItems: " + materialItemsStr);

        if (electricInfoList != null && !electricInfoList.isEmpty()) {
            PassbackData data = electricInfoList.get(0);
            data.setMaterialCode(materialItemsStr); // 设置物料编码
//            System.out.println("data:"+data);
            return ResponseEntity.ok(data);
        } else {
            return ResponseEntity.notFound().build(); // 返回404
        }
    }


    @PostMapping("/api/start-test")
    @ResponseBody
    public ResponseEntity<?> startTest(@RequestBody Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();

        String questStats = (String) params.get("questStats");


        Map<String, Object> projectData = (Map<String, Object>) params.get("projectData");
//        System.out.println("projectData:"+projectData);
        if (projectData == null) {
            response.put("message", "缺少项目信息");
            response.put("success", false); // 可选：用于前端区分是否成功
            return ResponseEntity.ok(response);
        }
        String sample_actual_id = (String) projectData.get("sample_actual_id");
        String electric_sample_id = (String) projectData.get("sample_id");

        String category = (String) projectData.get("sample_type");
        String isHighFrequency = (String) projectData.get("high_frequency");


        int quantity = Integer.parseInt(projectData.get("sample_quantity").toString());

        // 20251129新增
        String apply_createdAt = (String) projectData.get("apply_createdAt");
        String demandFinishedTime = (String) projectData.get("demandFinishedTime");
        String sample_dqe = (String) projectData.get("sample_dqe");
        String sample_rd = (String) projectData.get("sample_rd");
        String sample_leader = (String) projectData.get("sample_leader");
        System.out.println("sample_leader:"+sample_leader);
        String reliability_quantity = (String) projectData.get("reliability_quantity");
        String envproction_quantity = (String) projectData.get("envproction_quantity");

        // 20250616 判断是否包含 "XZ"
        if (electric_sample_id != null && !electric_sample_id.isEmpty() && electric_sample_id.contains("XZ")) {
            response.put("message", "此任务为项目专员自建单号(电气编号非IT提单系统的)，请自行创建任务并查找准确的电气编号（如果IT系统没有说明是未提单，您可以先暂时搁置绑定电气编号这一步）");
            response.put("success", false); // 可选：用于前端区分是否成功
            return ResponseEntity.ok(response);
        }

//        System.out.println("electric_sample_id:"+electric_sample_id);
        if (sample_actual_id != null && !sample_actual_id.trim().isEmpty()) {
            response.put("message", "已经有这个版本信息的任务开始测试了，请检查是否写误");
            response.put("success", false); // 可选：用于前端区分是否成功
            return ResponseEntity.ok(response);
        }


        // 获取 materialCode 字段值
        String materialCodeObj = (String) projectData.get("materialCode");
        if (materialCodeObj != null) {
            String materialCodeStr = materialCodeObj.toString().trim();

            String model = (String) projectData.get("sample_model");
            // 清理文件名中的非法字符
            if (model != null) {
                model = sanitizeFileName(model.trim());
            }
            if (materialCodeStr != null) {
                materialCodeStr = sanitizeFileName(materialCodeStr);
            }
            //  it传过来的sample_category暂定是小类
            String small_species = (String) projectData.get("sample_category");
            String version = (String) projectData.get("version");

            String tester = (String) projectData.get("tester");
            String sample_name = (String) projectData.get("sample_name");

            String scheduleStartTune = (String) projectData.get("schedule_start_date");
            String scheduleEndTune = (String) projectData.get("schedule_end_date");
            
            // 修改时间格式：开始时间设置为10:00:00，结束时间设置为18:30:00
            if (scheduleStartTune != null && !scheduleStartTune.trim().isEmpty()) {
                // 处理包含T的ISO格式时间，提取日期部分，时间部分设置为10:00:00
                String datePart;
                if (scheduleStartTune.contains("T")) {
                    // 处理ISO格式：2025-07-23T00:00:00
                    datePart = scheduleStartTune.split("T")[0];
                } else {
                    // 处理普通格式：2025-07-23 00:00:00
                    String[] startParts = scheduleStartTune.split(" ");
                    datePart = startParts[0];
                }
                scheduleStartTune = datePart + " 10:00:00";
            }
            
            if (scheduleEndTune != null && !scheduleEndTune.trim().isEmpty()) {
                // 处理包含T的ISO格式时间，提取日期部分，时间部分设置为18:30:00
                String datePart;
                if (scheduleEndTune.contains("T")) {
                    // 处理ISO格式：2025-07-23T00:00:00
                    datePart = scheduleEndTune.split("T")[0];
                } else {
                    // 处理普通格式：2025-07-23 00:00:00
                    String[] endParts = scheduleEndTune.split(" ");
                    datePart = endParts[0];
                }
                scheduleEndTune = datePart + " 18:30:00";
            }
            String scheduleTestCycle = String.valueOf(projectData.get("scheduleDays"));

            // 判断是否包含多个，用逗号分隔
            String[] codeEntries = materialCodeStr.split("，|,"); // 支持中文逗号或英文逗号

            String cleanElectricSampleId = sanitizeFileName(electric_sample_id);
            // materialCodeStr 已经在上面清理过了，这里直接使用
            String cleanMaterialCode = materialCodeStr;


            for (String codeEntry : codeEntries) {
                System.out.println("codeEntry:"+codeEntry);
                String trimmed = codeEntry.trim(); // 去空格
                if (!trimmed.isEmpty()) {
                    // 分割成 STTestCode 和 sample_frequency
                    String[] parts = trimmed.split("=");
                    if (parts.length == 2) {
                        String materialCode = parts[0].trim();
                        int sampleFrequency = Integer.parseInt(parts[1].trim());

//                        excelShowService.sampleCount();
                        Samples sample = new Samples();
                        // 创建空 Excel 文件，使用清理后的 model 和 materialCode
//                        String fileName = electric_sample_id + "_" + materialCode + "_" + sampleFrequency+ ".xlsx";
//                        String fileName = cleanElectricSampleId + "_" + (model != null ? model : "") + "_" + cleanMaterialCode + "_" + sampleFrequency + ".xlsx";
                        String cleanMaterialCodeEntry = sanitizeFileName(materialCode);
                        String fileName = cleanElectricSampleId + "_"
                                + (model != null ? model : "") + "_"
                                + cleanMaterialCodeEntry + "_"
                                + sampleFrequency + ".xlsx";

                        String fileDir = savepath;
                        // 确保目录存在
                        File dir = new File(fileDir);
                        if (!dir.exists() && !dir.mkdirs()) {
                            logger.error("无法创建目录: " + fileDir);
                            response.put("message", "无法创建文件保存目录: " + fileDir);
                            response.put("success", false);
                            return ResponseEntity.ok(response);
                        }
                        
                        // 使用 File.separator 确保跨平台兼容性
                        String filePath = fileDir + File.separator + fileName;
                        System.out.println("filePath:"+filePath);

                        // 先检查是否已存在相同的测试任务
                        int count = excelShowService.sampleCount(model,materialCode,category,version,sampleFrequency,"",small_species,isHighFrequency,questStats);
//                        System.out.println("count:"+count);
                        if(count != 0){
                            response.put("message", "已经存在这个版本信息的测试任务了，不可以再创造重复的测试任务！");
                            response.put("success", false); // 可选：用于前端区分是否成功
                            return ResponseEntity.ok(response);
                        }

                        // 只有在 count == 0 时才创建文件
                        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                            workbook.createSheet("Sheet1");
                            try (FileOutputStream out = new FileOutputStream(filePath)) {
                                workbook.write(out);
                                    sample.setFilepath(filePath);

                                    sample.setSample_model(model);
                                    sample.setSample_coding(materialCode);
                                    sample.setSample_category(category);
                                    sample.setQuestStats(questStats);
//                                sample.setBig_species();
                                    sample.setSmall_species(small_species);
                                    sample.setVersion(version);
                                    sample.setTester(tester);
                                    sample.setSample_name(sample_name);
                                    sample.setScheduleStartTime(scheduleStartTune);
                                    sample.setScheduleEndTime(scheduleEndTune);
                                    sample.setScheduleTestCycle(scheduleTestCycle);
                                    sample.setFull_model(model+" "+materialCode);
                                    sample.setHigh_frequency(isHighFrequency);
                                    sample.setSample_quantity(quantity);
                                    sample.setSample_frequency(sampleFrequency);
                                    sample.setTester_teamwork(tester);

                                    //20251129新增
                                    sample.setSample_DQE(sample_dqe);
                                    sample.setSample_Developer(sample_rd);
                                    sample.setSample_leader(sample_leader);

                                    int insert = testManIndexService.insertSampleFromElectric(sample);
                                    if(insert==0){
                                        logger.info(fileName+"插入失败，请重新试一下或者联系管理员卢健！");
                                        return ResponseEntity.badRequest().body("插入失败，请重新试一下或者联系管理员卢健！");
                                    }else{
                                        String sampleId = sample.getSample_id(); // ✅ 这里能拿到自动生成的 samples表的sample_id
//                                        System.out.println("sampleId:"+sampleId);
                                        // 获取当前北京时间（Asia/Shanghai 时区就是北京时间）
                                        String actual_start_time = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
                                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                                        int insertActual = testManIndexService.updateActualSampleId(electric_sample_id,sampleId,actual_start_time);
                                        if(insertActual>0){
                                            logger.info("排期面板的id插入真实id成功："+sampleId+"成功插入到对应的electric_info表的"+electric_sample_id);

                                            int insertElectric_sample_id = testManIndexService.insertElectric_sample_id(electric_sample_id,sampleId);
                                            if(insertElectric_sample_id>0){
                                                logger.info("electric_sample_id插入成功："+sampleId);
                                            }
                                            String actual_time = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))
                                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                                            //发送实际开始测试时间给IT
                                            postStartTestTime(electric_sample_id,actual_time);
                                        }
                                    }
                            }
                        } catch (IOException e) {
                            logger.error("XLSX 文件创建失败，文件路径: " + filePath + ", 错误信息: " + e.getMessage(), e);
                            response.put("message", "XLSX 文件创建失败: " + e.getMessage());
                            response.put("success", false); // 可选：用于前端区分是否成功
                            return ResponseEntity.ok(response);
                        }

                    }
                }
            }
        }

        response.put("message", "测试任务提交成功");
        response.put("success", true); // 可选：用于前端区分是否成功
        return ResponseEntity.ok(response);
    }


    ResponseEntity<Map<String, Object>> postStartTestTime(String testNumber,String actual_time){

        if (testNumber == null || testNumber.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("staus", 400);
            response.put("msg", "测试编号不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        boolean updateStartTime = testManIndexService.StartTestElectricalTest(testNumber, actual_time);

        if (!updateStartTime) {
            Map<String, Object> response = new HashMap<>();
            response.put("staus", 400);
            response.put("msg", "发送测试开始时间没有找到该测试编号，请输入正确的测试编号");
            return ResponseEntity.badRequest().body(response);
        }

        // 构造请求数据
        Map<String, String> requestPayload = new HashMap<>();
        requestPayload.put("ETTestCode", testNumber);
        requestPayload.put("ActualTestStartDate", actual_time);
//        System.out.println("requestPayload:" + requestPayload);

        // 添加认证头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic MzUxMDpMaXVkaW5nMjAyMg==");

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestPayload, headers);

        // 发送 POST 请求到目标接口
        String targetUrl = "https://www.ugreensmart.com/backend/ugreen-qc/Api/ElectricalTest/StartTestElectricalTest";
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<Map> apiResponse = restTemplate.exchange(targetUrl, HttpMethod.POST, requestEntity, Map.class);
            logger.info("发送测试开始时间远程接口响应状态码: {}", apiResponse.getStatusCode());
            logger.info("发送测试开始时间远程接口响应体: {}", apiResponse.getBody());


            return ResponseEntity.status(apiResponse.getStatusCode()).body(apiResponse.getBody());
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("staus", 500);
            errorResponse.put("msg", testNumber+" 的发送测试开始时间调用远程接口失败: " + e.getMessage());
            logger.info("发送测试开始时间远程接口调用失败:"+e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/api/getChangeLog")
    @ResponseBody
    public List<Map<String, String>> getChangeLog(@RequestParam String sample_id) {
        List<ChangeRecord> changeRecords = testManIndexService.getChangeRecordsBySampleId(sample_id);
        System.out.println("sample_id:"+sample_id);
        System.out.println("changeRecords count:"+changeRecords.size());
        List<Map<String, String>> result = new ArrayList<>();

        if (changeRecords != null && !changeRecords.isEmpty()) {
            for (ChangeRecord record : changeRecords) {
                Map<String, String> map = new HashMap<>();
                map.put("tester", record.getTester() != null ? record.getTester() : "");
                map.put("startDate", record.getStart_date() != null ? record.getStart_date().toString() : "");
                map.put("endDate", record.getEnd_date() != null ? record.getEnd_date().toString() : "");
                map.put("updateTime", record.getUpdate_time() != null ? record.getUpdate_time().toString() : "");
                map.put("color", record.getSchedule_color() != null ? record.getSchedule_color() : "");
                map.put("used", record.getIs_used() != null ? record.getIs_used() : "");
                map.put("remark", record.getRemark() != null ? record.getRemark() : "");

                result.add(map);
            }
        }
        
        System.out.println("result:"+result);
        return result;
    }

    // 清洗非法字符
    public static String sanitizeFileName(String input) {
        return input.replaceAll("[\\\\/:*?\"<>|]", "_");
    }


    @GetMapping("/testManIndex/viewData")
    @ResponseBody
    public Map<String, String> viewData(@RequestParam String sample_id) {
        // 模拟两组 URL 数据（用逗号分隔）
        String admitUrls = testManIndexService.getProductApprovalDoc(sample_id);
        String devUrls = testManIndexService.getProductRequirement(sample_id);

        Map<String, String> result = new HashMap<>();
        result.put("admit", admitUrls);
        result.put("develop", devUrls);
        return result;
    }

    @GetMapping("/testManIndex/getTestItems")
    @ResponseBody
    public List<ElectricalTestItem> getTestItems(@RequestParam String sample_id) {
        return testManIndexService.getTestItemsBySampleId(sample_id);
    }


    private static final Map<String, String> fieldMapping = new HashMap<>();

    static {
        fieldMapping.put("样品型号", "full_model");
        fieldMapping.put("SKU", "sku");
        fieldMapping.put("样品阶段", "sample_stage");
        fieldMapping.put("版本", "version");
        fieldMapping.put("芯片方案", "chip_solution");
        fieldMapping.put("测试平台", "test_platform");
        fieldMapping.put("显示设备", "test_device");
        fieldMapping.put("其他设备", "other_device");
        fieldMapping.put("问题点", "problem");
        fieldMapping.put("问题类别", "problemCategory");
        fieldMapping.put("问题视频或图片", "problem_image_or_video");
        fieldMapping.put("日期", "problem_time"); // 报告日期
        fieldMapping.put("报告日期", "problem_time"); // 冗余处理
        fieldMapping.put("复现概率", "reproduction_probability");
        fieldMapping.put("复现手法", "reproduction_method");
        fieldMapping.put("恢复方法", "recovery_method");
        fieldMapping.put("缺陷等级", "defect_level");
        fieldMapping.put("当前状态", "current_status");
        fieldMapping.put("对比上一版或竞品", "comparison_with_previous");
        fieldMapping.put("测试人员", "tester");
        fieldMapping.put("DQE确认回复原因与对策（每个版本的回复请勿删除）", "green_union_dqe");
        fieldMapping.put("研发确认回复原因与对策（每个版本的回复请勿删除）", "green_union_rd");
        fieldMapping.put("DQE责任人", "dqe");
        fieldMapping.put("分析责任人", "responsible_person");
        fieldMapping.put("责任单位", "responsibleDepartment");
        fieldMapping.put("改善后风险", "post_improvement_risk");
        fieldMapping.put("评审结论", "review_conclusion");
        fieldMapping.put("下一版回归测试", "next_version_regression_test");
        fieldMapping.put("备注", "remark");
        fieldMapping.put("供应商", "supplier");
        fieldMapping.put("内/外贸", "test_Overseas");
    }

    @PostMapping("/testManIndex/passbackProblem")
    @ResponseBody
    public Map<String, Object> handlePassbackProblem(@RequestParam("excelFile") MultipartFile file,
                                                     @RequestParam("sample_id") int sampleId,
                                                     @RequestParam("job") String job) {
        Map<String, Object> result = new HashMap<>();

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            // 查找包含"测试问题点汇总"的工作表（支持前后空格）
            Sheet sheet = null;
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                String sheetName = workbook.getSheetName(i);
                if (sheetName != null && sheetName.trim().equals("测试问题点汇总")) {
                    sheet = workbook.getSheetAt(i);
                    break;
                }
            }
            
            if (sheet == null) {
                result.put("message", "未找到名称为 '测试问题点汇总' 的工作表（支持前后空格）");
                return result;
            }

            List<Map<String, String>> parsedData = new ArrayList<>();
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) rowIterator.next(); // 跳过第一行

            List<String> headers = new ArrayList<>();
            if (rowIterator.hasNext()) {
                Row headerRow = rowIterator.next();
                for (Cell cell : headerRow) {
                    headers.add(cell.toString().replaceAll("\\s+", "").replaceAll("\n", ""));
                }
            }
//            System.out.println("headers:"+headers);
            
            // 验证headers是否包含fieldMapping中的所有必需字段（排除"日期"和"报告日期"）
            Set<String> missingFields = new HashSet<>();
            Set<String> excludedFields = new HashSet<>(Arrays.asList("日期", "报告日期"));
            
            for (String requiredField : fieldMapping.keySet()) {
                // 跳过被排除的字段
                if (excludedFields.contains(requiredField)) {
                    continue;
                }
                
                boolean found = false;
                for (String header : headers) {
                    if (header.equals(requiredField)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    missingFields.add(requiredField);
                }
            }
            
            // 如果有缺少的字段，返回错误信息
            if (!missingFields.isEmpty()) {
                result.put("message", "Excel文件缺少以下必需列：" + String.join("、", missingFields) + "，请检查文件格式是否正确。");
                return result;
            }

            int history_id = testManIndexService.queryHistoryid(sampleId);
            Drawing<?> drawing = sheet.getDrawingPatriarch(); // 获取绘图对象
            String sheetName = sheet.getSheetName();

            int successCount = 0, failCount = 0;
            boolean hasValidIssues = false; // 标记是否有有效的问题点

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Map<String, String> rowMap = new LinkedHashMap<>();

                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    cell.setCellType(CellType.STRING);
                    rowMap.put(headers.get(i), cell.getStringCellValue().trim());
                }

                // 跳过空行
                boolean isEmpty = rowMap.values().stream().allMatch(String::isEmpty);
                if (isEmpty) continue;

                // 检查问题点字段是否为空，如果为空则跳过该行
                if (isOnlySequenceNumberRow(rowMap)) {
                    continue; // 跳过问题点字段为空的行
                }

                // 检查问题类别的值是否为空或是否包含"-"符号
                String problemCategory = rowMap.get("问题类别");
                if (problemCategory == null || problemCategory.trim().isEmpty()) {
                    result.put("message", "问题类别不能为空，请填写问题类别，问题类别的值同时必须包含'-'");
                    return result;
                }
                if (!problemCategory.contains("-")) {
                    result.put("message", "问题类别的值请按标准库有\"-\"来写，当前值：" + problemCategory);
                    return result;
                }

                // 检查复现概率不能为空
                String reproductionProbability = rowMap.get("复现概率");
                if (reproductionProbability == null || reproductionProbability.trim().isEmpty()) {
                    result.put("message", "复现概率不能为空，请填写复现概率");
                    return result;
                }

                // 检查当前状态必须为open、close、follow up三个其中一个，兼容大小写和close、closed
                String currentStatus = rowMap.get("当前状态");
                if (currentStatus == null || currentStatus.trim().isEmpty()) {
                    result.put("message", "当前状态不能为空，请填写当前状态");
                    return result;
                }
                String normalizedStatus = currentStatus.trim().toLowerCase();
                if (!normalizedStatus.equals("open") && 
                    !normalizedStatus.equals("close") && 
                    !normalizedStatus.equals("closed") && 
                    !normalizedStatus.equals("following up") &&
                    !normalizedStatus.equals("follow up")) {
                    result.put("message", "当前状态必须为open、close、closed或follow up中的一个，当前值：" + currentStatus);
                    return result;
                }

                // 检查缺陷等级的值必须为S、A、B、C、D之一，要求必须为大写
                String defectLevel = rowMap.get("缺陷等级");
                if (defectLevel == null || defectLevel.trim().isEmpty()) {
                    result.put("message", "缺陷等级不能为空，请填写缺陷等级");
                    return result;
                }
                String normalizedDefectLevel = defectLevel.trim().toUpperCase();
                if (!normalizedDefectLevel.equals("S") && 
                    !normalizedDefectLevel.equals("A") && 
                    !normalizedDefectLevel.equals("B") && 
                    !normalizedDefectLevel.equals("C") && 
                    !normalizedDefectLevel.equals("D")) {
                    result.put("message", "缺陷等级必须为S、A、B、C、D中的一个（大写），当前值：" + defectLevel);
                    return result;
                }

                // 检查DQE确认回复原因与对策不能为空
                String green_union_dqe = rowMap.get("DQE确认回复原因与对策（每个版本的回复请勿删除）");
                if (green_union_dqe == null || green_union_dqe.trim().isEmpty()) {
                    result.put("message", "DQE确认回复原因与对策不能为空，请填写DQE确认回复原因与对策");
                    return result;
                }

                // 检查研发确认回复原因与对策不能为空
                String green_union_rd = rowMap.get("研发确认回复原因与对策（每个版本的回复请勿删除）");
                if (green_union_rd == null || green_union_rd.trim().isEmpty()) {
                    result.put("message", "研发确认回复原因与对策不能为空，请填写研发确认回复原因与对策");
                    return result;
                }

                TestIssues issue = convertToTestIssue(rowMap, sampleId, history_id, job);
                String problem = issue.getProblem();
                if (problem == null || problem.trim().isEmpty()) continue;

                try {
                    // 插入并获取 ID（你要实现该方法并配置 MyBatis 的 useGeneratedKeys）
                    int insertedId = testManIndexService.insertTestIssues(issue);
//                    System.out.println("insertedId:"+insertedId);

                    if (insertedId > 0) {
                        successCount++;
                        hasValidIssues = true; // 标记有有效问题点

                        // 查找"问题视频或图片"列索引
                        int imageColIndex = -1;
                        for (int i = 0; i < headers.size(); i++) {
                            if (headers.get(i).contains("问题视频或图片")) {
                                imageColIndex = i;
                                break;
                            }
                        }

                        // 提取图片并保存
                        if (imageColIndex >= 0 && drawing != null) {
                            Cell imageCell = row.getCell(imageColIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            String imagePath = saveImageFromCell(
                                    imageCell,
                                    drawing,
                                    sheetName,            // 第三个参数 String sheetName
                                    row.getRowNum(),      // 第四个参数 int rowIndex
                                    imageColIndex,        // 第五个参数 int columnIndex
                                    String.valueOf(sampleId),  // 第六个参数 String filepath —— 这里随便传个字符串，主要用来构造图片文件名
                                    imageColIndex,        // 第七个参数 Integer finalCurrentCol
                                    issue.getId(),
                                    sampleId
                            );

                            // 更新数据库
                            if (imagePath != null && !imagePath.isEmpty()) {
                                int uploadImage = dqEproblemMoudleService.uploadImage(issue.getId(),imagePath);
//                                System.out.println("uploadImage:"+uploadImage);

                            }
                        }
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    System.err.println("处理行失败：" + e.getMessage());
                    failCount++;
                }
            }

            // 如果没有有效的问题点，插入一条默认的问题点记录
            if (!hasValidIssues) {
                try {
                    TestIssues defaultIssue = createDefaultIssue(sampleId, history_id, job);
                    int insertedId = testManIndexService.insertTestIssues(defaultIssue);
                    if (insertedId > 0) {
                        successCount++;
                        result.put("message", "文件解析完成，未发现有效问题点，已插入默认问题点记录。共处理 " + (successCount + failCount) + " 条，成功插入 " + successCount + " 条，失败 " + failCount + " 条");
                    } else {
                        failCount++;
                        result.put("message", "文件解析完成，未发现有效问题点，插入默认问题点失败。共处理 " + (successCount + failCount) + " 条，成功插入 " + successCount + " 条，失败 " + failCount + " 条");
                    }
                } catch (Exception e) {
                    failCount++;
                    result.put("message", "文件解析完成，未发现有效问题点，插入默认问题点时发生错误: " + e.getMessage() + "。共处理 " + (successCount + failCount) + " 条，成功插入 " + successCount + " 条，失败 " + failCount + " 条");
                }
            } else {
                result.put("message", "上传并解析成功，共处理 " + (successCount + failCount) + " 条，成功插入 " + successCount + " 条，失败 " + failCount + " 条");
            }
            String passbackConfirm = "已回传";

            // 回传成功后，这里默认帮electric_info的项目都打上勾，这样子DQE后续就可以直接签样
            int updateSamplePassbackConfirm = testManIndexService.updateSamplePassbackConfirm(sampleId,passbackConfirm);
            System.out.println("sssss");

        } catch (Exception e) {
            e.printStackTrace();
            result.put("message", "处理文件失败: " + e.getMessage());
        }

        return result;
    }

    public TestIssues convertToTestIssue(Map<String, String> rowData, Integer sampleId, Integer historyId, String job) {
        TestIssues issue = new TestIssues();

        for (Map.Entry<String, String> entry : rowData.entrySet()) {
            String rawKey = entry.getKey().replaceAll("\\s+", "").replaceAll("\n", "");
            String value = entry.getValue();

            String javaField = fieldMapping.get(rawKey);
            if (javaField == null) continue;

            try {
                Field field = TestIssues.class.getDeclaredField(javaField);
                field.setAccessible(true);

                if (field.getType() == String.class) {
                    field.set(issue, value);
                } else if (field.getType() == Integer.class || field.getType() == int.class) {
                    if (!value.isEmpty()) {
                        field.set(issue, Integer.parseInt(value));
                    }
                } else if (field.getType() == LocalDateTime.class) {
                    if (!value.isEmpty()) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[yyyy.MM.dd][yyyy-MM-dd]");
                        field.set(issue, LocalDate.parse(value, formatter).atStartOfDay());
                    }
                }
            } catch (Exception e) {
                System.out.println("字段赋值失败：" + javaField + " <- " + value);
            }
        }

        // 设置通用字段
        issue.setSample_id(sampleId.toString());
        issue.setHistory_id(historyId);
        issue.setCreated_at(LocalDateTime.now());
        issue.setCreated_by("tester");
        issue.setDqe_confirm("未确认");
        issue.setRd_confirm("未确认");
        issue.setCreated_by(job);
        issue.setDqe_confirm("确认");
        issue.setDqe_review_at(LocalDateTime.now());

        return issue;
    }

    /**
     * 创建默认的问题点记录
     * @param sampleId 样品ID
     * @param historyId 历史版本ID
     * @param job 操作人员
     * @return 默认的TestIssues对象
     */
    private TestIssues createDefaultIssue(Integer sampleId, Integer historyId, String job) {
        TestIssues defaultIssue = new TestIssues();
        
        // 设置基本信息
        defaultIssue.setSample_id(sampleId.toString());
        defaultIssue.setHistory_id(historyId);
        defaultIssue.setCreated_at(LocalDateTime.now());

        // 设置默认问题点
        defaultIssue.setProblem("本次回传无问题点，此条问题点用于迭代作用而已");

        defaultIssue.setTester("/");
        
        // 设置其他必要字段的默认值
        defaultIssue.setDqe_confirm("未确认");
        defaultIssue.setRd_confirm("未确认");
        defaultIssue.setDqe_review_at(LocalDateTime.now());
        
        // 设置当前时间作为问题时间
        defaultIssue.setProblem_time(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        return defaultIssue;
    }



    private String saveImageFromCell(Cell cell, Drawing<?> drawing, String sheetName, int rowIndex, int columnIndex, String filepath, Integer finalCurrentCol,Long insertedId ,int sampleId) {
        try {
            // 检查单元格是否包含图片
            for (Shape shape : drawing) {
                if (shape instanceof Picture) {
                    Picture picture = (Picture) shape;

                    // 确保 picture 和其相关的 anchor 不为 null
                    ClientAnchor anchor = picture.getClientAnchor();

                    if (anchor != null && finalCurrentCol == anchor.getCol1() && cell.getRowIndex() == anchor.getRow1()) {
                        // 增加对 picture.getPictureData() 的空指针检查
                        if (picture.getPictureData() != null) {
                            byte[] pictureData = picture.getPictureData().getData();

                            int lastIndex = filepath.lastIndexOf('\\');
                            String fileName = filepath.substring(lastIndex + 1);

                            // 生成图片文件名
                            String imageName = sampleId + "_" + insertedId + ".png";

                            // 检查并创建目录
                            if (!Files.exists(getImageLocationC())) {
                                Files.createDirectories(getImageLocationC());
                            }

                            // 保存图片到指定目录
                            Path imagePathC = getImageLocationC().resolve(imageName);
                            Files.write(imagePathC, pictureData);

                            // 返回图片文件路径
                            return "/issuespath/" + imageName;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // 处理空指针异常以确保继续执行
            e.printStackTrace();
        }
        return "";
    }


    private Path getImageLocationC(){
        return Paths.get(issuespath.replace("/","\\"));
    }

    @Value("${file.storage.issuespath}")
    private String issuespath;

    @GetMapping("/testScheduleEndTime")
    @ResponseBody
    public ResponseEntity<String> testScheduleEndTime() {
        try {
            accessTokenService.testScheduleEndTimeQuery();
            return ResponseEntity.ok("排期结束时间检查测试完成，请查看日志");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("测试失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/testFindAllValidSamples")
    @ResponseBody
    public ResponseEntity<String> testFindAllValidSamples() {
        try {
            accessTokenService.testFindAllValidSamples();
            return ResponseEntity.ok("findAllValidSamples测试完成，请查看日志");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("测试失败: " + e.getMessage());
        }
    }

    /**
     * 判断行是否为无效数据行（以问题点字段为准）
     * @param rowMap 行数据映射
     * @return true 如果问题点字段为空（无效行），false 如果问题点字段有值（有效行）
     */
    private boolean isOnlySequenceNumberRow(Map<String, String> rowMap) {
        // 优先检查"问题点"字段，以问题点字段为准判断是否为有效的问题点行
        String problemValue = rowMap.get("问题点");
        if (problemValue == null || problemValue.trim().isEmpty()) {
            return true; // 如果问题点字段为空，则认为无效，跳过该行
        }
        
        // 如果问题点字段有值，则认为是有效的问题点行
        return false;
    }

    /**
     * 手动触发查询部门 XX的子部门（分组）信息
     * 访问地址：/queryDeptUsers
     */
    @GetMapping("/queryDeptUsers")
    @ResponseBody
    public Map<String, Object> queryDeptUsers() {
        Map<String, Object> result = new HashMap<>();
        try {
//            logger.info("手动触发查询部门 63652303L 的子部门（分组）信息");
//            Long deptId = 63652303L;
            // 1055000000：品质管理平台     63652303 ：品质工程部          产品研发中心：90070106     1055223378：充电创意事业部
//            349996662：充电创意事业部-研发部-DC组                 // 595956188 ：阙兰所在的组
            Long deptId = 595956188L;
            accessTokenService.queryAndPrintDeptUsers(deptId);
            result.put("success", true);
            result.put("message", "查询完成，请查看日志获取详细信息");
            result.put("deptId", deptId);
        } catch (Exception e) {
            logger.error("手动查询部门子部门时发生错误: " + e.getMessage(), e);
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }

}
