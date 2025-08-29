package com.lu.ddwyydemo04.controller.DQE;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiCspaceAddRequest;
import com.dingtalk.api.response.OapiCspaceAddResponse;
import com.dingtalk.api.response.OapiV2DepartmentListsubResponse;
import com.lu.ddwyydemo04.Service.AccessTokenService;
import com.lu.ddwyydemo04.Service.DQE.DQEIndexService;
import com.lu.ddwyydemo04.Service.DQE.DQEproblemMoudleService;
import com.lu.ddwyydemo04.Service.TestManIndexService;
import com.lu.ddwyydemo04.controller.testManIndexController;
import com.lu.ddwyydemo04.pojo.Samples;
import com.lu.ddwyydemo04.pojo.TaskNode;
import com.lu.ddwyydemo04.pojo.TestIssues;
import com.taobao.api.ApiException;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;

import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DQEproblemMoudleController {
    @Autowired
    private DQEproblemMoudleService dqeproblemMoudleService;

    @Autowired
    private DQEIndexService dqeIndexService;

    @Autowired
    private TestManIndexService testManIndexService;

    @Autowired
    private AccessTokenService accessTokenService;
    private static final Logger logger = LoggerFactory.getLogger(testManIndexController.class);

    @Value("${file.storage.issuespath}")
    private String issuespath;

    @Value("${file.storage.imagepath}")
    private String imagepath;

    @Value("${dingtalk.agentid}")
    private String agentid;

    @GetMapping("/problemMoudle/searchTestissues") // 处理页面跳转请求
    @ResponseBody
    public List<TestIssues> searchTestissues() {
        List<TestIssues> resultTestissues = dqeproblemMoudleService.searchTestissues();
        System.out.println(resultTestissues);
        return resultTestissues;
    }

    @GetMapping("/problemMoudle/editClickBtn") // 处理页面跳转请求
    @ResponseBody
    public List<TestIssues> editClickBtn(@RequestParam int sampleId) {

        List<TestIssues> editTestissues = dqeproblemMoudleService.selectTestIssuesFromSampleid(sampleId);
//        System.out.println("editClickBtn获取到数据了:");
//        System.out.println(editTestissues);

        return editTestissues;
    }

    @GetMapping("/problemMoudle/searchSamplesDQE")
    @ResponseBody
    public List<Samples> searchSamplesDQE(@RequestParam(required = false) String sample_id,
                                         @RequestParam(required = false) String full_model,
                                         @RequestParam(required = false) String questStats,
                                         @RequestParam(required = false) String sample_category,
                                         @RequestParam(required = false) String version,
                                         @RequestParam(required = false) String big_species,
                                         @RequestParam(required = false) String small_species,
                                         @RequestParam(required = false) String supplier,
                                         @RequestParam(required = false) String test_Overseas,
                                         @RequestParam(required = false) String sample_DQE,
                                         @RequestParam(required = false) String sample_Developer,
                                         @RequestParam(required = false) String tester,
                                         @RequestParam(required = false) String priority,
                                         @RequestParam(required = false) String sample_leader,
                                         @RequestParam(required = false) String sample_schedule,
                                         @RequestParam(required = false) String result_judge,
                                         @RequestParam(required = false) String rd_result_judge,
                                         @RequestParam(required = false) String key,
                                         @RequestParam(required = false) String problemTimeStart,
                                         @RequestParam(required = false) String problemTimeEnd) {

        // 将空字符串转换为 null
        sample_id = (sample_id != null && !sample_id.isEmpty()) ? sample_id : null;
        full_model = (full_model != null && !full_model.isEmpty()) ? full_model : null;
        questStats = (questStats != null && !questStats.isEmpty()) ? questStats : null;
        sample_category = (sample_category != null && !sample_category.isEmpty()) ? sample_category : null;
        version = (version != null && !version.isEmpty()) ? version : null;
        big_species = (big_species != null && !big_species.isEmpty()) ? big_species : null;
        small_species = (small_species != null && !small_species.isEmpty()) ? small_species : null;
        supplier = (supplier != null && !supplier.isEmpty()) ? supplier : null;
        test_Overseas = (test_Overseas != null && !test_Overseas.isEmpty()) ? test_Overseas : null;
        sample_DQE = (sample_DQE != null && !sample_DQE.isEmpty()) ? sample_DQE : null;
        sample_Developer = (sample_Developer != null && !sample_Developer.isEmpty()) ? sample_Developer : null;
        tester = (tester != null && !tester.isEmpty()) ? tester : null;
        priority = (priority != null && !priority.isEmpty()) ? priority : null;
        sample_schedule = (sample_schedule != null && !sample_schedule.isEmpty()) ? sample_schedule : null;
        result_judge = (result_judge != null && !result_judge.isEmpty()) ? result_judge : null;
        // 最近更新的
        rd_result_judge = (rd_result_judge != null && !rd_result_judge.isEmpty()) ? rd_result_judge : null;
        problemTimeStart = (problemTimeStart != null && !problemTimeStart.isEmpty()) ? problemTimeStart : null;
        problemTimeEnd = (problemTimeEnd != null && !problemTimeEnd.isEmpty()) ? problemTimeEnd : null;

        //20241128新增项目和模糊搜索
        sample_leader = (sample_leader != null && !sample_leader.isEmpty()) ? sample_leader : null;
        key = (key != null && !key.isEmpty()) ? key : null;

        List<Samples> samples = dqeproblemMoudleService.searchSamplesDQE(sample_id, full_model, questStats, sample_category, version,
                big_species, small_species, supplier, test_Overseas,
                sample_DQE, sample_Developer, tester, priority,
                sample_schedule, result_judge,rd_result_judge, problemTimeStart, problemTimeEnd,
                sample_leader,key);
//        System.out.println(samples);

        return samples;
    }

    @GetMapping("/problemMoudle/addNewRow") // 处理页面跳转请求
    @ResponseBody
    public Map<String, Object> addNewRow(@RequestParam int sampleId, @RequestParam(required = false) String username) {
        // 如果提供了用户名，进行权限检查
        if (username != null && !username.trim().isEmpty()) {
            try {
                // 获取项目信息
                List<Samples> samples = dqeproblemMoudleService.querySamples(String.valueOf(sampleId));
                if (samples == null || samples.isEmpty()) {
                    throw new RuntimeException("项目不存在");
                }

                Samples sample = samples.get(0);
                String projectDQE = sample.getSample_DQE();
                String projectRD = sample.getSample_Developer();
                String projectLeader = sample.getSample_leader();

                // 检查用户是否是项目的直接负责人
                boolean isDirectResponsible = username.equals(projectDQE.trim()) ||
                                            username.equals(projectRD.trim()) ||
                                            username.equals(projectLeader.trim());

                if (!isDirectResponsible) {
                    // 检查用户是否是代理人
                    String agentsStr = dqeIndexService.getAgents(username);
                    boolean isAgent = false;

                    if (agentsStr != null && !agentsStr.trim().isEmpty()) {
                        String[] agentArray = agentsStr.split(",");
                        for (String agent : agentArray) {
                            String trimmedAgent = agent.trim();
                            if (!trimmedAgent.isEmpty()) {
                                // 检查代理人是否被授权给项目的DQE、RD或项目负责人
                                if (trimmedAgent.equals(projectDQE) || 
                                    trimmedAgent.equals(projectRD) || 
                                    trimmedAgent.equals(projectLeader)) {
                                    isAgent = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (!isAgent) {
                        throw new RuntimeException("非您的项目，请叫对应负责人设置您为代理人");
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("权限检查失败: " + e.getMessage());
            }
        }

        System.out.println("addNewRow的sampleId:"+sampleId);
        int max_history_id = dqeproblemMoudleService.searchTestIssuesHistroyidFromId(sampleId);
        System.out.println("max_history_id:"+max_history_id);

        Map<String, Object> sample;

        if(max_history_id==0){
            sample = dqeproblemMoudleService.addNewRowHistroyidZero(sampleId);
            sample.put("max_history_id", max_history_id);

        }else{
            sample = dqeproblemMoudleService.addNewRow(sampleId);
        }

        // 生成当前时间的字符串格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentTime = LocalDateTime.now().format(formatter);

        // 添加 created_at 字段
        sample.put("created_at", currentTime);
        System.out.println(sample);

//        // 打印 SQL 语句返回的字段
//        for (Map<String, Object> sample : samples) {
//            System.out.println("full_model: " + sample.get("full_model"));
//            System.out.println("sample_category: " + sample.get("sample_category"));
//            console.log("version: " + sample.get("version"));
//            console.log("max_history_id: " + sample.get("max_history_id"));
//        }


        // 直接返回查询结果（不需要再转换或提取）
        return sample;
    }


    //问题点模块保存问题点修改的方法
    @PostMapping("/problemMoudle/saveChanges")
    @ResponseBody
    public ResponseEntity<String> saveChanges(@RequestBody List<Map<String, Object>> allData,@RequestParam("sampleId") String sampleId,
                                              @RequestParam("username") String username,@RequestParam("job") String job) {
        try {
            // 权限检查
            try {
                // 获取项目信息
                List<Samples> samples = dqeproblemMoudleService.querySamples(sampleId);
                if (samples == null || samples.isEmpty()) {
                    return ResponseEntity.badRequest().body("项目不存在");
                }

                Samples sample = samples.get(0);
                String projectDQE = sample.getSample_DQE();
                String projectRD = sample.getSample_Developer();
                String projectLeader = sample.getSample_leader();

                // 检查用户是否是项目的直接负责人
                boolean isDirectResponsible = username.equals(projectDQE) || 
                                            username.equals(projectRD) || 
                                            username.equals(projectLeader);

                if (!isDirectResponsible) {
                    // 检查用户是否是代理人
                    String agentsStr = dqeIndexService.getAgents(username);
                    boolean isAgent = false;

                    if (agentsStr != null && !agentsStr.trim().isEmpty()) {
                        String[] agentArray = agentsStr.split(",");
                        for (String agent : agentArray) {
                            String trimmedAgent = agent.trim();
                            if (!trimmedAgent.isEmpty()) {
                                // 检查代理人是否被授权给项目的DQE、RD或项目负责人
                                if (trimmedAgent.equals(projectDQE) || 
                                    trimmedAgent.equals(projectRD) || 
                                    trimmedAgent.equals(projectLeader)) {
                                    isAgent = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (!isAgent) {
                        return ResponseEntity.badRequest().body("非您的项目，请叫对应负责人设置您为代理人");
                    }
                }
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("权限检查失败: " + e.getMessage());
            }

//            System.out.println("接收到的数据量: " + allData.size()); // 打印接收到的数据量

            for (Map<String, Object> row : allData) {
//                System.out.println("处理行数据: " + row); // 打印每一行的数据
                // 创建 TestIssue 对象并填充数据
                TestIssues issue = new TestIssues();
                issue.setFull_model((String) row.get("full_model"));
                issue.setSample_stage((String) row.get("sample_stage"));
                issue.setVersion((String) row.get("version"));
                issue.setChip_solution((String) row.get("chip_solution"));
                issue.setTest_platform((String) row.get("test_platform"));
                issue.setTest_device((String) row.get("test_device"));
                issue.setOther_device((String) row.get("other_device"));
                issue.setProblem((String) row.get("problem"));
                issue.setProblemCategory((String) row.get("problemCategory"));
                issue.setResponsibleDepartment((String) row.get("responsibleDepartment"));

//                issue.setProblem_image_or_video((String) row.get("problem_image_or_video"));
                issue.setProblem_time((String) row.get("problem_time"));
                issue.setReproduction_method((String) row.get("reproduction_method"));
                issue.setRecovery_method((String) row.get("recovery_method"));
                issue.setReproduction_probability((String) row.get("reproduction_probability"));
                issue.setDefect_level((String) row.get("defect_level"));
                issue.setCurrent_status((String) row.get("current_status"));
                issue.setComparison_with_previous((String) row.get("comparison_with_previous"));
                issue.setTester((String) row.get("tester"));
                issue.setDqe_and_development_confirm((String) row.get("dqe_and_development_confirm"));
                issue.setImprovement_plan((String) row.get("improvement_plan"));
                issue.setResponsible_person((String) row.get("responsible_person"));
                issue.setPost_improvement_risk((String) row.get("post_improvement_risk"));
                issue.setNext_version_regression_test((String) row.get("next_version_regression_test"));
                issue.setRemark((String) row.get("remark"));

                String createdAtString = (String) row.get("created_at");
                // 将字符串转换为 LocalDateTime
                LocalDateTime createdAt = replaceT(createdAtString);
                issue.setCreated_at(createdAt);

                String dqe_confirm = (String) row.get("dqe_confirm");
                issue.setDqe_confirm(dqe_confirm);

                String dqe_review_atAsString =  (String) row.get("dqe_review_at");
                LocalDateTime dqe_review_at = replaceT(dqe_review_atAsString);
                issue.setDqe_review_at(dqe_review_at);
                issue.setDqe((String) row.get("dqe"));


                String rd_confirm = (String) row.get("rd_confirm");
                issue.setRd_confirm(rd_confirm);

                String rd_review_atAsString =  (String) row.get("rd_review_at");
                LocalDateTime rd_review_at = replaceT(rd_review_atAsString);
                issue.setRd_review_at(rd_review_at);
                issue.setRd((String) row.get("rd"));

                if(job.equals("DQE")){
                    // 检查 dqe_confirm 和 rd_confirm
                    if ("确认".equals(issue.getDqe_confirm())) {
                        issue.setDqe_review_at(LocalDateTime.now()); // 设置当前时间
                        issue.setDqe(username);
                    }
                }else if(job.equals("rd")){
                    if ("确认".equals(rd_confirm)) {
                        issue.setRd_review_at(LocalDateTime.now()); // 设置当前时间
                        issue.setRd(username);
                    }

                }


                issue.setHistory_id((Integer) row.get("history_id"));
                issue.setSample_id(sampleId);
                issue.setCreated_by((String) row.get("created_by"));

                //新增dqe和rd意见等信息
                issue.setDqe_confirm((String) row.get("dqe_confirm"));

                issue.setRd_confirm((String) row.get("rd_confirm"));

                issue.setModifier(username);
                issue.setModify_at(LocalDateTime.now());

                // 其他字段可以继续填充...
                // 20250102 换新模板，新增其他字段
                issue.setSku((String) row.get("sku"));
                issue.setGreen_union_dqe((String) row.get("green_union_dqe"));
                issue.setGreen_union_rd((String) row.get("green_union_rd"));
                issue.setSolution_provider((String) row.get("solution_provider"));
                issue.setSupplier((String) row.get("supplier"));
                issue.setReview_conclusion((String) row.get("review_conclusion"));

                Integer id = (Integer) row.get("id");
                if (id != null) {

                    issue.setId(id.longValue()); // 将 Integer 转换为 Long
                    dqeproblemMoudleService.updateTestIssues(issue);
                } else {

                    dqeproblemMoudleService.insertTestIssues(issue);
                }

                //20241111新增一个保存的时候统计好问题点数量并传到samples表里的problemNumber
                List<Map<String, Object>> countDefectLevel = dqeproblemMoudleService.countDefectLevelsBySampleId(sampleId);
//                System.out.println("countDefectLevel:"+countDefectLevel);
                String problemCounts = dqeproblemMoudleService.formatDefectLevels(countDefectLevel);
//                System.out.println("problemCounts:"+problemCounts);
                // problemCounts打印出来: S:2 A:1 B:0 C:1 待确定:1

                int updateProblemCounts = dqeproblemMoudleService.updatepProblemCounts(sampleId,problemCounts);
                if(updateProblemCounts>0){
                    System.out.println("updateProblemCounts更新成功");
                }

            }

            return ResponseEntity.ok("保存成功"); // 返回成功消息
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("保存失败: " + e.getMessage());
        }
    }


    private LocalDateTime replaceT(String createdAtString){
//        System.out.println("createdAtString:"+createdAtString);
        if (createdAtString == null || createdAtString.isEmpty()) {
            return null; // 返回 null
        }

        // 替换 'T' 为 ' ' 并处理两种日期格式
        createdAtString = createdAtString.replace("T", " ");

        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        // 尝试解析两种格式
        try {
            return LocalDateTime.parse(createdAtString, formatter1);
        } catch (DateTimeParseException e) {
            // 如果第一种格式失败，尝试第二种格式
            return LocalDateTime.parse(createdAtString, formatter2);
        }
    }



    @GetMapping("/problemMoudle/deleteProblemById/{id}/{username}")
    @ResponseBody
    public ResponseEntity<String> deleteProblemById(@PathVariable Long id, @PathVariable String username) {
        try {
            // 权限检查 - 需要先获取问题点所属的项目ID
            Long sampleId = dqeproblemMoudleService.getSampleIdByProblemId(id);
            if (sampleId == null) {
                return ResponseEntity.badRequest().body("问题点不存在");
            }

            // 获取项目信息
            List<Samples> samples = dqeproblemMoudleService.querySamples(String.valueOf(sampleId));
            if (samples == null || samples.isEmpty()) {
                return ResponseEntity.badRequest().body("项目不存在");
            }

            Samples sample = samples.get(0);
            String projectDQE = sample.getSample_DQE();
            String projectRD = sample.getSample_Developer();
            String projectLeader = sample.getSample_leader();

            // 检查用户是否是项目的直接负责人
            boolean isDirectResponsible = username.equals(projectDQE) || 
                                        username.equals(projectRD) || 
                                        username.equals(projectLeader);

            if (!isDirectResponsible) {
                // 检查用户是否是代理人
                String agentsStr = dqeIndexService.getAgents(username);
                boolean isAgent = false;

                if (agentsStr != null && !agentsStr.trim().isEmpty()) {
                    String[] agentArray = agentsStr.split(",");
                    for (String agent : agentArray) {
                        String trimmedAgent = agent.trim();
                        if (!trimmedAgent.isEmpty()) {
                            // 检查代理人是否被授权给项目的DQE、RD或项目负责人
                            if (trimmedAgent.equals(projectDQE) || 
                                trimmedAgent.equals(projectRD) || 
                                trimmedAgent.equals(projectLeader)) {
                                isAgent = true;
                                break;
                            }
                        }
                    }
                }

                if (!isAgent) {
                    return ResponseEntity.badRequest().body("非您的项目，请叫对应负责人设置您为代理人");
                }
            }

            boolean deleted = dqeproblemMoudleService.deleteProblemById(id);
            if (deleted) {
                logger.info("用户 " + username + " 删除 id 为 " + id + " 的问题点成功");
                return ResponseEntity.ok("问题点删除成功");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("问题点未找到");
            }
        } catch (Exception e) {
            logger.error("删除问题点时发生错误: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("删除失败: " + e.getMessage());
        }
    }

//    @PostMapping("/problemMoudle/uploadImage")
//    @ResponseBody
//    public ResponseEntity<Map<String, String>> uploadImage(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam("sampleId") String sampleId,
//            @RequestParam("id") Long issueid
//    ) {
//        System.out.println("靳开来了uploadImage");
//        System.out.println("sampleId：" + sampleId);
//        System.out.println("file：" + file);
//        System.out.println("id：" + issueid);
//        try {
//            // 检查文件是否为空
//            if (file.isEmpty()) {
//                Map<String, String> response = new HashMap<>();
//                response.put("message", "文件不能为空");
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//            }
//
//            // 文件保存路径
//            String fileName = sampleId + "_" + issueid + "." + getFileExtension(file.getOriginalFilename());
//            File destinationFile = new File(issuespath + File.separator + fileName);
//
//            // 保存文件
//            file.transferTo(destinationFile);
//
//            String sqlFileName = "/" + "issuespath" + "/" + fileName;
//            System.out.println("sqlFileName:" + sqlFileName);
//
//            int uploadImageJudge = dqeproblemMoudleService.uploadImage(issueid, sqlFileName);
//            if (uploadImageJudge > 0) {
//                logger.info("序号为" + issueid + "的问题点更新图片路径为" + sqlFileName);
//            } else {
//                logger.info("序号为" + issueid + "的问题点更新图片路径为" + sqlFileName + "失败");
//            }
//
//            Map<String, String> response = new HashMap<>();
//            response.put("message", "图片上传成功，序号为: "+ issueid + "的问题点更新图片路径为" + sqlFileName);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            Map<String, String> response = new HashMap<>();
//            response.put("message", "文件上传失败: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
//    }

    //新增imageMagick对HEIC文件转为png的方式
    @PostMapping("/problemMoudle/uploadImage")
    @ResponseBody
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sampleId") String sampleId,
            @RequestParam("id") Long issueid,
            @RequestParam(required = false) String username
    ) {
        System.out.println("进入 uploadImage");
        System.out.println("sampleId：" + sampleId);
        System.out.println("file：" + file);
        System.out.println("id：" + issueid);
        
        // 权限检查
        if (username != null && !username.trim().isEmpty()) {
            try {
                // 获取项目信息
                List<Samples> samples = dqeproblemMoudleService.querySamples(sampleId);
                if (samples == null || samples.isEmpty()) {
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "项目不存在");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }

                Samples sample = samples.get(0);
                String projectDQE = sample.getSample_DQE();
                String projectRD = sample.getSample_Developer();
                String projectLeader = sample.getSample_leader();

                // 检查用户是否是项目的直接负责人
                boolean isDirectResponsible = username.equals(projectDQE) || 
                                            username.equals(projectRD) || 
                                            username.equals(projectLeader);

                if (!isDirectResponsible) {
                    // 检查用户是否是代理人
                    String agentsStr = dqeIndexService.getAgents(username);
                    boolean isAgent = false;

                    if (agentsStr != null && !agentsStr.trim().isEmpty()) {
                        String[] agentArray = agentsStr.split(",");
                        for (String agent : agentArray) {
                            String trimmedAgent = agent.trim();
                            if (!trimmedAgent.isEmpty()) {
                                // 检查代理人是否被授权给项目的DQE、RD或项目负责人
                                if (trimmedAgent.equals(projectDQE) || 
                                    trimmedAgent.equals(projectRD) || 
                                    trimmedAgent.equals(projectLeader)) {
                                    isAgent = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (!isAgent) {
                        Map<String, String> response = new HashMap<>();
                        response.put("message", "非您的项目，请叫对应负责人设置您为代理人");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }
                }
            } catch (Exception e) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "权限检查失败: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
        
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "文件不能为空");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // 获取文件扩展名并判断是否为 HEIC
            String fileExtension = getFileExtension(file.getOriginalFilename());
            File destinationFile;

            if ("heic".equalsIgnoreCase(fileExtension)) {
                // 如果是 HEIC 文件，则转换为 PNG
                String fileName = sampleId + "_" + issueid + ".png"; // 输出文件名
                destinationFile = new File(issuespath + File.separator + fileName);

                // 保存文件到临时路径
                File tempFile = new File(issuespath + File.separator + "temp_" + file.getOriginalFilename());
                file.transferTo(tempFile);

                // 使用 ImageMagick 转换 HEIC 为 PNG
                convertHeicToPng(tempFile, destinationFile);
                // 删除临时 HEIC 文件
                tempFile.delete();
            } else {
                // 如果不是 HEIC 格式，直接保存文件
                String fileName = sampleId + "_" + issueid + "." + fileExtension;
                destinationFile = new File(issuespath + File.separator + fileName);
                file.transferTo(destinationFile);
            }

            // 保存图片路径到数据库（假设是使用相同的文件路径）
            String sqlFileName = "/" + "issuespath" + "/" + destinationFile.getName();
            System.out.println("sqlFileName:" + sqlFileName);

            int uploadImageJudge = dqeproblemMoudleService.uploadImage(issueid, sqlFileName);
            if (uploadImageJudge > 0) {
                logger.info("序号为" + issueid + "的问题点更新图片路径为" + sqlFileName);
            } else {
                logger.info("序号为" + issueid + "的问题点更新图片路径为" + sqlFileName + "失败");
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "图片上传成功，序号为: "+ issueid + "的问题点更新图片路径为" + sqlFileName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private void convertHeicToPng(File sourceFile, File destinationFile) throws IOException {
        try {
            // ImageMagick的命令行参数
            String[] command = {"magick", sourceFile.getAbsolutePath(), destinationFile.getAbsolutePath()};

            // 执行 ImageMagick 命令
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);  // 将错误流合并到标准输出

            // 启动进程并等待执行
            Process process = processBuilder.start();

            // 获取输出信息
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // 等待进程结束并获取返回码
            int exitCode = process.waitFor();

            // 输出错误信息（如果有）
            if (exitCode != 0) {
                throw new IOException("ImageMagick 转换失败，错误信息: " + output.toString());
            }

            // 正常结束，输出转换结果
            System.out.println("ImageMagick 转换成功: " + output.toString());

        } catch (IOException | InterruptedException e) {
            // 记录详细的异常信息
            throw new IOException("ImageMagick 转换时出现异常: " + e.getMessage(), e);
        }
    }


    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            return ""; // 没有扩展名
        }
        return fileName.substring(dotIndex + 1);
    }



    @PostMapping("/problemMoudle/processConfirm/{sampleId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> processConfirm(@PathVariable String sampleId) {
        Map<String, String> result = dqeproblemMoudleService.searchScheduleAndResultJudge(sampleId);

        return ResponseEntity.ok(result);
    }


    @PostMapping("/problemMoudle/updateResult")
    @ResponseBody
    public int updateResult(@RequestBody Map<String, String> request)  {
        String sampleId = request.get("sample_id");
        String selectedOption = request.get("selectedOption");
        String selectedOptionStr = accessTokenService.returnResult(selectedOption);
        String job = request.get("job");
        String filepath = testManIndexService.queryFilepath(sampleId);
        //20250303 在DQE和研发给出承认结果的时候，把各自的节点信息修改为已完成，这样子就不会警报各自的节点了
        int updateNodeAsFinish = 0;
        if(job.equals("DQE")){
            updateNodeAsFinish = dqeproblemMoudleService.updateNodeAsFinishWithDQE(sampleId);
            String testNumber = testManIndexService.queryElectricIdByActualId(sampleId);;
            Map<String, Object> processTestElectricalTest = testManIndexService.processTestElectricalTest(testNumber,selectedOptionStr,filepath);
            logger.info("推送DQE审核结果远程成功: {}", processTestElectricalTest.get("remoteBody"));
        }else if(job.equals("rd")){
            updateNodeAsFinish = dqeproblemMoudleService.updateNodeAsFinishWithRD(sampleId);
            String reportReviewTime = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);  // 输出 2025-04-14T16:34:23
            String testNumber = testManIndexService.queryElectricIdByActualId(sampleId);;
            testManIndexService.updateRdElectricInfoReview(testNumber,reportReviewTime,selectedOptionStr);

        }


        //查看DQE和研发是否都给出了承认结果
        int updateResult = dqeproblemMoudleService.updateResult(sampleId , job, selectedOption);
//        System.out.println("updateResult:"+updateResult);
        int allHaveValue = dqeproblemMoudleService.queryResults(sampleId);

        return allHaveValue;
    }

    // 待DQE审核进来的流程确认方法
    @PostMapping("/problemMoudle/updateSchedule")
    @ResponseBody
    public ResponseEntity<String> updateSchedule(@RequestBody Map<String, String> request) throws ApiException, UnsupportedEncodingException {

        String sampleSchedule = request.get("sample_schedule");
        String sampleId = request.get("sample_id");
        String statusBarBgColor = request.get("statusBarBgColor");
        String selectedOption = request.get("selectedOption");
        String isReceiverTester = request.get("isReceiverTester");

        String job = request.get("job");
        String sender = request.get("sender");
        String receiver = request.get("receiver");

//        绿色 0xFF78C06E，红色 0xFFF65E5E，橙色 0xFFFF9D46
        String statusBg ="0xFF78C06E";

        Long dept_id = null;
        String setting_role = "";

        String node_number = "";

        //62712385 -->产品研发部，523528658 --> 电子DQE组， 523459714 --》电子测试组
        //20250228 简化审核流程
        if(sampleSchedule.equals("2")){
            setting_role = "okManager";
        }else if(sampleSchedule.equals("9") || sampleSchedule.equals("10") ){
            setting_role = "okManager";
        }else if (sampleSchedule.equals("1")){
            if(job.equals("DQE")){
                setting_role = "dqeManager";
            }else if(job.equals("rd")){
                setting_role = "rdManager";
            }
        }


        List<Samples> sampleList = dqeproblemMoudleService.querySamples(sampleId);
        // 检查列表是否为空并获取第一条记录
        Samples sample = sampleList.isEmpty() ? null : sampleList.get(0);

        if(sample!=null){
            sample.setSample_schedule(sampleSchedule);
        }

        //从数据库获取警示时间设置的天数
        String notify_days_str = dqeproblemMoudleService.queryWarnDays(setting_role);
        int notify_days;
        try {
            notify_days = Integer.parseInt(notify_days_str);
        } catch (NumberFormatException e) {
            // 处理转换失败的情况，例如设置一个默认值或抛出异常
            notify_days = 0; // 默认值
        }

        // 更新 schedule
        int isUpdated = dqeproblemMoudleService.updateSampleWithSchAndResult(sampleId, sampleSchedule, job, selectedOption);
        if(isUpdated>0){
//            System.out.println("isUpdated状态修改成功");
            if(selectedOption!=null){
                if(sample!=null){
                    if(job.equals("DQE")){
                        sample.setResult_judge(selectedOption);
                    }else if(job.equals("rd")){
                        sample.setRd_result_judge(selectedOption);
                    }
                }

            }

        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 获取当前时间并格式化为字符串
        LocalDateTime notifyTime = LocalDateTime.now();
        String notify_time = notifyTime.format(formatter);
//        System.out.println("notify_time: " + notify_time);

        // 计算 warn_time
        String warn_time = null;
        if (notify_days > 0) {
            LocalDateTime warnTime = notifyTime.plusDays(notify_days);

            // 如果 notifyTime 是星期五，则在 warnTime 上多加两天
            if (notifyTime.getDayOfWeek() == DayOfWeek.FRIDAY || notifyTime.getDayOfWeek() == DayOfWeek.SATURDAY) {
                warnTime = warnTime.plusDays(2);
            }

            warn_time = warnTime.format(formatter);
//            System.out.println("warn_time: " + warn_time);
        }

//        Long task_id  = accessTokenService.findUserIdByUsernameInDeptHierarchy(receiver , sample ,statusBarBgColor,sender,
//                notify_time,warn_time,"");
        Map<String, Object> resultMap = accessTokenService.findUserIdByUsernameInDeptHierarchy(
                receiver, sample, statusBarBgColor, sender, notify_time, warn_time, "");

        Long task_id = (Long) resultMap.get("taskId");
        String messageUrl = (String) resultMap.get("messageUrl");


        // 如果找到匹配的用户ID，执行更新逻辑，否则返回失败消息
        if (task_id != null && task_id != 1L) {
            TaskNode taskNode = new TaskNode();
            taskNode.setTask_id(task_id);
            taskNode.setSample_id(Integer.parseInt(sampleId));

            if(job.equals("DQE") && sampleSchedule.equals("1")){
                node_number = "1"; //node_number =1 是DQE审核的信息节点
            }else if(job.equals("rd") && sampleSchedule.equals("1")){
                node_number = "2";  //node_number=2 是研发审核的信息节点
            }else if(sampleSchedule.equals("2")){
                node_number = "3";  //node_number=3则是节点已完成
            }

            taskNode.setNode_number(node_number);
            taskNode.setStatus_value(accessTokenService.returnSchedule(sampleSchedule));
            taskNode.setCreate_time(notify_time);
            taskNode.setWarn_time(warn_time);

            //插入新节点之前先通过sampleSchedule>1则证明有之前的节点，则需要先对旧节点进行更新
            if(Integer.parseInt(sampleSchedule)>1){
                int updatePreviousNodes = accessTokenService.updatePreviousNodes(taskNode);
                if(updatePreviousNodes>0){
                    logger.info(sampleId+"的旧节点更新成功");
                }
                // 处理逻辑，首先获取所有的 task_id
                List<Long> taskIds = accessTokenService.selectTaskId(sampleId);
//                System.out.println("taskIds:"+taskIds);
                for(Long taskId:taskIds){
                    // 更新状态栏信息，用for循环把所有的节点的task_id状态栏都更新为最新
                    String updateStatusBar = accessTokenService.updateStatusBar(taskId,sampleSchedule,statusBg,sample);
                    logger.info("更新之前的节点返回信息:"+updateStatusBar);
                }

            }

            // 暂停2秒
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                // 可以根据需要处理 InterruptedException
            }

            // 插入新节点到数据库
            int insertTaskNode = accessTokenService.insertTaskNode(taskNode);


            String getSchedule = accessTokenService.getOASchedule(task_id);
//            System.out.println("222222:"+sampleSchedule);
            // 修改一下这里，我希望
            if (sampleSchedule.equals("1") && getSchedule.equals("ok")) {
//                System.out.println("dddd");
                String content = "您好！电气性能测试报告已完成，请点击以下链接跳转审核：\n"+ messageUrl;
                return ResponseEntity.ok("copyable|" + content);
            }
            else if (getSchedule.equals("ok")) {
                return ResponseEntity.ok("进度更新成功！OA消息已发送给 " + receiver);
            } else {
                return ResponseEntity.status(400).body("Failed to update schedule");
            }
        } else {
            logger.info(sampleId+"的消息发送失败,没有找到"+receiver);
            return ResponseEntity.status(404).body("已进入下一个流程，但是未找到与 " + receiver + " 匹配的用户，无法发送OA消息通知,建议撤回重新提交");

        }
    }

    @PostMapping("/problemMoudle/exportProblemXlsx")
    @ResponseBody
    public Map<String, String> exportProblemXlsx(@RequestBody Map<String, List<TestIssues>> request,
                                                 @RequestParam("dirId") String dirId,
                                                 @RequestParam("spaceId") String spaceId,
                                                 @RequestParam("receiverId") String receiverId,
                                                 @RequestParam("authCode") String authCode) throws IOException {
        List<TestIssues> issues = request.get("issues"); // 从 Map 中获取 issues
//        System.out.println("issues:"+issues);
        Map<String, String> response = new HashMap<>();

        // 创建 Excel 工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("测试问题点汇总");

        // 创建一个样式，用于居中对齐
        CellStyle centeredStyle = workbook.createCellStyle();
        centeredStyle.setAlignment(HorizontalAlignment.CENTER);
        centeredStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 设置边框
        centeredStyle.setBorderTop(BorderStyle.THIN);
        centeredStyle.setBorderBottom(BorderStyle.THIN);
        centeredStyle.setBorderLeft(BorderStyle.THIN);
        centeredStyle.setBorderRight(BorderStyle.THIN);

        // 设置列宽和行高
        for (int i = 0; i < 33; i++) {
            sheet.setColumnWidth(i, 20 * 256); // 设置列宽
        }

        // 创建表头

        // 在第一行添加标题“测试问题点汇总”
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(30); // 设置行高
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("测试问题点汇总");

// 设置标题样式（居中 + 加粗 + 字号 + 边框）
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setFontHeightInPoints((short) 20);
        titleFont.setBold(true);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleStyle.setBorderTop(BorderStyle.THIN);
        titleStyle.setBorderBottom(BorderStyle.THIN);
        titleStyle.setBorderLeft(BorderStyle.THIN);
        titleStyle.setBorderRight(BorderStyle.THIN);
        titleCell.setCellStyle(titleStyle);

// 合并单元格（比如从第0列到第32列）
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 32));


        Row headerRow = sheet.createRow(1); // 表头移到第二行

        headerRow.setHeightInPoints(50); // 设置表头行高
//        String[] headers = {
//                "ID", "DQE确认", "DQE意见信息", "研发确认", "研发意见信息",
//                "完整型号", "样品阶段", "版本", "芯片方案", "测试平台",
//                "显示设备", "其他设备", "问题描述", "问题图片", "问题时间",
//                "复现方法", "恢复方法", "复现概率", "缺陷等级", "当前状态",
//                "对比上一版或竞品", "提出人", "DQE&研发确认", "改善对策（研发回复）",
//                "分析责任人", "改善后风险", "下一版回归测试", "备注", "创建时间",
//                "历史ID", "问题点创建者", "最后一次改动者", "改动时间"
//        };

        // 修改新的导出问题点的列名：
        String[] headers = {
                "序号", "样品型号", "SKU", "样品阶段", "内/外贸",
                "供应商", "版本", "芯片方案", "日期", "测试人员",
                "测试平台", "显示设备", "其他设备", "问题点", "问题类别",
                "责任单位", "问题视频或图片", "复现概率", "复现手法", "恢复方法",
                "缺陷等级", "当前状态", "对比上一版或竞品", "DQE确认回复（每个版本的回复请勿删除）",
                "研发确认回复（每个版本的回复请勿删除）" , "DQE责任人", "分析责任人", "改善后风险", "评审结论",
                "下一版回归测试", "备注"
        };



        // 创建表头并应用居中样式
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(centeredStyle);
        }

        // 填充数据
        int rowNum = 2;
        for (TestIssues issue : issues) {
            Row row = sheet.createRow(rowNum++);
            row.setHeightInPoints(50); // 设置每行行高
            createCell(row, 0, issue.getId(), centeredStyle);
            createCell(row, 1, issue.getFull_model(), centeredStyle);
            createCell(row, 2, issue.getSku(), centeredStyle);
            createCell(row, 3, issue.getSample_stage(), centeredStyle);
            createCell(row, 4, issue.getTest_Overseas(), centeredStyle);
            createCell(row, 5, issue.getSupplier(), centeredStyle);
            createCell(row, 6, issue.getVersion(), centeredStyle);
            createCell(row, 7, issue.getChip_solution(), centeredStyle);
            createCell(row, 8, issue.getProblem_time(), centeredStyle);
            createCell(row, 9, issue.getTester(), centeredStyle);
            createCell(row, 10, issue.getTest_platform(), centeredStyle);
            createCell(row, 11, issue.getTest_device(), centeredStyle);
            createCell(row, 12, issue.getOther_device(), centeredStyle);

            createCell(row, 13, issue.getProblem(), centeredStyle);
            createCell(row, 14, issue.getProblemCategory(), centeredStyle);
            createCell(row, 15, issue.getResponsibleDepartment(), centeredStyle);
//            createCell(row, 15, issue.getProblem_image_or_video(), centeredStyle);

            // 处理问题图片（略去具体实现）

            // 处理问题图片
            String problemImage = issue.getProblem_image_or_video();
            if (problemImage != null && (problemImage.contains("/imageDirectory/") || problemImage.contains("/issuespath/"))) {
                String filePath;
                if (problemImage.contains("/imageDirectory/")) {
                    filePath = imagepath + problemImage.replace("imageDirectory/", "");
                } else {
                    filePath = issuespath + problemImage.replace("issuespath/", "");
                }

                // 读取图片文件并插入
                try (InputStream is = new FileInputStream(filePath);
                     ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    // 将图片添加到工作簿中
                    String imageFormat = problemImage.substring(problemImage.lastIndexOf(".") + 1);
                    int pictureType;
                    if ("png".equalsIgnoreCase(imageFormat)) {
                        pictureType = Workbook.PICTURE_TYPE_PNG;
                    } else if ("jpeg".equalsIgnoreCase(imageFormat) || "jpg".equalsIgnoreCase(imageFormat)) {
                        pictureType = Workbook.PICTURE_TYPE_JPEG;
                    } else {
                        throw new IllegalArgumentException("Unsupported image format: " + imageFormat);
                    }
                    int pictureIndex = workbook.addPicture(baos.toByteArray(), pictureType);
                    Drawing<?> drawing = sheet.createDrawingPatriarch();
                    // 创建图片锚点
                    ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, (short) 16, rowNum - 1, (short) 17, rowNum);
                    // 插入图片
                    drawing.createPicture(anchor, pictureIndex);
                } catch (IOException e) {
                    e.printStackTrace();
                    createCell(row, 16, "图片加载失败", centeredStyle);
                }
            } else {
                createCell(row, 16, problemImage, centeredStyle);
            }

//            createCell(row, 14, issue.getProblem_time(), centeredStyle);
//            createCell(row, 15, issue.getReproduction_method(), centeredStyle);
//            createCell(row, 16, issue.getRecovery_method(), centeredStyle);
//            createCell(row, 17, issue.getReproduction_probability(), centeredStyle);
//            createCell(row, 18, issue.getDefect_level(), centeredStyle);
//            createCell(row, 19, issue.getCurrent_status(), centeredStyle);
//            createCell(row, 20, issue.getComparison_with_previous(), centeredStyle);
//            createCell(row, 21, issue.getTester(), centeredStyle);
//            createCell(row, 22, issue.getDqe_and_development_confirm(), centeredStyle);
//            createCell(row, 23, issue.getImprovement_plan(), centeredStyle);
//            createCell(row, 24, issue.getResponsible_person(), centeredStyle);
//            createCell(row, 25, issue.getPost_improvement_risk(), centeredStyle);
//            createCell(row, 26, issue.getNext_version_regression_test(), centeredStyle);
//            createCell(row, 27, issue.getRemark(), centeredStyle);
//            createCell(row, 28, issue.getCreated_at() != null ? issue.getCreated_at().toString() : "", centeredStyle);
//            createCell(row, 29, issue.getHistory_id(), centeredStyle);
//            createCell(row, 30, issue.getCreated_by(), centeredStyle);
//            createCell(row, 31, issue.getModifier(), centeredStyle);
//            createCell(row, 32, issue.getModify_at() != null ? issue.getModify_at().toString() : "", centeredStyle);
            createCell(row, 17, issue.getReproduction_probability(), centeredStyle);
            createCell(row, 18, issue.getReproduction_method(), centeredStyle);
            createCell(row, 19, issue.getRecovery_method(), centeredStyle);
            createCell(row, 20, issue.getDefect_level(), centeredStyle);
            createCell(row, 21, issue.getCurrent_status(), centeredStyle);
            createCell(row, 22, issue.getComparison_with_previous(), centeredStyle);
            createCell(row, 23, issue.getGreen_union_dqe(), centeredStyle);
            createCell(row, 24, issue.getGreen_union_rd(), centeredStyle);
            createCell(row, 25, issue.getDqe(), centeredStyle);
            createCell(row, 26, issue.getResponsible_person(), centeredStyle);
            createCell(row, 27, issue.getPost_improvement_risk(), centeredStyle);
            createCell(row, 28, issue.getReview_conclusion(), centeredStyle);
            createCell(row, 29, issue.getNext_version_regression_test(), centeredStyle);
            createCell(row, 30, issue.getRemark(), centeredStyle);
        }

        // 创建文件名
        TestIssues firstIssue = issues.get(0);
        String fileName = String.format("%s_%s_%s问题点.xlsx",
                sanitizeFileName(firstIssue.getFull_model()),
                sanitizeFileName(firstIssue.getSample_stage()),
                sanitizeFileName(firstIssue.getVersion())
        );


        // 保存到临时文件
        String tempFilePath = System.getProperty("java.io.tmpdir") + "/" + fileName;
        System.out.println("fileName:"+fileName);
        try (FileOutputStream fileOut = new FileOutputStream(tempFilePath)) {
            workbook.write(fileOut);
        } finally {
            workbook.close();
        }

        // 上传文件到钉盘
        try {
            String accessToken = accessTokenService.getAccessToken();
            String media_id = testManIndexService.getMediaId(tempFilePath, accessToken,agentid);
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/cspace/add");
            OapiCspaceAddRequest req = new OapiCspaceAddRequest();
            req.setAgentId(agentid);
            req.setCode(authCode);
            req.setFolderId(dirId);
            req.setMediaId(media_id);
            req.setSpaceId(spaceId);
            req.setName(fileName);
            req.setOverwrite(true);
            req.setHttpMethod("GET");
            OapiCspaceAddResponse rsp = client.execute(req, accessToken);
            logger.info("Response from DingTalk cspace add: {}", rsp.getBody());

            // 发送钉盘文件给用户
            testManIndexService.sendDingFileToUser(accessToken, fileName, media_id, receiverId,agentid);

            response.put("status", "发送成功");
            response.put("filePath", tempFilePath); // 返回临时文件路径
            response.put("fileName", fileName); // 返回文件名

            logger.info("exportProblemXlsx successfully.");
        } catch (ApiException e) {
            e.printStackTrace();
            response.put("status", "发送失败");
            logger.info("exportProblemXlsx fail.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return response;
    }

    /**
     * 清理文件名：移除或替换操作系统不允许的字符
     */
    private String sanitizeFileName(String input) {
        if (input == null) {
            return "unknown";
        }
        // 替换 Windows/Linux 文件名中不允许的字符
        return input.replaceAll("[\\\\/:*?\"<>\\|\\r\\n]", "_")
                .replaceAll("\\s+", "_")  // 多个空白字符（包括空格、制表符）替换为单个下划线
                .trim()
                .replaceAll("_+", "_");  // 去除连续多个下划线
    }



    // 辅助方法用于创建单元格并设置样式
    private void createCell(Row row, int columnIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else {
            cell.setCellValue(value != null ? value.toString() : "");
        }
        cell.setCellStyle(style); // 应用样式
    }


    @GetMapping("/problemMoudle/getFilePath")
    @ResponseBody
    public ResponseEntity<?> getFilePath(@RequestParam String sampleId) {
        try {
            // 从数据库获取文件路径
            String filePath = dqeproblemMoudleService.getFilePathBySampleId(sampleId);
            System.out.println("filePath:"+filePath);

            if (filePath != null) {
                // 从 filePath 提取文件名
                String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1);


                // 返回文件路径和文件名
                Map<String, String> response = new HashMap<>();
                response.put("filePath", filePath);  // 文件路径
                response.put("fileName", fileName);  // 文件名
                System.out.println("filePath:"+filePath);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("文件未找到");
            }
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("服务器错误");
        }
    }


    @GetMapping("/problemMoudle/queryResultJudge")
    @ResponseBody
    public ResponseEntity<Boolean> queryResultJudge(@RequestParam("sample_id") String sample_id) {
        try {
            String hasValue = dqeproblemMoudleService.queryResultJudge(sample_id);

            // 只要 hasValue 不为 null，就返回 true
            boolean result = (hasValue != null);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @GetMapping("/problemMoudle/checkPassbackConfirm")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkPassbackConfirm(@RequestParam String sampleId) {
        try {
            // 获取项目的回传确认状态
            String passbackConfirm = dqeproblemMoudleService.queryPassbakConfirm(sampleId);
            String tester = testManIndexService.queryTester(sampleId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("passbackConfirm", passbackConfirm);
            response.put("tester", tester);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "检查回传状态时发生错误: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/problemMoudle/checkAccessPermission")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkAccessPermission(@RequestParam String sampleId, @RequestParam String username) {
        try {
            // 获取项目信息
            List<Samples> samples = dqeproblemMoudleService.querySamples(sampleId);
            if (samples == null || samples.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "项目不存在");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if ("卢健".equals(username) || "李良健".equals(username) || "张华".equals(username)
            || "黄家灿".equals(username) || "荣成彧".equals(username) || "钟海龙".equals(username)
            || "肖政文".equals(username)) {

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("hasPermission", true);
                response.put("message", "您是系统默认管理员，拥有访问权限");
                return ResponseEntity.ok(response);
            }

            Samples sample = samples.get(0);
            String projectDQE = sample.getSample_DQE();
            String projectRD = sample.getSample_Developer();
            String projectLeader = sample.getSample_leader();

            // 检查用户是否是项目的直接负责人
            boolean isDirectResponsible = username.equals(projectDQE.trim()) ||
                                        username.equals(projectRD.trim()) ||
                                        username.equals(projectLeader.trim());
//            System.out.println("isDirectResponsible:"+isDirectResponsible);

            if (isDirectResponsible) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("hasPermission", true);
                response.put("message", "您是该项目的直接负责人，可以访问");
                return ResponseEntity.ok(response);
            }


            // 检查用户是否是代理人
            String agentsStr = dqeIndexService.getAgents(username);
            boolean isAgent = false;
            String agentFor = "";

            if (agentsStr != null && !agentsStr.trim().isEmpty()) {
                String[] agentArray = agentsStr.split(",");
                for (String agent : agentArray) {
                    String trimmedAgent = agent.trim();
                    if (!trimmedAgent.isEmpty()) {
                        // 检查代理人是否被授权给项目的DQE、RD或项目负责人
                        if (trimmedAgent.equals(projectDQE.trim()) ||
                            trimmedAgent.equals(projectRD.trim()) ||
                            trimmedAgent.equals(projectLeader.trim())) {
                            isAgent = true;
                            agentFor = trimmedAgent;
                            break;
                        }
                    }
                }
            }

            if (isAgent) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("hasPermission", true);
                response.put("message", "您是该项目的代理人，可以访问");
                response.put("agentFor", agentFor);
                return ResponseEntity.ok(response);
            }

            // 用户没有权限
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("hasPermission", false);
            response.put("message", "非您的项目，请叫对应负责人设置您为代理人");
            response.put("projectDQE", projectDQE);
            response.put("projectRD", projectRD);
            response.put("projectLeader", projectLeader);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "检查权限时发生错误: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    


}
