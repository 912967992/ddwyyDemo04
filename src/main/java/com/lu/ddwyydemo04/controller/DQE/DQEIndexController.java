package com.lu.ddwyydemo04.controller.DQE;

import com.lu.ddwyydemo04.Service.AccessTokenService;
import com.lu.ddwyydemo04.Service.DQE.DQEIndexService;
import com.lu.ddwyydemo04.Service.TestManIndexService;
import com.lu.ddwyydemo04.pojo.Samples;
import com.lu.ddwyydemo04.pojo.TaskNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class DQEIndexController {

    @Autowired
    private DQEIndexService dqeIndexService;

    @Autowired
    private AccessTokenService accessTokenService;


    @GetMapping("/DQEIndex") // 处理页面跳转请求
    public String loginDQEIndex() {
        // 返回跳转页面的视图名称
        return "DQEIndex";
    }

    @GetMapping("/projectSchedule") // 处理页面跳转请求
    public String loginProjectSchedule() {
        // 返回跳转页面的视图名称
        return "DQE/projectSchedule";
    }

    @GetMapping("/problemMoudle") // 处理页面跳转请求
    public String loginProblemMoudle() {

        // 返回跳转页面的视图名称
        return "DQE/problemMoudle";
    }

    @GetMapping("/sampleData") // 处理页面跳转请求
    public String loginSampleData() {
        // 返回跳转页面的视图名称
        return "DQE/sampleData";
    }

    @GetMapping("/otherMoudle") // 处理页面跳转请求
    public String loginOtherMoudle() {
        // 返回跳转页面的视图名称
        return "DQE/otherMoudle";
    }

    @GetMapping("/dataBoard") // 处理页面跳转请求
    public String logindataBoard() {
        // 返回跳转页面的视图名称
        return "DQE/dataBoard";
    }

    @PostMapping("/DQEIndex/saveWarningDays")
    @ResponseBody
    public ResponseEntity<String> saveWarningDays(@RequestBody Map<String, Object> request) {
        // 获取警示天数和角色
        String warningDays = (String) request.get("warningDays");
        String role = (String) request.get("role");
        String roleManager = "";

        // 处理逻辑：根据角色进行不同的保存操作
        if ("DQE".equals(role)) {
            roleManager = "dqeManager";
        } else if ("rd".equals(role)) {
            roleManager = "rdManager";
        }
        int updateSetting = dqeIndexService.updateSetting(roleManager,warningDays);
        // 检查更新结果
        if (updateSetting > 0) {
            return ResponseEntity.ok("警示天数已成功保存");
        } else {
            return ResponseEntity.status(500).body("警示天数保存失败，请重试");
        }
    }


    @GetMapping("/DQEIndex/getOverdueSample")
    @ResponseBody
    public List<TaskNode> getOverdueSample() {
        List<TaskNode> overdueTaskNode = dqeIndexService.findFirstOverdueSamplesOnce();
        System.out.println(overdueTaskNode);


        return overdueTaskNode;
    }

}
