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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

//    @GetMapping("/labModuleTester") // 处理页面跳转请求
//    public String loginSampleData() {
//        // 返回跳转页面的视图名称
//        return "labModuleTester";
//    }

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

    @GetMapping("/scheduleBoardNAS") // 处理页面跳转请求
    public String loginScheduleBoardNAS() {
        // 返回跳转页面的视图名称
        return "DQE/scheduleBoardNAS";
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
//        System.out.println(overdueTaskNodeOnce);

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

//        System.out.println("combinedTaskNodes:"+combinedTaskNodes);
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

//        System.out.println("combinedTaskNodes:" + combinedTaskNodes);
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

    @PostMapping("/DQEIndex/saveAgents")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveAgents(@RequestBody Map<String, Object> requestData) {
        try {
            List<String> agents = (List<String>) requestData.get("agents");

            String username = (String) requestData.get("username");
            String agent_name = String.join(",", agents);
            System.out.println("agent_name:"+agent_name);
            
            int queryAgent = dqeIndexService.queryAgents(username);
            if(queryAgent > 0){
                dqeIndexService.updateAgents(agent_name, username);
            } else {
                dqeIndexService.insertAgents(agent_name, username);
            }

            // 返回JSON格式的成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "代理人设置保存成功");
            response.put("data", agents);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            
            // 返回JSON格式的错误响应
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "保存失败: " + e.getMessage());
            errorResponse.put("error", e.toString());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/DQEIndex/getAgents")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAgents(@RequestParam String username) {
        try {

            // 检查用户名是否为空
            if (username == null || username.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "用户名不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 调用service层方法
            String agentsStr = dqeIndexService.getAgents(username);

            
            List<String> agents = new ArrayList<>();
            
            if (agentsStr != null && !agentsStr.trim().isEmpty()) {
                // 将字符串按逗号分割成数组
                String[] agentArray = agentsStr.split(",");
                for (String agent : agentArray) {
                    String trimmedAgent = agent.trim();
                    if (!trimmedAgent.isEmpty()) {
                        agents.add(trimmedAgent);
                    }
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("agents", agents);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {

            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch agents: " + e.getMessage());
            errorResponse.put("exceptionType", e.getClass().getName());
            errorResponse.put("details", e.toString());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/DQEIndex/getProjectTotal")
    @ResponseBody
    public Map<String, Object> getProjectTotal(@RequestParam String username) {
        Map<String, Object> result = new HashMap<>();

//        System.out.println("username:"+username);
        
        try {
            // 获取提单待测试的数量：sample_sender=username 且 actual_start_time 为空
            int ladingBillWaitTest = testManIndexService.getLadingBillWaitTestCount(username);
            
            // 获取测试进行中的数量：sample_sender=username 且 actual_start_time 有值但 actual_finish_time 为空
            int testing = testManIndexService.getTestingCount(username);
            
            // 获取闭环完成的数量：sample_sender=username 且 sampleRecognizeResult 有值
            int closedCount = testManIndexService.getClosedCount(username);
            
            int projectCount = testManIndexService.queryCountElectricinfo(username);
            
            result.put("ladingBillWaitTest", ladingBillWaitTest);
            result.put("testing", testing);
            result.put("closedCount", closedCount);
            result.put("projectCount", projectCount);
            result.put("success", true);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "获取项目统计信息时出错: " + e.getMessage());
        }
        
        return result;
    }

    @GetMapping("/DQEIndex/getProjectDetail")
    @ResponseBody
    public Map<String, Object> getProjectDetail(@RequestParam String username, @RequestParam String projectType) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Map<String, Object>> projectDetails = new ArrayList<>();
            
            switch (projectType) {
                case "ladingBillWaitTest":
                    // 获取提单待测试的项目详情
                    projectDetails = testManIndexService.getLadingBillWaitTestDetails(username);
                    break;
                case "testing":
                    // 获取测试进行中的项目详情
                    projectDetails = testManIndexService.getTestingDetails(username);
                    break;
                case "recentCount":
                    // 获取待审核的项目详情
                    projectDetails = dqeIndexService.getRecentNotificationsDetails(username);
                    break;
                case "overdueCount":
                    // 获取超期项目详情
                    projectDetails = dqeIndexService.getOverdueDetails(username);
                    break;
                case "closedCount":
                    // 获取闭环完成项目详情
                    projectDetails = testManIndexService.getClosedDetails(username);
                    break;
                case "projectCount":
                    // 获取所有项目详情
                    projectDetails = testManIndexService.getAllProjectDetails(username);
                    break;
                default:
                    result.put("success", false);
                    result.put("error", "无效的项目类型");
                    return result;
            }
            
            result.put("success", true);
            result.put("data", projectDetails);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "获取项目详情时出错: " + e.getMessage());
        }
        
        return result;
    }

    // 新增数据看板路由
    @GetMapping("/managerboard")
    public String managerBoard() {
        return "managerboard";
    }

    @GetMapping("/rdDashboard")
    public String rdDashboard(@RequestParam(required = false) String username) {
//        System.out.println("username:"+username);
        if (username != null && !username.isEmpty()) {
            try {
                String encodedUsername = java.net.URLEncoder.encode(username, "UTF-8");
                return "redirect:/managerboard?job=rd&username=" + encodedUsername;
            } catch (Exception e) {
                System.err.println("编码username失败: " + e.getMessage());
                return "redirect:/managerboard?job=rd";
            }
        }
        return "redirect:/managerboard?job=rd";
    }

    @GetMapping("/manager")
    public String manager() {
        return "manager";
    }

    @GetMapping("/projectLeaderDashboard")
    public String projectLeaderDashboard() {
        return "projectLeaderDashboard";
    }

    // DQE个人数据看板
    @GetMapping("/dqeDashboard")
    public String dqeDashboard(@RequestParam(required = false) String username,
                               @RequestParam(required = false)String job) {
        System.out.println("username:"+username);
        System.out.println("job:"+job);
        if (username != null && !username.isEmpty()) {
            try {
                String encodedUsername = java.net.URLEncoder.encode(username, "UTF-8");
                return "redirect:/managerboard?job="+ job + "&username=" + encodedUsername;

            } catch (Exception e) {
                System.err.println("编码username失败: " + e.getMessage());
                return "redirect:/managerboard?job=DQE";
            }
        }
        return "redirect:/managerboard?job=DQE";
    }

    // 研发个人数据看板
    @GetMapping("/rdPersonalDashboard")
    public String rdPersonalDashboard() {
        return "rdPersonalDashboard";
    }

    // 研发个人数据看板
    @GetMapping("/problemLibraryManage")
    public String problemLibraryManage() {
        return "problemLibraryManage";
    }

    // 智能项目识别与统计分析系统
//    @GetMapping("/smart-project-analyzer")
//    public String smartProjectAnalyzer() {
//        // 返回智能项目识别与统计分析系统页面
//        return "DQE/smart-project-analyzer-source";
//    }

    // 获取电气项目数据（管理层看板）
    @GetMapping("/DQEIndex/getElectricProjectData")
    @ResponseBody
    public Map<String, Object> getElectricProjectData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 调用服务层获取electric_info表的数据
            List<Map<String, Object>> projectData = dqeIndexService.getElectricProjectData();
//            System.out.println("projectData"+projectData);
            
            result.put("success", true);
            result.put("data", projectData);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "获取电气项目数据时出错: " + e.getMessage());
        }

        return result;
    }

    // 获取DQE个人电气项目数据
    @GetMapping("/DQEIndex/getDqeElectricProjectData")
    @ResponseBody
    public Map<String, Object> getDqeElectricProjectData(@RequestParam String username) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 调用服务层获取electric_info表的数据，筛选DQE负责人
            List<Map<String, Object>> projectData = dqeIndexService.getDqeElectricProjectData(username);
            
            result.put("success", true);
            result.put("data", projectData);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "获取DQE电气项目数据时出错: " + e.getMessage());
        }

        return result;
    }

    // 获取研发个人电气项目数据
    @GetMapping("/DQEIndex/getRdElectricProjectData")
    @ResponseBody
    public Map<String, Object> getRdElectricProjectData(@RequestParam String username) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 调用服务层获取electric_info表的数据，筛选研发负责人
            List<Map<String, Object>> projectData = dqeIndexService.getRdElectricProjectData(username);
            
            result.put("success", true);
            result.put("data", projectData);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "获取研发电气项目数据时出错: " + e.getMessage());
        }

        return result;
    }

    // 获取排期时间分布数据（项目专员数据看板）
    @GetMapping("/DQEIndex/getScheduleDistributionData")
    @ResponseBody
    public Map<String, Object> getScheduleDistributionData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 调用服务层获取排期时间分布数据
            Map<String, Object> distributionData = dqeIndexService.getScheduleDistributionData();
            
            result.put("success", true);
            result.put("data", distributionData);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "获取排期时间分布数据时出错: " + e.getMessage());
        }

        return result;
    }

    // 获取排期详情数据
    @GetMapping("/DQEIndex/getScheduleDetails")
    @ResponseBody
    public Map<String, Object> getScheduleDetails(@RequestParam String scheduleStatus) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 调用服务层获取排期详情数据
            List<Map<String, Object>> detailsData = dqeIndexService.getScheduleDetails(scheduleStatus);
            
            result.put("success", true);
            result.put("data", detailsData);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "获取排期详情数据时出错: " + e.getMessage());
        }

        return result;
    }

}
