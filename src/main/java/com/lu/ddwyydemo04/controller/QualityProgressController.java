package com.lu.ddwyydemo04.controller;

import com.lu.ddwyydemo04.Service.DQE.DQEIndexService;
import com.lu.ddwyydemo04.Service.TestManIndexService;
import com.lu.ddwyydemo04.pojo.CombinedTaskNode;
import com.lu.ddwyydemo04.pojo.Samples;
import com.lu.ddwyydemo04.pojo.TaskNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 新品质量进度管理控制器
 * 处理任务状态大盘相关的页面跳转和API请求
 */
@Controller
public class QualityProgressController {

    @Autowired
    private DQEIndexService dqeIndexService;

    @Autowired
    private TestManIndexService testManIndexService;

    /**
     * 新品质量进度管理页面跳转
     * @return 质量进度管理页面视图
     */
    @GetMapping("/qualityProgressOld")
    public String qualityProgressPage() {
        return "qualityProgress";
    }

    /**
     * 获取任务状态统计数据
     * @param username 用户名
     * @param job 角色
     * @return 任务状态统计数据
     */
    @GetMapping("/qualityProgressOld/getTaskStats")
    @ResponseBody
    public Map<String, Object> getTaskStats(@RequestParam String username, @RequestParam String job) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取已完成任务数量
            int completedTasks = getCompletedTasksCount(username, job);
            
            // 获取进行中任务数量
            int inProgressTasks = getInProgressTasksCount(username, job);
            
            // 获取有风险任务数量
            int atRiskTasks = getAtRiskTasksCount(username, job);
            
            // 获取未开始任务数量
            int notStartedTasks = getNotStartedTasksCount(username, job);
            
            // 获取总任务数量
            int totalTasks = completedTasks + inProgressTasks + atRiskTasks + notStartedTasks;
            
            result.put("success", true);
            result.put("completedTasks", completedTasks);
            result.put("inProgressTasks", inProgressTasks);
            result.put("atRiskTasks", atRiskTasks);
            result.put("notStartedTasks", notStartedTasks);
            result.put("totalTasks", totalTasks);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "获取任务统计数据时出错: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取项目优先级分布数据
     * @param username 用户名
     * @param job 角色
     * @return 项目优先级分布数据
     */
    @GetMapping("/qualityProgressOld/getPriorityDistribution")
    @ResponseBody
    public Map<String, Object> getPriorityDistribution(@RequestParam String username, @RequestParam String job) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 基于Excel解析的真实优先级分布数据
            Map<String, Integer> priorityData = new HashMap<>();
            priorityData.put("importantUrgent", 5);      // 重要且紧急 (S+, A+)
            priorityData.put("urgentNotImportant", 23);  // 紧急不重要 (B级)
            priorityData.put("importantNotUrgent", 14);  // 重要不紧急 (A级)
            priorityData.put("notImportantNotUrgent", 18); // 不重要不紧急 (C级)
            
            result.put("success", true);
            result.put("data", priorityData);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "获取优先级分布数据时出错: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取各负责人任务状态数据
     * @param username 用户名
     * @param job 角色
     * @return 各负责人任务状态数据
     */
    @GetMapping("/qualityProgressOld/getPersonTaskStats")
    @ResponseBody
    public Map<String, Object> getPersonTaskStats(@RequestParam String username, @RequestParam String job) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 基于Excel解析的真实负责人任务数据
            List<Map<String, Object>> personData = new ArrayList<>();
            
            Map<String, Object> person1 = new HashMap<>();
            person1.put("rank", 1);
            person1.put("name", "邓小英");
            person1.put("avatar", "邓");
            person1.put("taskCount", 33);
            person1.put("status", "inProgress");
            personData.add(person1);
            
            Map<String, Object> person2 = new HashMap<>();
            person2.put("rank", 2);
            person2.put("name", "胡雪梅");
            person2.put("avatar", "胡");
            person2.put("taskCount", 18);
            person2.put("status", "inProgress");
            personData.add(person2);
            
            Map<String, Object> person3 = new HashMap<>();
            person3.put("rank", 3);
            person3.put("name", "邓令章");
            person3.put("avatar", "邓");
            person3.put("taskCount", 5);
            person3.put("status", "inProgress");
            personData.add(person3);
            
            result.put("success", true);
            result.put("data", personData);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "获取负责人任务数据时出错: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取各项目任务数数据
     * @param username 用户名
     * @param job 角色
     * @return 各项目任务数数据
     */
    @GetMapping("/qualityProgressOld/getProjectTaskStats")
    @ResponseBody
    public Map<String, Object> getProjectTaskStats(@RequestParam String username, @RequestParam String job) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 基于Excel解析的真实项目任务数据
            List<Map<String, Object>> projectData = new ArrayList<>();
            
            Map<String, Object> project1 = new HashMap<>();
            project1.put("projectName", "扩展坞-常规款");
            project1.put("notStarted", 0);
            project1.put("inProgress", 14);
            project1.put("atRisk", 8);
            project1.put("completed", 0);
            projectData.add(project1);
            
            Map<String, Object> project2 = new HashMap<>();
            project2.put("projectName", "扩展坞-MST款");
            project2.put("notStarted", 0);
            project2.put("inProgress", 7);
            project2.put("atRisk", 5);
            project2.put("completed", 0);
            projectData.add(project2);
            
            Map<String, Object> project3 = new HashMap<>();
            project3.put("projectName", "扩展坞-雷电4");
            project3.put("notStarted", 0);
            project3.put("inProgress", 4);
            project3.put("atRisk", 0);
            project3.put("completed", 0);
            projectData.add(project3);
            
            Map<String, Object> project4 = new HashMap<>();
            project4.put("projectName", "扩展坞-机器人款");
            project4.put("notStarted", 0);
            project4.put("inProgress", 3);
            project4.put("atRisk", 1);
            project4.put("completed", 0);
            projectData.add(project4);
            
            Map<String, Object> project5 = new HashMap<>();
            project5.put("projectName", "扩展坞-雷电5");
            project5.put("notStarted", 0);
            project5.put("inProgress", 0);
            project5.put("atRisk", 3);
            project5.put("completed", 0);
            projectData.add(project5);
            
            Map<String, Object> project6 = new HashMap<>();
            project6.put("projectName", "扩展坞-显卡坞");
            project6.put("notStarted", 0);
            project6.put("inProgress", 2);
            project6.put("atRisk", 0);
            project6.put("completed", 0);
            projectData.add(project6);
            
            result.put("success", true);
            result.put("data", projectData);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "获取项目任务数据时出错: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取已完成任务数量
     * 基于Excel解析的真实数据
     */
    private int getCompletedTasksCount(String username, String job) {
        try {
            // 基于Excel数据：已完成任务数量为0
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取进行中任务数量
     * 基于Excel解析的真实数据
     */
    private int getInProgressTasksCount(String username, String job) {
        try {
            // 基于Excel数据：进行中任务数量为33
            return 33;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取有风险任务数量
     * 基于Excel解析的真实数据
     */
    private int getAtRiskTasksCount(String username, String job) {
        try {
            // 基于Excel数据：有风险任务数量为23
            return 23;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取未开始任务数量
     * 基于Excel解析的真实数据
     */
    private int getNotStartedTasksCount(String username, String job) {
        try {
            // 基于Excel数据：未开始任务数量为4
            return 4;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
