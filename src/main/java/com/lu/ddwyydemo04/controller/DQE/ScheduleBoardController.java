package com.lu.ddwyydemo04.controller.DQE;

import com.lu.ddwyydemo04.Service.DQE.DQEIndexService;
import com.lu.ddwyydemo04.Service.DQE.ScheduleBoardService;
import com.lu.ddwyydemo04.Service.TestManIndexService;
import com.lu.ddwyydemo04.pojo.*;
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
public class ScheduleBoardController {
    @Autowired
    private ScheduleBoardService scheduleBoardService;
    @Autowired
    private TestManIndexService testManIndexService;


    @GetMapping("/scheduleBoardController/getTestEngineers")
    @ResponseBody
    public List<TestEngineerInfo> getTestEngineers() {
//        System.out.println("getTestEngineers:"+scheduleBoardService.getTestEngineers());
        return scheduleBoardService.getTestEngineers();
    }

    @PostMapping("/scheduleBoardController/updateTesterInfo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateTesterInfo(@RequestBody List<TesterInfo> testerInfoList) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 这里可以添加具体的业务逻辑，比如将数据保存到数据库
            for (TesterInfo testerInfo : testerInfoList) {
//                System.out.println("更新测试人员信息: " + testerInfo);
                // 示例：调用服务层方法保存数据
                // testerService.saveOrUpdate(testerInfo);
                scheduleBoardService.updateTesterInfo(testerInfo);

            }
            response.put("success", true);
            response.put("message", "测试人员信息更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "测试人员信息更新失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/scheduleBoardController/addTesterInfo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addTesterInfo(@RequestBody TesterInfo testerInfo) {
        Map<String, Object> response = new HashMap<>();
        try {

            // 先查询新增的名字是否已存在
            int count = scheduleBoardService.queryExistTester(testerInfo);
            if (count == 0) {
                // 查询是否存在工号
                Map<String, String> data = scheduleBoardService.queryJobnumber(testerInfo);

                // 处理可能为空的情况
                String job_number = data != null ? data.get("job_number") : null;
                String hire_date = data != null ? data.get("hire_date") : null;

                testerInfo.setEngineer_id(job_number);
                testerInfo.setHire_date(hire_date);

                boolean insert = scheduleBoardService.insertTesterInfo(testerInfo);
                if (insert) {
                    response.put("success", true);
                    response.put("message", "测试人员添加成功");
                    return new ResponseEntity<>(response, HttpStatus.OK);
                } else {
                    response.put("success", false);
                    response.put("message", "数据库插入失败，添加测试人员失败");
                    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                response.put("success", false);
                response.put("message", "该测试人员已存在，请勿重复添加");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "新增测试人员失败，原因：" + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/scheduleBoardController/deleteTester")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteTester(@RequestBody Map<String, String> request) {
        String test_engineer_name = request.get("test_engineer_name");
        Map<String, Object> response = new HashMap<>();

        if (test_engineer_name == null) {
            response.put("success", false);
            response.put("message", "测试人员 ID 不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        boolean isDeleted = scheduleBoardService.deleteTester(test_engineer_name);
        if (isDeleted) {
            response.put("success", true);
            response.put("message", "测试人员删除成功");
        } else {
            response.put("success", false);
            response.put("message", "未找到该测试人员，删除失败");
        }

        return ResponseEntity.ok(response);
    }



    @GetMapping("/scheduleBoardController/getGroupData")
    @ResponseBody
    public List<Group> getGroupData() {
        List<Group> groupList = scheduleBoardService.getGroupData();
        return groupList;
    }

    @PostMapping("/scheduleBoardController/saveGroupSettings")
    @ResponseBody
    public Map<String, Object> saveGroupSettings(@RequestBody List<Group> groupSettings) {
        Map<String, Object> result = new HashMap<>();

        try {
            for (Group group : groupSettings) {
                scheduleBoardService.updateGroup(group);
            }
            result.put("success", true);
            result.put("message", "组设置已保存到数据库");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "数据库更新失败：" + e.getMessage());
        }

        return result;
    }


    @GetMapping("/scheduleBoardController/getGroupOrder")
    @ResponseBody
    public List<String> getGroupOrder() {
        // 假设从数据库查询，按 display_order 排序后只取 name 字段
        List<Group> groups = scheduleBoardService.getAllGroupsOrderByDisplayOrder();
        return groups.stream()
                .map(Group::getName)
                .collect(Collectors.toList());
    }

    @PostMapping("/scheduleBoardController/addGroup")
    @ResponseBody
    public String addGroup(@RequestBody Group group) {

        scheduleBoardService.addGroup(group);
        return "新增成功";
    }



    @PostMapping("/scheduleBoardController/searchProjects")
    @ResponseBody
    public List<PassbackData> searchProjects(@RequestBody Map<String, String> requestData) {
        String sampleId = requestData.get("sampleId");
        String sampleModel = requestData.get("sampleModel");
        String tester = requestData.get("tester");

        // 查询结果
        List<PassbackData> results = scheduleBoardService.searchProjects(sampleId, sampleModel, tester);

        // 遍历结果，补全 materialCode
        for (PassbackData data : results) {
            if (data.getMaterialCode() == null || data.getMaterialCode().trim().isEmpty()) {
                String projectId = data.getSample_id(); // 或者 data.getProjectId()，看你数据结构
                List<String> materialItems = testManIndexService.getMaterialCodes(projectId);
                String materialItemsStr = String.join("，", materialItems);
                data.setMaterialCode(materialItemsStr);
            }
        }

        return results;
    }


}
