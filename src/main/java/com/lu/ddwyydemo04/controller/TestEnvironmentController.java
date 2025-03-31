package com.lu.ddwyydemo04.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lu.ddwyydemo04.Service.ExcelShowService;
import com.lu.ddwyydemo04.Service.TestManIndexService;
import com.lu.ddwyydemo04.pojo.ElectricScheduleInfo;
import com.lu.ddwyydemo04.pojo.PassbackData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class TestEnvironmentController {
    private static final Logger logger = LoggerFactory.getLogger(testManIndexController.class);


    @Autowired
    private TestManIndexService testManIndexService;

    @GetMapping("/loginTestEnvironment")
    public String loginTestEnvironment(){
        return "testEnvironment";
    }


    @PostMapping("/passback/receiveData")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> receiveData(@RequestBody PassbackData requestData) throws JsonProcessingException {

        // 获取传入的请求数据
        String sampleId = requestData.getSample_id();
//        String sampleCategory = requestData.getSample_category();
//        String sampleModel = requestData.getSample_model();
//        String materialCode = requestData.getMaterialCode();
//        String sampleFrequency = requestData.getSample_frequency();
//        String sampleName = requestData.getSample_name();
//        String version = requestData.getVersion();
//        String priority = requestData.getPriority();
//        String sampleLeader = requestData.getSample_leader();
//        String supplier = requestData.getSupplier();
//        String testProjectCategory = requestData.getTestProjectCategory();
//        Object testProjects = requestData.getTestProjects();
//        String schedule = requestData.getSchedule();
//        int isUsed = requestData.getIsUsed();

//        logger.info("接收到IT系统传入的数据: {}", new ObjectMapper().writeValueAsString(requestData));


        // TODO: 这里你可以将数据保存到数据库
        int exist = testManIndexService.queryElectricalCode(sampleId);
        if (exist == 0) {
            int insertTestEnvir = testManIndexService.insertElectricInfo(requestData);
            if (insertTestEnvir > 0) {
                logger.info("接收到数据，插入electric_info库成功,电气编号为:" + sampleId);
            }
        } else {
            int updateTestEnvir = testManIndexService.updateElectricInfo(requestData);
            if (updateTestEnvir > 0) {
                logger.info("接收到数据，更新electric_info库成功,电气编号为:" + sampleId);
            }
        }

        // 调用sendFeedbackMessage，反馈给 IT 系统
        // 这里不再需要发送请求给外部接口了，只需要记录反馈信息
        String feedbackMessage = "研发质量管理系统接收数据成功";

        // 你可以记录反馈信息或做其他处理，避免发送 HTTP 请求到未知目标
        logger.info(feedbackMessage);

        // 构造返回报文
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "数据接收成功");

        Map<String, String> data = new HashMap<>();
        data.put("sample_id", sampleId);
        response.put("data", data);

        return ResponseEntity.ok(response);  // 返回给 IT 接口 200 状态码
    }


    @GetMapping("/passback/getReceivedData")
    @ResponseBody
    public ResponseEntity<List<PassbackData>> getReceivedData() {
        List<PassbackData> receivedData =  testManIndexService.getReceivedData();
        logger.info("/passback/getReceivedData接收到的数据是："+receivedData.toString());
        return ResponseEntity.ok(receivedData);
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


    @GetMapping("/getScheduleBoard")
    @ResponseBody
    public List<Map<String, Object>> getScheduleBoard() {
        List<ElectricScheduleInfo> scheduleList = testManIndexService.getAllSchedules();
    //    System.out.println("testManIndexService.getAllSchedules(): " + scheduleList);

        List<Integer> electricInfoIds = scheduleList.stream()
                .map(ElectricScheduleInfo::getElectric_info_id)
                .distinct()
                .collect(Collectors.toList());

        List<PassbackData> passbackList = testManIndexService.getPassbackByElectricInfoIds(electricInfoIds);

        // 将 PassbackData 映射成 Map<Integer, PassbackData>
        Map<Integer, PassbackData> passbackMap = passbackList.stream()
                .filter(p -> p.getSample_id() != null)
                .collect(Collectors.toMap(p -> Integer.parseInt(p.getSample_id()), p -> p));

        List<Map<String, Object>> result = new ArrayList<>();

        for (ElectricScheduleInfo schedule : scheduleList) {
            Map<String, Object> merged = new LinkedHashMap<>(); // 保证字段顺序一致

            // 将 ElectricScheduleInfo 的属性写入 map
            merged.put("id", schedule.getId());
            merged.put("electric_info_id", schedule.getElectric_info_id());
            merged.put("tester", schedule.getTester());
            merged.put("schedule_start_date", schedule.getSchedule_start_date());
            merged.put("schedule_end_date", schedule.getSchedule_end_date());
            merged.put("row_index", schedule.getRow_index());
            merged.put("column_index", schedule.getColumn_index());
            merged.put("create_time", schedule.getCreate_time());
            merged.put("update_time", schedule.getUpdate_time());
            merged.put("sizecoding", schedule.getSizecoding());

            // 合并对应的 PassbackData 字段
            PassbackData passback = passbackMap.get(schedule.getElectric_info_id());
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
            }

            result.add(merged);
        }
    //    System.out.println("result:"+result);
        return result;
    }

    @GetMapping("/getSchedulesByStartDate")
    @ResponseBody
    public List<Map<String, Object>> getScheduleBoardWithTime(@RequestParam("startDate") String startDate,
                                                              @RequestParam("endDate") String endDate) {
        System.out.println(startDate);
        System.out.println(endDate);
        List<ElectricScheduleInfo> scheduleList;
        if (endDate == null || endDate.isEmpty()) {
            scheduleList = testManIndexService.getSchedulesByStartDate(startDate);
        } else {
            scheduleList = testManIndexService.getSchedulesByStartAndEndDate(startDate, endDate);
        }


//        List<ElectricScheduleInfo> scheduleList = testManIndexService.getAllSchedules();

        List<Integer> electricInfoIds = scheduleList.stream()
                .map(ElectricScheduleInfo::getElectric_info_id)
                .distinct()
                .collect(Collectors.toList());

        System.out.println("electricInfoIds:"+electricInfoIds);
        // 如果electricInfoIds为空，直接返回空的结果列表
        if (electricInfoIds.isEmpty()) {
            return new ArrayList<>();
        }


        List<PassbackData> passbackList = testManIndexService.getPassbackByElectricInfoIds(electricInfoIds);

        // 将 PassbackData 映射成 Map<Integer, PassbackData>
        Map<Integer, PassbackData> passbackMap = passbackList.stream()
                .filter(p -> p.getSample_id() != null)
                .collect(Collectors.toMap(
                        p -> Integer.parseInt(p.getSample_id()),
                        p -> p,
                        (existing, replacement) -> existing // 处理键冲突
                ));

        List<Map<String, Object>> result = new ArrayList<>();
        for (ElectricScheduleInfo schedule : scheduleList) {
            result.add(mergeScheduleAndPassback(schedule, passbackMap));
        }
        System.out.println("result:"+result);

        return result;
    }

    private Map<String, Object> mergeScheduleAndPassback(ElectricScheduleInfo schedule, Map<Integer, PassbackData> passbackMap) {
        Map<String, Object> merged = new LinkedHashMap<>(); // 保证字段顺序一致

        // 将 ElectricScheduleInfo 的属性写入 map
        merged.put("id", schedule.getId());
        merged.put("electric_info_id", schedule.getElectric_info_id());
        merged.put("tester", schedule.getTester());
        merged.put("schedule_start_date", schedule.getSchedule_start_date());
        merged.put("schedule_end_date", schedule.getSchedule_end_date());
        merged.put("row_index", schedule.getRow_index());
        merged.put("column_index", schedule.getColumn_index());
        merged.put("create_time", schedule.getCreate_time());
        merged.put("update_time", schedule.getUpdate_time());
        merged.put("sizecoding", schedule.getSizecoding());

        // 合并对应的 PassbackData 字段
        PassbackData passback = passbackMap.get(schedule.getElectric_info_id());
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
            merged.put("passback_create_time", passback.getCreate_time()); // 避免与 schedule 的 create_time 重名
            merged.put("scheduleDays", passback.getScheduleDays());
            merged.put("isUsed", passback.getIsUsed());
        }

        return merged;
    }


    @PostMapping("/passback/saveScheduleDays")
    @ResponseBody
    public Map<String, Object> saveScheduleDays(@RequestBody Map<String, Object> params) {
        String sampleId = (String) params.get("sample_id");
        String scheduleDays = (String) params.get("scheduleDays");

        System.out.println("sample_id: " + sampleId + ", scheduleDays: " + scheduleDays);
        int saveScheduleDays = testManIndexService.saveScheduleDays(sampleId, scheduleDays);

        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("message", "排期天数保存成功！");
        return result;
    }


