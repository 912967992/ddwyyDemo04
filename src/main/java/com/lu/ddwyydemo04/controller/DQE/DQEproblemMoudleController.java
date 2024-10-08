package com.lu.ddwyydemo04.controller.DQE;

import com.lu.ddwyydemo04.Service.DQE.DQEproblemMoudleService;
import com.lu.ddwyydemo04.dao.DQE.DQEDao;
import com.lu.ddwyydemo04.pojo.Samples;
import com.lu.ddwyydemo04.pojo.TestIssues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class DQEproblemMoudleController {
    @Autowired
    private DQEproblemMoudleService dqeproblemMoudleService;


    @GetMapping("/problemMoudle/searchTestissues") // 处理页面跳转请求
    @ResponseBody
    public List<TestIssues> searchTestissues() {
        List<TestIssues> resultTestissues = dqeproblemMoudleService.searchTestissues();
        System.out.println(resultTestissues);
        return resultTestissues;
    }

    @PostMapping("/problemMoudle/editClickBtn") // 处理页面跳转请求
    @ResponseBody
    public List<TestIssues> editClickBtn(@RequestBody int sampleId) {

        List<TestIssues> selectTestissues = dqeproblemMoudleService.selectTestIssuesFromSampleid(sampleId);

        // 示例: 返回一个简单的响应
        return selectTestissues;
    }

    @GetMapping("/problemMoudle/searchSamplesDQE") // 处理页面跳转请求
    @ResponseBody
    public List<Samples> searchSamplesDQE() {
        List<Samples> resultSamples = dqeproblemMoudleService.searchSamplesDQE();
        System.out.println(resultSamples);
        return resultSamples;
    }

    //问题点模块保存问题点修改的方法
    @PostMapping("/problemMoudle/saveAllData")
    @ResponseBody
    public ResponseEntity<String> saveAllData(@RequestBody List<Map<String, Object>> allData) {
        try {
            System.out.println("接收到的数据量: " + allData.size()); // 打印接收到的数据量
            for (Map<String, Object> row : allData) {
                System.out.println("处理行数据: " + row); // 打印每一行的数据
            }

            return ResponseEntity.ok("保存成功"); // 返回成功消息
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("保存失败: " + e.getMessage());
        }
    }





}
