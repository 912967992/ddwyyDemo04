package com.lu.ddwyydemo04.controller.DQE;

import com.lu.ddwyydemo04.Service.DQE.DQEproblemMoudleService;
import com.lu.ddwyydemo04.dao.DQE.DQEDao;
import com.lu.ddwyydemo04.pojo.Samples;
import com.lu.ddwyydemo04.pojo.TestIssues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

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
        // 处理接收到的 selectedIds 列表
        System.out.println("接收到的sampleID: " + sampleId);

        List<TestIssues> selectTestissues = dqeproblemMoudleService.selectTestIssuesFromSampleid(sampleId);

        System.out.println("返回的结果: " + selectTestissues);

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





}