//    @PostMapping("/scheduleBoard/saveSchedule")
//    public ResponseEntity<Map<String, String>> saveSchedule(@RequestBody List<Map<String, Object>> scheduleChanges) {
//        if (scheduleChanges == null || scheduleChanges.isEmpty()) {
//            Map<String, String> response = new HashMap<>();
//            response.put("message", "没有需要保存的排期变更");
//            return ResponseEntity.badRequest().body(response);
//        }
//
//        // 处理数据
//        System.out.println("收到的排期变更数据: " + scheduleChanges);
//
//        // 这里可以调用服务层将数据保存到数据库或文件
//        // scheduleService.saveChanges(scheduleChanges);
//
//        Map<String, String> response = new HashMap<>();
//        response.put("message", "排期变更已成功保存");
//        return ResponseEntity.ok(response);
//    }

    @PostMapping("/scheduleBoard/saveSchedule")
    public ResponseEntity<Map<String, String>> saveSchedule(@RequestBody List<Map<String, Object>> scheduleChanges) {
        if (scheduleChanges == null || scheduleChanges.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "没有需要保存的排期变更");
            return ResponseEntity.badRequest().body(response);
        }

        // 处理数据
        System.out.println("收到的排期变更数据: " + scheduleChanges);

        // 遍历排期变更数据并发送 HTTP 请求
        RestTemplate restTemplate = new RestTemplate();
        String updateScheduleUrl = "http://localhost:8080/Api/ElectricalTest/UpdateScheduleElectricalTest";

        for (Map<String, Object> schedule : scheduleChanges) {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("ETTestCode", schedule.get("sample_id"));
            requestBody.put("ExpectedTestStartDate", schedule.get("start_date"));
            requestBody.put("ExpectedTestEndDate", schedule.get("end_date"));
            requestBody.put("TestLeaderName", schedule.get("tester"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            try {
                ResponseEntity<String> response = restTemplate.exchange(updateScheduleUrl, HttpMethod.POST, requestEntity, String.class);
                System.out.println("更新结果：" + response.getBody());
            } catch (Exception e) {
                System.err.println("更新失败：" + e.getMessage());
            }
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "排期变更已成功保存，并已同步到 ElectricalTest 接口");
        return ResponseEntity.ok(response);
    }

    //更新排期的 模拟调试
    @PostMapping("/Api/ElectricalTest/UpdateScheduleElectricalTest")
    public ResponseEntity<Map<String, Object>> updateSchedule(@RequestBody Map<String, Object> requestData) {
        System.out.println("收到的排期更新请求：" + requestData);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "成功接收排期变更");
        response.put("receivedData", requestData);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/passback/StartTestElectricalTest")
    public ResponseEntity<Map<String, Object>> startTest(@RequestBody Map<String, String> requestData) {
        String testNumber = requestData.get("test_number");
        String actual_time = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        if (testNumber == null || testNumber.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", 400);
            response.put("message", "测试编号不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        boolean updateStartTime = testManIndexService.StartTestElectricalTest(testNumber,actual_time);
        System.out.println("updateStartTime:"+updateStartTime);

        if (!updateStartTime) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", 400);
            response.put("message", "没有找到该测试编号，请输入正确的测试编号");
            return ResponseEntity.badRequest().body(response);
        }

        // 构造请求数据
        Map<String, String> requestPayload = new HashMap<>();
        requestPayload.put("ETTestCode", testNumber);
        requestPayload.put("ActualTestStartDate", actual_time);

        // 发送 POST 请求到目标接口
        String targetUrl = "http://localhost:8080/Api/ElectricalTest/StartTestElectricalTest";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> apiResponse = restTemplate.postForEntity(targetUrl, requestPayload, Map.class);

        return ResponseEntity.status(apiResponse.getStatusCode()).body(apiResponse.getBody());
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
    public ResponseEntity<Map<String, Object>> finishTest(@RequestBody Map<String, String> requestData) {
        String testNumber = requestData.get("test_number");
        String actualTestEndDate = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String actual_work_time = requestData.get("actual_work_time");

        if (testNumber == null || testNumber.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", 400);
            response.put("message", "测试编号不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        boolean updateFinishTime = testManIndexService.FinishTestElectricalTest(testNumber,actualTestEndDate);
        System.out.println("updateFinishTime:"+updateFinishTime);

        if (!updateFinishTime) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", 400);
            response.put("message", "没有找到该测试编号，请输入正确的测试编号");
            return ResponseEntity.badRequest().body(response);
        }

        // 构造请求数据
        Map<String, String> requestPayload = new HashMap<>();
        requestPayload.put("ETTestCode", testNumber);
        requestPayload.put("ActualTestEndDate", actualTestEndDate);
        requestPayload.put("ActualTestWrokHour", actual_work_time);

        // 发送 POST 请求到目标接口
        String targetUrl = "http://localhost:8080/Api/ElectricalTest/FinishTestElectricalTest";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> apiResponse = restTemplate.postForEntity(targetUrl, requestPayload, Map.class);

        return ResponseEntity.status(apiResponse.getStatusCode()).body(apiResponse.getBody());
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
    public ResponseEntity<Map<String, Object>> processTest(@RequestBody Map<String, String> requestData) {
        String testNumber = requestData.get("test_number");
        String reportReviewTime = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String sampleRecognizeResult = requestData.get("sampleRecognizeResult");

        System.out.println(ZoneId.systemDefault());

        if (testNumber == null || testNumber.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", 400);
            response.put("message", "测试编号不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        // 构造请求数据
        Map<String, String> requestPayload = new HashMap<>();
        requestPayload.put("ETTestCode", testNumber);
        requestPayload.put("ReportReviewTime", reportReviewTime);
        requestPayload.put("SampleRecognizeResult", sampleRecognizeResult);

        // 发送 POST 请求到目标接口
        String targetUrl = "http://localhost:8080/Api/ElectricalTest/ProcessTestElectricalTest";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> apiResponse = restTemplate.postForEntity(targetUrl, requestPayload, Map.class);

        return ResponseEntity.status(apiResponse.getStatusCode()).body(apiResponse.getBody());
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


}
