package com.lu.ddwyydemo04.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lu.ddwyydemo04.Service.TestManIndexService;
import com.lu.ddwyydemo04.pojo.ElectricScheduleInfo;
import com.lu.ddwyydemo04.pojo.PassbackData;

import com.lu.ddwyydemo04.pojo.TrashProject;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class TestEnvironmentController {
    private static final Logger logger = LoggerFactory.getLogger(testManIndexController.class);

    @Value("${file.storage.savepath}")
    private String jsonpath;

    @Autowired
    private TestManIndexService testManIndexService;

    @GetMapping("/loginTestEnvironment")
    public String loginTestEnvironment(){
        return "testEnvironment";
    }


    @PostMapping("/passback/receiveData")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> receiveData(@RequestBody List<PassbackData> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            testManIndexService.saveAll(requestData);
            response.put("status", "200");
            response.put("message", "全部数据接收成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // ✅ 打印详细错误日志供后台排查
            logger.error("数据保存失败，异常信息：", e);

            // ❌ 不将异常信息直接返回前端
            response.put("status", "fail");
            response.put("message", "数据接收失败，请联系管理员或稍后再试，报错消息："+e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }


    @GetMapping("/passback/getReceivedData")
    @ResponseBody
    public ResponseEntity<List<PassbackData>> getReceivedData() {
        List<PassbackData> receivedData =  testManIndexService.getReceivedData();
//        System.out.println("receivedData:"+receivedData);
        return ResponseEntity.ok(receivedData);
    }


    @GetMapping("/passback/getAllReceivedData")
    @ResponseBody
    public ResponseEntity<List<PassbackData>> getAllReceivedData(@RequestParam(required = false) String sample_id) {
        List<PassbackData> receivedData = testManIndexService.getAllReceivedData(sample_id);
        logger.info("/passback/getAllReceivedData 查询参数 sample_id = " + sample_id);
        return ResponseEntity.ok(receivedData);
    }

    @GetMapping("/passback/getPendingSampleData")
    @ResponseBody
    public ResponseEntity<List<PassbackData>> getPendingSampleData(@RequestParam(required = false) String category) {
        List<PassbackData> pendingSampleData = testManIndexService.getPendingSampleData(category);
//        logger.info("/passback/getPendingSampleData 查询参数 category = " + category);
        return ResponseEntity.ok(pendingSampleData);
    }


    @PostMapping("/passback/cancelData")
    @ResponseBody
    public ResponseEntity<String> cancelData(@RequestBody Map<String, String> request) {
        // 获取参数
        String sampleId = request.get("sample_id");
        String cancelReason = request.get("cancel_reason");
        String cancelBy = request.get("cancel_by");
        String cancelCode = request.get("cancel_code");
        String cancelDate = request.get("cancel_date");

        // 校验参数
        if (sampleId == null || sampleId.isEmpty()) {
            return ResponseEntity.badRequest().body("样品ID不能为空");
        }
        if (cancelReason == null || cancelReason.isEmpty()) {
            return ResponseEntity.badRequest().body("作废原因不能为空");
        }
        if (cancelBy == null || cancelBy.isEmpty()) {
            return ResponseEntity.badRequest().body("作废人不能为空");
        }
        if (cancelCode == null || cancelCode.isEmpty()) {
            return ResponseEntity.badRequest().body("作废人工号不能为空");
        }
        if (cancelDate == null || cancelDate.isEmpty()) {
            return ResponseEntity.badRequest().body("作废时间不能为空");
        }

        // 解析时间
        LocalDateTime parsedDate;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            parsedDate = LocalDateTime.parse(cancelDate, formatter);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("作废时间格式错误，应为 yyyy-MM-dd HH:mm:ss");
        }

        // 处理作废逻辑
        boolean success = processCancel(sampleId, cancelReason, cancelBy, cancelCode, parsedDate);

        if (success) {
            return ResponseEntity.ok("数据作废成功");
        } else {
            return ResponseEntity.badRequest().body("没有找到这个测试编号的数据，所以无法作废");
        }
    }

    private boolean processCancel(String sampleId, String cancelReason, String cancelBy, String cancelCode, LocalDateTime cancelDate) {
        // 这里添加实际的作废逻辑，例如数据库操作
        System.out.println("作废数据: sampleId=" + sampleId + ", cancelReason=" + cancelReason +
                ", cancelBy=" + cancelBy + ", cancelCode=" + cancelCode + ", cancelDate=" + cancelDate);

        int count = testManIndexService.queryElectricalCode(sampleId);
        if(count>0){
            boolean updateCancel = testManIndexService.cancelElectricalCode(sampleId,cancelReason,cancelBy,
                    cancelCode,cancelDate);
            System.out.println("updateCancel:"+updateCancel);
            return true;
        }else{
            return false;
        }

//        return true; // 假设作废成功
    }



    @GetMapping("/getSchedulesByStartDate")
    @ResponseBody
    public List<Map<String, Object>> getScheduleBoardWithTime(@RequestParam("startDate") String startDate,
                                                              @RequestParam("endDate") String endDate) {
//        System.out.println(startDate);
//        System.out.println(endDate);
        List<ElectricScheduleInfo> scheduleList;
        if (endDate == null || endDate.isEmpty()) {
            scheduleList = testManIndexService.getSchedulesByStartDate(startDate);
        } else {
            scheduleList = testManIndexService.getSchedulesByStartAndEndDate(startDate, endDate);
        }
//        System.out.println("scheduleList:"+scheduleList);


//        List<ElectricScheduleInfo> scheduleList = testManIndexService.getAllSchedules();

        List<String> electricInfoIds = scheduleList.stream()
                .map(ElectricScheduleInfo::getSample_id)
                .distinct()
                .collect(Collectors.toList());

//        System.out.println("electricInfoIds:"+electricInfoIds);
        // 如果electricInfoIds为空，直接返回空的结果列表
        if (electricInfoIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<PassbackData> passbackList = testManIndexService.getPassbackByElectricInfoIds(electricInfoIds);

        // 将 PassbackData 映射成 Map<Integer, PassbackData>
        Map<String, PassbackData> passbackMap = passbackList.stream()
                .filter(p -> p.getSample_id() != null && !p.getSample_id().isEmpty())
                .collect(Collectors.toMap(
                        PassbackData::getSample_id,
                        p -> p,
                        (existing, replacement) -> existing
                ));


        List<Map<String, Object>> result = new ArrayList<>();
        for (ElectricScheduleInfo schedule : scheduleList) {
            result.add(mergeScheduleAndPassback(schedule, passbackMap));
        }
//        System.out.println("result:"+result);

        return result;
    }

    private Map<String, Object> mergeScheduleAndPassback(ElectricScheduleInfo schedule, Map<String, PassbackData> passbackMap) {
        Map<String, Object> merged = new LinkedHashMap<>(); // 保证字段顺序一致
        String username = schedule.getTester();

        // 将 ElectricScheduleInfo 的属性写入 map
        merged.put("id", schedule.getId());
        merged.put("sample_id",  schedule.getSample_id());
        merged.put("tester", schedule.getTester());
        merged.put("schedule_start_date", schedule.getSchedule_start_date());
        merged.put("schedule_end_date", schedule.getSchedule_end_date());
        merged.put("create_time", schedule.getCreate_time());
        merged.put("update_time", schedule.getUpdate_time());
        merged.put("sizecoding", schedule.getSizecoding());

        //从user数据库里根据名字获取工号
        String job_number = testManIndexService.queryJobnumberFromUser(username);
        merged.put("job_number",job_number);

        // 合并对应的 PassbackData 字段
        PassbackData passback = passbackMap.get(schedule.getSample_id());
        if (passback != null) {
            merged.put("sample_id", passback.getSample_id());
            merged.put("sample_category", passback.getSample_category());
            merged.put("sample_model", passback.getSample_model());
            merged.put("materialCode", passback.getMaterialCode());
            merged.put("sample_frequency", passback.getSample_frequency());
            merged.put("sample_name", passback.getSample_name());
            merged.put("version", passback.getVersion());
            merged.put("priority", passback.getPriority());
            merged.put("sample_leader", passback.getSample_leader());
            merged.put("supplier", passback.getSupplier());
            merged.put("testProjectCategory", passback.getTestProjectCategory());
            merged.put("testProjects", passback.getTestProjects());
            merged.put("schedule", passback.getSchedule());
            merged.put("create_time", passback.getCreate_time()); // 避免与 schedule 的 create_time 重名
            merged.put("scheduleDays", passback.getScheduleDays());
            merged.put("isUsed", passback.getIsUsed());
            merged.put("schedule_color", passback.getSchedule_color());
            merged.put("remark", passback.getRemark());
        }

        return merged;
    }


//    @PostMapping("/passback/saveScheduleDays")
//    @ResponseBody
//    public Map<String, Object> saveScheduleDays(@RequestBody Map<String, Object> params) {
//        String sampleId = (String) params.get("sample_id");
//        String scheduleDays = (String) params.get("scheduleDays");
//
//        System.out.println("sample_id: " + sampleId + ", scheduleDays: " + scheduleDays);
//        Map<String, Object> result = new HashMap<>();
//        if(!Objects.equals(scheduleDays, "")){
//            int saveScheduleDays = testManIndexService.saveScheduleDays(sampleId, scheduleDays);
//            result.put("status", "ok");
//            result.put("message", "排期天数保存成功！");
//        }
//
//        return result;
//    }




    @PostMapping("/schedule/saveScheduleInfo")
    @ResponseBody
    public Map<String, Object> saveScheduleInfo(@RequestBody Map<String, Object> params) {
        String sampleId = (String) params.get("sample_id");
        String scheduleDays = (String) params.get("scheduleDays");
        String scheduleColor = (String) params.get("schedule_color");
        String remark = (String) params.get("remark");
        System.out.println("remark:"+remark);


        Map<String, Object> result = new HashMap<>();

        if (sampleId == null || sampleId.trim().isEmpty()) {
            result.put("status", "error");
            result.put("message", "缺少 sample_id 参数！");
            return result;
        }

        // 1️⃣ 保存排期天数
        if (scheduleDays != null && !scheduleDays.trim().isEmpty()) {
            try {
                int saveScheduleDays = testManIndexService.saveScheduleDays(sampleId, scheduleDays);

                result.put("scheduleDays", "ok");
            } catch (Exception e) {
                result.put("scheduleDaysStatus", "error");
                result.put("scheduleDaysError", "保存排期天数失败：" + e.getMessage());
            }
        }

        // 2️⃣ 保存排期颜色
        if (scheduleColor != null && !scheduleColor.trim().isEmpty()) {
            try {
                testManIndexService.updateElectricInfoColor(sampleId, scheduleColor);

                result.put("scheduleColor", scheduleColor.equals("null") ? "无色" : scheduleColor);
            } catch (Exception e) {
                result.put("scheduleColorStatus", "error");
                result.put("scheduleColorError", "保存颜色失败：" + e.getMessage());
            }
        }

        int updateRemark = testManIndexService.updateRemark(sampleId,remark);

        result.put("remark",remark);

//        if (remark != null && !remark.trim().isEmpty()) {
//            try{
//
//            }catch (Exception e){
//                result.put("remarkError","保存备注失败:"+e.getMessage());
//            }
//        }

        result.put("status", "ok");
        result.put("message", "排期信息保存成功！");
        return result;
    }



    @PostMapping("/scheduleBoard/saveSchedule")
    @ResponseBody
    public ResponseEntity<Map<String, String>> saveSchedule(@RequestBody List<Map<String, String>> scheduleChanges) {
        List<String> statusList = new ArrayList<>();
        if (scheduleChanges == null || scheduleChanges.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "没有需要保存的排期变更");
            return ResponseEntity.badRequest().body(response);
        }

        // 处理数据
        logger.info("收到的排期变更数据: " + scheduleChanges);

            // 遍历每条排期变更数据（每个 sample_id 只存在一条记录）
        for (Map<String, String> change : scheduleChanges) {
            String sampleId = change.get("sample_id");

            testManIndexService.processScheduleUpdate(sampleId, change); // 旧参数 changes 传 null 或者干脆改方法签名
        }


        // 遍历排期变更数据并发送 HTTP 请求
        RestTemplate restTemplate = new RestTemplate();
        String updateScheduleUrl = "https://www.ugreensmart.com/backend/ugreen-qc/Api/ElectricalTest/UpdateScheduleElectricalTest";

        for (Map<String, String> schedule : scheduleChanges) {
            Map<String, Object> requestBody = new HashMap<>();
            // 这里为什么判断delete，是为了告诉it那边接口说撤回排期，让对方更新
            if(schedule.get("change").equals("delete")){
                requestBody.put("ExpectedTestStartDate", "");
                requestBody.put("ExpectedTestEndDate", "");
                requestBody.put("TestLeaderName", "");
                requestBody.put("TestLeaderCode", "");
            }else{
                requestBody.put("ExpectedTestStartDate", schedule.get("start_date"));
                requestBody.put("ExpectedTestEndDate", schedule.get("end_date"));
                requestBody.put("TestLeaderName", schedule.get("tester"));
                requestBody.put("TestLeaderCode", schedule.get("job_number"));
            }
            requestBody.put("ETTestCode", schedule.get("sample_id"));

            System.out.println("requestBody:"+requestBody);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // IT那边需要认证
            headers.set("Authorization", "Basic MzUxMDpMaXVkaW5nMjAyMg==");

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            try {
                ResponseEntity<String> responseEntity = restTemplate.exchange(updateScheduleUrl, HttpMethod.POST, requestEntity, String.class);
                String body = responseEntity.getBody();

                // 解析 JSON 返回体
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(body);
                String apiStatus = jsonNode.path("staus").asText();
                String apiMsg = jsonNode.path("msg").asText();

                logger.info("更新排期发送数据成功: sample_id: " + schedule.get("sample_id") +
                        " -> 接口状态: " + apiStatus + "，消息: " + apiMsg);
                statusList.add("sample_id: " + schedule.get("sample_id") +
                        " -> 接口状态: " + apiStatus + "，消息: " + apiMsg);

            } catch (Exception e) {
                logger.info("更新排期发送数据失败: sample_id: " + schedule.get("sample_id") +
                        " -> 更新失败: " + e.getMessage());
                statusList.add("sample_id: " + schedule.get("sample_id") +
                        " -> 更新失败: " + e.getMessage());
            }
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "排期变更已成功保存，并已同步到 ElectricalTest 接口");
        response.put("statusSummary", String.join(" | ", statusList));
        return ResponseEntity.ok(response);
    }

    //更新排期的 模拟调试
    @PostMapping("/Api/ElectricalTest/UpdateScheduleElectricalTest")
    public ResponseEntity<Map<String, Object>> updateSchedule(@RequestBody Map<String, Object> requestData) {
//        System.out.println("收到的排期更新请求：" + requestData);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "成功接收排期变更");
        response.put("receivedData", requestData);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/passback/StartTestElectricalTest")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> startTest(@RequestBody Map<String, String> requestData) {
        String testNumber = requestData.get("test_number");
        String actual_time = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

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
            response.put("msg", "没有找到该测试编号，请输入正确的测试编号");
            return ResponseEntity.badRequest().body(response);
        }

        // 构造请求数据
        Map<String, String> requestPayload = new HashMap<>();
        requestPayload.put("ETTestCode", testNumber);
        requestPayload.put("ActualTestStartDate", actual_time);
        System.out.println("requestPayload:" + requestPayload);

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
            logger.info("远程接口响应状态码: {}", apiResponse.getStatusCode());
            logger.info("远程接口响应体: {}", apiResponse.getBody());

            return ResponseEntity.status(apiResponse.getStatusCode()).body(apiResponse.getBody());
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("staus", 500);
            errorResponse.put("msg", "调用远程接口失败: " + e.getMessage());
            logger.info("远程接口调用失败:"+e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }




    //开始测试  的模拟调试
    @PostMapping("/Api/ElectricalTest/StartTestElectricalTest")
    public ResponseEntity<Map<String, Object>> startTestMoni(@RequestBody Map<String, Object> requestData) {
        System.out.println("开始测试成功：" + requestData);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "成功开始测试");
        response.put("receivedData", requestData);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/passback/FinishTestElectricalTest")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> finishTest(@RequestBody Map<String, String> requestData) {
        String testNumber = requestData.get("test_number");
        String actualTestEndDate = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//        String actual_work_time = requestData.get("actual_work_time");

        if (testNumber == null || testNumber.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("staus", 400);
            response.put("msg", "测试编号不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        boolean updateFinishTime = testManIndexService.FinishTestElectricalTest(testNumber,actualTestEndDate);
        String actual_work_time = testManIndexService.queryActualWorkTime(testNumber);
        logger.info("结束测试的出来的实际工时为:"+actual_work_time);

        if (!updateFinishTime) {
            Map<String, Object> response = new HashMap<>();
            response.put("staus", 400);
            response.put("msg", "没有找到该测试编号，请输入正确的测试编号");
            return ResponseEntity.badRequest().body(response);
        }

        // 构造请求数据
        Map<String, String> requestPayload = new HashMap<>();
        requestPayload.put("ETTestCode", testNumber);
        requestPayload.put("ActualTestEndDate", actualTestEndDate);
        requestPayload.put("ActualTestWorkHour", actual_work_time);
        requestPayload.put("ActualTestWrokHour", actual_work_time);

        // 添加认证头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic MzUxMDpMaXVkaW5nMjAyMg==");

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestPayload, headers);


        // 发送 POST 请求到目标接口
        String targetUrl = "https://www.ugreensmart.com/backend/ugreen-qc/Api/ElectricalTest/FinishTestElectricalTest";
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<Map> apiResponse = restTemplate.exchange(targetUrl, HttpMethod.POST, requestEntity, Map.class);
            logger.info("远程接口响应状态码: {}", apiResponse.getStatusCode());
            logger.info("远程接口响应体: {}", apiResponse.getBody());

            return ResponseEntity.status(apiResponse.getStatusCode()).body(apiResponse.getBody());
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("staus", 500);
            errorResponse.put("msg", "调用远程接口失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

    }



    //结束测试  的模拟调试
    @PostMapping("/Api/ElectricalTest/FinishTestElectricalTest")
    public ResponseEntity<Map<String, Object>> finishTestMoni(@RequestBody Map<String, Object> requestData) {
        System.out.println("结束测试成功：" + requestData);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "成功结束测试");
        response.put("receivedData", requestData);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/passback/ProcessTestElectricalTest")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processTest(@RequestBody Map<String, String> requestData) {
        String testNumber = requestData.get("test_number");
//        String reportReviewTime = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))
//                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String reportReviewTime = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);  // 输出 2025-04-14T16:34:23

        String sampleRecognizeResult = requestData.get("sampleRecognizeResult");

        System.out.println("reportReviewTime:"+reportReviewTime);

        Map<String, Object> result = new HashMap<>();

        if (testNumber == null || testNumber.isEmpty()) {
            result.put("staus", 400);
            result.put("msg", "测试编号不能为空");
            return ResponseEntity.badRequest().body(result);
        }

        // 本地保存数据库状态
        testManIndexService.updateElectricInfoReview(testNumber, reportReviewTime, sampleRecognizeResult);

//        System.out.println("testNumber:"+testNumber);
        try {
            // 拼接文件路径
            String filePath = testManIndexService.queryElectricInfoFilepath(testNumber);
            File file = new File(filePath);

            // 日志输出文件信息
            System.out.println("准备上传文件路径：" + filePath);
            System.out.println("文件是否存在：" + file.exists());
            System.out.println("文件大小：" + file.length());

            if (!file.exists()) {
                result.put("staus", 404);
                result.put("msg", "未找到对应的文件: " + filePath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }

            // request JSON 字符串
            Map<String, String> jsonMap = new HashMap<>();
            jsonMap.put("ETTestCode", testNumber);
            jsonMap.put("ReportReviewTime", reportReviewTime);
            jsonMap.put("SampleRecognizeResult", sampleRecognizeResult);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(jsonMap);

            System.out.println("发送 JSON 参数：" + jsonString);

            // 构造 multipart/form-data 请求体
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("request", jsonString); // 不再用 HttpEntity 包裹
            body.add("fileList", new FileSystemResource(file));

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Basic MzUxMDpMaXVkaW5nMjAyMg==");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 发送请求
            String targetUrl = "https://www.ugreensmart.com/backend/ugreen-qc/Api/ElectricalTest/ReportReviewElectricalTest";
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map> response = restTemplate.exchange(targetUrl, HttpMethod.POST, requestEntity, Map.class);

            System.out.println("远程接口响应状态码：" + response.getStatusCode());
            System.out.println("远程接口响应体：" + response.getBody());

            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());

        } catch (Exception e) {
            e.printStackTrace(); // 打印异常堆栈
            result.put("staus", 500);
            result.put("msg",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    // 创建 request 字段的 text/plain Header
    private HttpHeaders createTextPartHeaders() {
        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(MediaType.APPLICATION_JSON);
        return partHeaders;
    }



    //结束测试  的模拟调试
    @PostMapping("/Api/ElectricalTest/ProcessTestElectricalTest")
    public ResponseEntity<Map<String, Object>> processTestMoni(@RequestBody Map<String, Object> requestData) {
        System.out.println("结报告审核成功：" + requestData);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "成功结束测试");
        response.put("receivedData", requestData);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/getTesters")
    public ResponseEntity<List<Map<String, Object>>> getTesters() {
        List<Map<String, Object>> testers = testManIndexService.getAllTesters();
//        System.out.println("testers:"+testers);
        return ResponseEntity.ok(testers);
    }

//    @PostMapping("/schedule/saveScheduleColor")
//    public ResponseEntity<String> saveScheduleColor(@RequestParam("color") String color,@RequestParam("sample_id") String sample_id) {
//        // 打印接收到的颜色
////        logger.info("接收到的颜色值为: " + color);
////        logger.info("接收到的sample_id值为: " + sample_id);
//
//        testManIndexService.updateElectricInfoColor(sample_id, color);
//
//        // 这里可以根据实际需求执行逻辑，比如保存颜色到某个排期记录
//        if(color.equals("null")){
//            color = "无色";
//        }
//
//        return ResponseEntity.ok(color);
//    }

    @PostMapping("/passback/uploadXlsx")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadReport(
            @RequestParam("testId") String testId,
            @RequestParam("file") MultipartFile file) {

        System.out.println("testId:"+testId);
        Map<String, Object> response = new HashMap<>();

        if (file.isEmpty()) {
            response.put("message", "文件为空");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // 确保保存路径存在
            File dir = new File(jsonpath);
            if (!dir.exists() && !dir.mkdirs()) {
                response.put("message", "保存路径创建失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            // 目标文件名：测试编号.xlsx
            String fileName = sanitizeFileName(testId) + ".xlsx";
            File destFile = new File(dir, fileName);

            // 保存文件
            file.transferTo(destFile);

            // ✅ 保存完整路径
            String fullPath = destFile.getAbsolutePath();
            testManIndexService.saveElectricInfoFilePath(testId, fullPath);

            response.put("message", "上传成功");
            response.put("savedPath", destFile.getAbsolutePath());
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            response.put("message", "文件保存失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private String sanitizeFileName(String input) {
        // 只过滤掉 Windows 文件名不允许的字符：\ / : * ? " < > |
        return input.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }

    @PostMapping("/scheduleBoard/addProjectCard")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addNewProject(@RequestBody Map<String, String> requestData) {
        String sampleName = requestData.get("sample_name");
        String sampleModel = requestData.get("sample_model");
        String materialCode = requestData.get("materialCode");
        String sampleLeader = requestData.get("sample_leader");
        String scheduleDaysStr = requestData.get("scheduleDays");
        String scheduleColor = requestData.get("schedule_color");
        String waitSample_classify = requestData.get("waitSample_classify");
        String remark = requestData.get("remark");

        BigDecimal scheduleDays = null;
        if (scheduleDaysStr != null && !scheduleDaysStr.trim().isEmpty()) {
            try {
                scheduleDays = new BigDecimal(scheduleDaysStr.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("无效的排期天数格式: " + scheduleDaysStr);
            }
        }

        System.out.println("sampleName: " + sampleName);
        System.out.println("sampleModel: " + sampleModel);
        System.out.println("materialCode: " + materialCode);
        System.out.println("sampleLeader: " + sampleLeader);
        System.out.println("scheduleDays: " + scheduleDays);
        System.out.println("scheduleColor: " + scheduleColor);
        System.out.println("remark: " + remark);

        // 电气编号用XZ+时间来命名：  XZ202506101500
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        String timestamp = LocalDateTime.now(ZoneId.of("Asia/Shanghai")).format(formatter);
        String sample_id = "XZ" + timestamp;

        PassbackData data = new PassbackData();
        data.setSample_id(sample_id);
        data.setSample_name(sampleName);
        data.setSample_model(sampleModel);
        data.setMaterialCode(materialCode);
        data.setSample_leader(sampleLeader);
        data.setScheduleDays(scheduleDays);
        data.setSchedule_color(scheduleColor);
        data.setWaitSample_classify(waitSample_classify);
        data.setRemark(remark);
        int addProjectCard = testManIndexService.insertElectricInfo(data);

        // 返回结果
        Map<String, Object> response = new HashMap<>();
        if(addProjectCard>0){
            response.put("success", true);
            response.put("message", "项目添加成功");
        }else{
            response.put("fail", true);
            response.put("message", "项目新增失败!");
        }

        return ResponseEntity.ok(response);
    }


    @PostMapping("/scheduleBoard/deleteProject")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteProject(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String sampleId = (String) request.get("sample_id");
            String cancel_reason = (String) request.get("cancel_reason");
            String username = (String) request.get("username");
            System.out.println("cancelReason:"+cancel_reason);

            // 作废人：
            String job_number = testManIndexService.queryJobnumberFromUser(username);
            System.out.println("job_number:"+job_number);

            LocalDateTime cancelDate = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));

            boolean updateCancel = testManIndexService.cancelElectricalCode(sampleId,cancel_reason,username,
                    job_number,cancelDate);
            if (updateCancel) {
                response.put("success", true);
                response.put("message", "项目删除成功");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", "未找到该项目");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除项目时出错");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/scheduleBoard/getTrashList")
    @ResponseBody
    public List<TrashProject> getTrashedProjects() {
        List<TrashProject> trashProjects = testManIndexService.getTrashedProjects();
        System.out.println("trashProjects:"+trashProjects);
        return trashProjects;
    }

    @PostMapping("/scheduleBoard/recoverProject")
    @ResponseBody
    public Map<String, Object> recoverProject(@RequestBody Map<String, Object> params) {
        String sampleId = (String) params.get("sample_id");

        // 调用服务层进行恢复操作
        boolean success = testManIndexService.recoverProjectById(sampleId);

        Map<String, Object> result = new HashMap<>();
        if (success) {
            result.put("message", "项目已成功恢复");
        } else {
            result.put("message", "恢复失败，请检查项目ID是否正确");
        }
        return result;
    }



}
