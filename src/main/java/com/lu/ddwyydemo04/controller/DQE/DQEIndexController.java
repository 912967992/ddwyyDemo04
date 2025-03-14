package com.lu.ddwyydemo04.controller.DQE;


import com.lu.ddwyydemo04.Service.DQE.DQEIndexService;

import com.lu.ddwyydemo04.Service.TestManIndexService;
import com.lu.ddwyydemo04.pojo.CombinedTaskNode;
import com.lu.ddwyydemo04.pojo.PassbackData;
import com.lu.ddwyydemo04.pojo.Samples;
import com.lu.ddwyydemo04.pojo.TaskNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class DQEIndexController {

    @Autowired
    private DQEIndexService dqeIndexService;

    @Autowired
    private TestManIndexService testManIndexService;


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

    @GetMapping("/labModule") // 处理页面跳转请求
    public String loginSampleData() {
        // 返回跳转页面的视图名称
        return "DQE/labModule";
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



    @GetMapping("/weeklyNewspaper") // 处理页面跳转请求
    public String logindWeeklyNewspaper() {
        // 返回跳转页面的视图名称
        return "DQE/weeklyNewspaper";
    }

    @GetMapping("/scheduleBoard") // 处理页面跳转请求
    public String loginScanBinning() {
        // 返回跳转页面的视图名称
        return "DQE/scheduleBoard";
    }

    @PostMapping("/DQEIndex/saveWarningDays")
    @ResponseBody
    public ResponseEntity<String> saveWarningDays(@RequestBody Map<String, Object> request) {
        // 获取警示天数和角色
        String warningDays = (String) request.get("warningDays");
        String role = (String) request.get("role");
        String roleManager = "";
        System.out.println("warningDays:"+warningDays);
        System.out.println("role:"+role);

        // 处理逻辑：根据角色进行不同的保存操作
        if ("DQE".equals(role) || "manager".equals(role)) {
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
    public List<CombinedTaskNode> getOverdueSample() {
        List<TaskNode> overdueTaskNodeOnce = dqeIndexService.findFirstOverdueSamplesOnce();
        System.out.println(overdueTaskNodeOnce);

        // 创建一个新的列表来存储组合后的结果
        List<CombinedTaskNode> combinedTaskNodes = new ArrayList<>();

        // 使用 for 循环提取 sample_id
        for (TaskNode taskNode : overdueTaskNodeOnce) {
            int sampleId = taskNode.getSample_id();
            List<Samples> samples = dqeIndexService.selectFromSampleId(sampleId);

            // 假设我们只取第一个样本的相关字段，或者你可以根据需要调整逻辑
            for (Samples sample : samples) {
                CombinedTaskNode combinedTaskNode = new CombinedTaskNode();
                combinedTaskNode.setTaskNode(taskNode); // 假设你想保留原始的 TaskNode 数据
                combinedTaskNode.setFullModel(sample.getFull_model());
                combinedTaskNode.setBigSpecial(sample.getBig_species());
                combinedTaskNode.setSmallSpecial(sample.getSmall_species());
                combinedTaskNode.setSampleCategory(sample.getSample_category());
                combinedTaskNode.setVersion(sample.getVersion());
                combinedTaskNode.setQuestStats(sample.getQuestStats());
                combinedTaskNode.setDqe(sample.getSample_DQE());
                combinedTaskNode.setRd(sample.getSample_Developer());
                combinedTaskNodes.add(combinedTaskNode);
            }
        }

        System.out.println("combinedTaskNodes:"+combinedTaskNodes);
        return combinedTaskNodes; // 返回新的组合数组

    }

    // 查看近期通知项目的 方法
    @GetMapping("/DQEIndex/getRecentNotifications")
    @ResponseBody
    public List<CombinedTaskNode> getRecentNotifications(@RequestParam String username) {
        List<TaskNode> overdueTaskNodeOnce = dqeIndexService.getRecentNotifications();

        // 创建一个新的列表来存储组合后的结果
        List<CombinedTaskNode> combinedTaskNodes = new ArrayList<>();

        // 使用 for 循环提取 sample_id
        for (TaskNode taskNode : overdueTaskNodeOnce) {
            int sampleId = taskNode.getSample_id();
            List<Samples> samples = dqeIndexService.selectFromSampleId(sampleId);

            // 遍历样本列表，并根据用户名过滤
            for (Samples sample : samples) {
                // 检查用户名是否匹配 DQE 或 Developer 字段
                if (username.equals(sample.getSample_DQE()) || username.equals(sample.getSample_Developer()) || username.equals(sample.getSample_leader())) {
                    CombinedTaskNode combinedTaskNode = new CombinedTaskNode();
                    combinedTaskNode.setTaskNode(taskNode); // 保留原始的 TaskNode 数据
                    combinedTaskNode.setFullModel(sample.getFull_model());
                    combinedTaskNode.setBigSpecial(sample.getBig_species());
                    combinedTaskNode.setSmallSpecial(sample.getSmall_species());
                    combinedTaskNode.setSampleCategory(sample.getSample_category());
                    combinedTaskNode.setVersion(sample.getVersion());
                    combinedTaskNode.setQuestStats(sample.getQuestStats());
                    combinedTaskNode.setDqe(sample.getSample_DQE());
                    combinedTaskNode.setRd(sample.getSample_Developer());
                    combinedTaskNode.setProjectLeader(sample.getSample_leader());
                    combinedTaskNodes.add(combinedTaskNode);
                }
            }
        }

        System.out.println("combinedTaskNodes:" + combinedTaskNodes);
        return combinedTaskNodes; // 返回过滤后的组合数组
    }

    // 获取 warningDays 的接口方法
    @GetMapping("/DQEIndex/getWarningDays")
    @ResponseBody
    public ResponseEntity<Integer> getWarningDays(@RequestParam String configType) {
        try {
            System.out.println("configType:"+configType);
            String manager = "";
            if(configType.equals("DQE")){
                manager = "dqeManager";
            }else if(configType.equals("研发")){
                manager = "rdManager";
            }
            // 通过 ConfigService 从数据库获取用户的 warningDays 值
            String warningDaysStr  = dqeIndexService.getWarningDaysByManager(manager);

            int warningDays = (warningDaysStr == null) ? 0 : Integer.parseInt(warningDaysStr);

            // 返回 warningDays
            return ResponseEntity.ok(warningDays);
        } catch (Exception e) {
            // 捕获异常并返回 500 错误状态码
            return ResponseEntity.status(500).build();
        }
    }


    @PostMapping("/DQEIndex/updateOverdueReason")
    @ResponseBody
    public ResponseEntity<String> updateOverdueReason(
            @RequestParam("id") String id,
            @RequestParam("overdueReason") String overdueReason) {
        try {

            // 调用 service 层方法更新数据库
            boolean isUpdated = dqeIndexService.updateOverdueReason(id, overdueReason);

            if (isUpdated) {
                System.out.println("id更新超时原因成功:"+id);
                return ResponseEntity.ok("超时原因更新成功");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("超时原因更新失败");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("更新超时原因时出错");
        }
    }






}
