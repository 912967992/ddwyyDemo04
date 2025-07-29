package com.lu.ddwyydemo04.controller.DQE;

import cn.hutool.json.JSONObjectIter;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiAttendanceGetupdatedataRequest;
import com.dingtalk.api.request.OapiAttendanceListRecordRequest;
import com.dingtalk.api.response.OapiAttendanceGetupdatedataResponse;
import com.dingtalk.api.response.OapiAttendanceListRecordResponse;
import com.lu.ddwyydemo04.Service.AccessTokenService;
import com.lu.ddwyydemo04.Service.DQE.DQEIndexService;
import com.lu.ddwyydemo04.Service.DQE.ScheduleBoardService;
import com.lu.ddwyydemo04.Service.TestManIndexService;
import com.lu.ddwyydemo04.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ScheduleBoardController {
    @Autowired
    private ScheduleBoardService scheduleBoardService;
    @Autowired
    private TestManIndexService testManIndexService;

    @Autowired
    private AccessTokenService accessTokenService;

    /**
     * 变更记录搜索看板页面
     */
    @GetMapping("/changeRecordSearch")
    public String changeRecordSearchPage() {
        return "DQE/changeRecordSearch";
    }

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
        return scheduleBoardService.getGroupData();
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

    /**
     * 基于 changeRecord 的搜索功能
     */
    @PostMapping("/scheduleBoardController/searchByChangeRecord")
    @ResponseBody
    public List<Map<String, Object>> searchByChangeRecord(@RequestBody Map<String, String> requestData) {
        System.out.println("requestData:"+requestData);

        String ETTestCode = requestData.get("ETTestCode");
        String sampleModel = requestData.get("sampleModel");
        String sampleCategory = requestData.get("sampleCategory");
        String sampleLeader = requestData.get("sampleLeader");
        String sampleSender = requestData.get("sampleSender");

        String tester = requestData.get("tester");
        String startDate = requestData.get("startDate");
        String endDate = requestData.get("endDate");
        String isUsed = requestData.get("isUsed");


        String isCancel  = requestData.get("isCancel");
        String schedule = requestData.get("schedule");
        String remark = requestData.get("remark");
        String changeRecordStartDate = requestData.get("changeRecordStartDate");
        String changeRecordEndDate = requestData.get("changeRecordEndDate");
        String changeRecordRemark = requestData.get("changeRecordRemark");



//        System.out.println("搜索参数 - ETTestCode: " + ETTestCode);
//        System.out.println("搜索参数 - sampleModel: " + sampleModel);
//        System.out.println("搜索参数 - sampleCategory: " + sampleCategory);
//        System.out.println("搜索参数 - sampleLeader: " + sampleLeader);
//        System.out.println("搜索参数 - sampleSender: " + sampleSender);
//
//        System.out.println("搜索参数 - tester: " + tester);
//        System.out.println("搜索参数 - startDate: " + startDate);
//        System.out.println("搜索参数 - endDate: " + endDate);
//        System.out.println("搜索参数 - isUsed: " + isUsed);
//
//        System.out.println("搜索参数 - isCancel: " + isCancel);
//        System.out.println("搜索参数 - schedule: " + schedule);
//        System.out.println("搜索参数 - remark: " + remark);
//        System.out.println("搜索参数 - changeRecordStartDate: " + changeRecordStartDate);
//        System.out.println("搜索参数 - changeRecordEndDate: " + changeRecordEndDate);
//        System.out.println("搜索参数 - changeRecordRemark: " + changeRecordRemark);


        //
        List<Map<String, Object>> changeRecordResults = scheduleBoardService.searchByChangeRecord(tester, startDate, endDate, isUsed, remark);
        System.out.println("changeRecordResults:"+changeRecordResults);
        return changeRecordResults;
    }



    /**
     * 解析 changeRecord 字符串为结构化数据
     */
    private List<Map<String, Object>> parseChangeRecord(String changeRecord) {
        List<Map<String, Object>> records = new ArrayList<>();
        if (changeRecord == null || changeRecord.trim().isEmpty()) {
            return records;
        }

        String[] recordArray = changeRecord.split("\\|");
        for (String record : recordArray) {
            String[] parts = record.trim().split("#");
            if (parts.length >= 7) {
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put("tester", parts[0].equals("null") ? "" : parts[0]);
                recordMap.put("start_date", parts[1].equals("null") ? "" : parts[1]);
                recordMap.put("end_date", parts[2].equals("null") ? "" : parts[2]);
                recordMap.put("update_time", parts[3].equals("null") ? "" : parts[3]);
                recordMap.put("schedule_color", parts[4].equals("null") ? "" : parts[4]);
                recordMap.put("is_used", parts[5].equals("null") ? "" : parts[5]);
                recordMap.put("remark", parts[6].equals("null") ? "" : parts[6]);
                records.add(recordMap);
            }
        }
        return records;
    }

    /**
     * 获取变更记录统计信息
     */
    @PostMapping("/scheduleBoardController/getChangeRecordStats")
    @ResponseBody
    public Map<String, Object> getChangeRecordStats(@RequestBody Map<String, String> requestData) {
        String startDate = requestData.get("startDate");
        String endDate = requestData.get("endDate");
        
        Map<String, Object> stats = scheduleBoardService.getChangeRecordStats(startDate, endDate);
        return stats;
    }

    /**
     * 基于新表的变更记录搜索
     */
    @PostMapping("/scheduleBoardController/searchByChangeRecordNew")
    @ResponseBody
    public List<Map<String, Object>> searchByChangeRecordNew(@RequestBody Map<String, String> requestData) {
        try {
            System.out.println("收到新接口请求数据: " + requestData);
            
            String ETTestCode = requestData.get("ETTestCode");
            String sampleModel = requestData.get("sampleModel");
            String sampleCategory = requestData.get("sampleCategory");
            String sampleLeader = requestData.get("sampleLeader");
            String sampleSender = requestData.get("sampleSender");
            String tester = requestData.get("tester");
            String startDate = requestData.get("startDate");
            String endDate = requestData.get("endDate");
            String isUsed = requestData.get("isUsed");
            String isCancel = requestData.get("isCancel");
            String schedule = requestData.get("schedule");
            String remark = requestData.get("remark");
            String dateRangeStart = requestData.get("dateRangeStart");
            String dateRangeEnd = requestData.get("dateRangeEnd");
            String changeRecordRemark = requestData.get("changeRecordRemark");

            List<Map<String, Object>> results = scheduleBoardService.searchByChangeRecordNew(
                ETTestCode, sampleModel, sampleCategory, sampleLeader, sampleSender, tester, startDate, endDate, 
                isUsed, isCancel, schedule, remark, dateRangeStart, dateRangeEnd, changeRecordRemark);
            System.out.println("results:"+results);

            return results;
        } catch (Exception e) {
            System.err.println("新接口处理异常: " + e.getMessage());
            e.printStackTrace();
            throw e; // 重新抛出异常，让Spring处理
        }
    }

    /**
     * 解析变更记录详情字符串
     */
    private List<Map<String, Object>> parseChangeRecordDetail(String changeRecordsDetail) {
        List<Map<String, Object>> records = new ArrayList<>();
        if (changeRecordsDetail == null || changeRecordsDetail.trim().isEmpty()) {
            return records;
        }

        String[] recordArray = changeRecordsDetail.split("###");
        for (String record : recordArray) {
            String[] parts = record.trim().split("\\|");
            if (parts.length >= 7) {
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put("tester", parts[0].equals("null") ? "" : parts[0]);
                recordMap.put("start_date", parts[1].equals("null") ? "" : parts[1]);
                recordMap.put("end_date", parts[2].equals("null") ? "" : parts[2]);
                recordMap.put("update_time", parts[3].equals("null") ? "" : parts[3]);
                recordMap.put("schedule_color", parts[4].equals("null") ? "" : parts[4]);
                recordMap.put("is_used", parts[5].equals("null") ? "" : parts[5]);
                recordMap.put("remark", parts[6].equals("null") ? "" : parts[6]);
                records.add(recordMap);
            }
        }
        return records;
    }

    /**
     * 测试方法：查看 changeRecord 数据
     */
    @GetMapping("/scheduleBoardController/testChangeRecord")
    @ResponseBody
    public List<Map<String, Object>> testChangeRecord() {
        List<PassbackData> results = scheduleBoardService.testChangeRecord();
        List<Map<String, Object>> formattedResults = new ArrayList<>();
        
        for (PassbackData data : results) {
            Map<String, Object> formattedData = new HashMap<>();
            formattedData.put("sample_id", data.getSample_id());
            formattedData.put("changeRecord", data.getChangeRecord());
            formattedData.put("create_time", data.getCreate_time());
            formattedResults.add(formattedData);
        }
        
        return formattedResults;
    }


    @PostMapping("/scheduleBoardController/getUpdateData")
    @ResponseBody
    public Map<String, Object> getAttendanceList(@RequestBody Map<String, Object> params) {
        List<String> names = (List<String>) params.get("names");
        String startDate = (String) params.get("startDate");
        String endDate = (String) params.get("endDate");

        if (startDate == null || startDate.trim().isEmpty()) {
            return Collections.singletonMap("error", "开始时间不能为空");
        }

        // 如果 endDate 没填，默认为 startDate 当天
        if (endDate == null || endDate.trim().isEmpty()) {
            endDate = startDate;
        }

        // 1. 查询用户ID（假设你有 userService 方法）
        List<String> userIds = new ArrayList<>();
        for (String name : names) {
            String userId = accessTokenService.getUserIdByName(name.trim()); // 自行实现
            if (userId != null) {
                userIds.add(userId);
            }
        }

        if (userIds.isEmpty()) {
            return Collections.singletonMap("error", "未找到对应的用户ID");
        }

        try {
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/attendance/listRecord");
            OapiAttendanceListRecordRequest req = new OapiAttendanceListRecordRequest();
            req.setUserIds(userIds);
            req.setCheckDateFrom(startDate + " 00:00:00");
            req.setCheckDateTo(endDate + " 23:59:59");
            req.setIsI18n(false);

            String accessToken = accessTokenService.getAccessToken(); // 自行实现 token 获取逻辑
            OapiAttendanceListRecordResponse rsp = client.execute(req, accessToken);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", rsp.getBody());
            System.out.println("rsp.getBody():"+rsp.getBody());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.singletonMap("error", "调用钉钉接口失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/scheduleBoardController/deleteGroup/{id}")
    public ResponseEntity<String> deleteGroup(@PathVariable("id") String id) {
        boolean success = scheduleBoardService.deleteGroupById(id);
        System.out.println("success:"+success);
        return success ? ResponseEntity.ok("删除成功") : ResponseEntity.status(500).body("删除失败");
    }


}
