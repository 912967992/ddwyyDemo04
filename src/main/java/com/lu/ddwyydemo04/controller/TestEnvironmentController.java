package com.lu.ddwyydemo04.controller;

import com.lu.ddwyydemo04.Service.ExcelShowService;
import com.lu.ddwyydemo04.Service.TestManIndexService;
import com.lu.ddwyydemo04.pojo.PassbackData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> receiveData(@RequestBody PassbackData requestData) {

        // 获取传入的请求数据
        String sampleId = requestData.getSampleId();
        String sampleCategory = requestData.getSampleCategory();
        String sampleModel = requestData.getSampleModel();
        String sampleCoding = requestData.getSampleCoding();
        String materialCode = requestData.getMaterialCode();
        String sampleFrequency = requestData.getSampleFrequency();
        String sampleName = requestData.getSampleName();
        String version = requestData.getVersion();
        String priority = requestData.getPriority();
        String sampleLeader = requestData.getSampleLeader();
        String supplier = requestData.getSupplier();
        String testProjectCategory = requestData.getTestProjectCategory();
        String testProjects = requestData.getTestProjects();
        String schedule = requestData.getSchedule();

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
    public ResponseEntity<List<Map<String, Object>>> getReceivedData() {
        String sql = "SELECT * FROM test_data ORDER BY id DESC LIMIT 10";
        List<Map<String, Object>> receivedData = testManIndexService.getReceivedData();
        return ResponseEntity.ok(receivedData);
    }

}
