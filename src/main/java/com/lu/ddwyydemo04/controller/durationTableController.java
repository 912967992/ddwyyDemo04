package com.lu.ddwyydemo04.controller;

import com.lu.ddwyydemo04.Service.DurationTableService;

import com.lu.ddwyydemo04.pojo.Samples;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;


@Controller
public class durationTableController {

    @Autowired
    private DurationTableService durationTableService;

    @GetMapping("/home") // 处理页面跳转请求
    public String loginHome(String role) {
        // 如果 role 为 null 或为空字符串，默认跳转到 "testManIndex"
        if (role == null || role.trim().isEmpty()) {
            return "testManIndex";
        }

        if(role.equals("tester")){
            return "testManIndex";
        }else{
            return "DQEIndex";
        }
        // 返回跳转页面的视图名称

    }


    @GetMapping("/durationTable") // 处理页面跳转请求
    public String loginCloud(String role) {
//        System.out.println(role);
        if(role.equals("DQE")){
            return "DQE/durationTableDQE";
        }else{
            return "durationTable";
        }

    }


//    @GetMapping("/searchSampleTestMan") // 处理页面跳转请求
//    @ResponseBody
//    public List<Samples> searchSampleTestMan() {
//        List<Samples> resultSamples = durationTableService.searchSampleTestMan();
//        System.out.println("resultSamples:"+resultSamples);
//        return resultSamples;
//    }

    @GetMapping("/searchSampleTestMan")
    @ResponseBody
    public List<Samples> searchSampleTestMan(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "problemTimeStart", required = false) String problemTimeStart,
            @RequestParam(value = "problemTimeEnd", required = false) String problemTimeEnd,
            @RequestParam(value = "problemFinishStart", required = false) String problemFinishStart,
            @RequestParam(value = "problemFinishEnd", required = false) String problemFinishEnd,
            @RequestParam(value = "sample_schedule", required = false) String sample_schedule
    ) {
        // 处理空字符串，转换为 null
        if (problemTimeStart != null && problemTimeStart.isEmpty()) {
            problemTimeStart = null;
        }
        if (problemTimeEnd != null && problemTimeEnd.isEmpty()) {
            problemTimeEnd = null;
        }
        if (problemFinishStart != null && problemFinishStart.isEmpty()) {
            problemFinishStart = null;
        }
        if (problemFinishEnd != null && problemFinishEnd.isEmpty()) {
            problemFinishEnd = null;
        }
        if (sample_schedule != null && sample_schedule.isEmpty()) {
            sample_schedule = null;
        }

        System.out.println("problemFinishStart:"+problemFinishStart);
        System.out.println("problemFinishEnd:"+problemFinishEnd);
        System.out.println("sample_schedule:"+sample_schedule);

        List<Samples> resultSamples = durationTableService.searchSampleTestMan(keyword, problemTimeStart, problemTimeEnd, problemFinishStart, problemFinishEnd,
                sample_schedule);
        System.out.println("resultSamples:"+resultSamples);
        return resultSamples;
    }


    @PostMapping("/exportSamples")
    @ResponseBody
    public ResponseEntity<byte[]> exportSamples(@RequestBody List<Map<String, Object>> data) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Samples");

            // 创建列标题
            Row headerRow = sheet.createRow(0);
//            String[] columns = {"ID", "创建时间", "预计完成时间", "实际完成时间", "预计测试时长", "实测时长", "测试人员", "完整编码", "样品阶段","大类","小类", "版本", "样品名称"  ,"样品进度"};
            String[] columns = {"ID", "创建时间",  "预计完成时间（弃用了）","排期开始时间","排期结束时间", "实际完成时间", "排期测试周期(天/8小时)", "实测时长", "测试人员", "完整编码", "样品阶段","大类","小类", "版本", "样品名称"  ,"样品进度"};


            // 创建样式
            CellStyle style = workbook.createCellStyle();
            style.setAlignment(HorizontalAlignment.CENTER); // 水平居中
            style.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(style); // 应用样式
                if(i==0){
                    sheet.setColumnWidth(i, 5 * 256); // 设置列宽
                }else if (i==1 || i==2 || i== 3 || i ==4 || i==5){
                    sheet.setColumnWidth(i, 30 * 256); // 设置列宽
                }
                /*else if (i==4 ||i==5){
                    sheet.setColumnWidth(i, 8 * 256); // 设置列宽
                }*/
                else{
                    sheet.setColumnWidth(i, 20 * 256); // 设置列宽
                }

            }

            // 填充数据
            for (int i = 0; i < data.size(); i++) {
                Map<String, Object> item = data.get(i);
                Row row = sheet.createRow(i + 1);

                for (int j = 0; j < columns.length; j++) {
                    Cell cell = row.createCell(j);
                    Object value = null;

                    switch (j) {
                        case 0: value = item.get("sample_id"); break;
                        case 1:
                            value = item.get("create_time");
                            if (value != null) {
                                // 去掉"T"并格式化
                                value = value.toString().replace("T", " ");
                            }
                            break;
                        case 2: value = item.get("planfinish_time"); break;
                        case 3: value = item.get("scheduleStartTime"); break;
                        case 4: value = item.get("scheduleEndTime"); break;
                        case 5: value = item.get("finish_time"); break;

                        case 6: value = item.get("scheduleTestCycle"); break;
                        case 7: value = item.get("testDuration"); break;
                        case 8: value = item.get("tester"); break;
                        case 9: value = item.get("full_model"); break;
                        case 10: value = item.get("sample_category"); break;
                        case 11: value = item.get("big_species"); break;
                        case 12: value = item.get("small_species"); break;
                        case 13: value = item.get("version"); break;
                        case 14: value = item.get("sample_name"); break;
                        case 15:
                            String scheduleValueStr = (String) item.get("sample_schedule");
                            if (scheduleValueStr != null) {
                                switch (scheduleValueStr) {
                                    case "0":
                                        value = "测试中";
                                        break;
                                    case "1":
                                        value = "待审核";
                                        break;
                                    case "2":
                                        value = "已完成";
                                        break;
                                    default:
                                        value = "未知状态"; // 处理其他可能的值
                                        break;
                                }
                            } else {
                                value = "未知状态"; // 处理为 null 的情况
                            }
                            break;
                    }
                    // 设置单元格值
                    if (j == 6 || j == 7) { // 如果是预计测试时长或实测时长
                        cell.setCellValue(value != null ? Double.parseDouble(value.toString()) : 0);
                    } else {
                        cell.setCellValue(value != null ? value.toString() : "");
                    }
                    cell.setCellStyle(style); // 应用样式
                }
            }

            // 将工作簿写入字节数组输出流
            workbook.write(outputStream);
            byte[] bytes = outputStream.toByteArray();

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=samples.xlsx");
            headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 数据库字段名到注释的映射
    private static final Map<String, String> FIELD_TO_COMMENT_MAP = new HashMap<>();

    static {
        FIELD_TO_COMMENT_MAP.put("sample_id", "样品编号");
        FIELD_TO_COMMENT_MAP.put("sample_model", "大编码");
        FIELD_TO_COMMENT_MAP.put("sample_coding", "小编码");
        FIELD_TO_COMMENT_MAP.put("sample_name", "样品名称");
        FIELD_TO_COMMENT_MAP.put("sample_category", "产品类别");
        FIELD_TO_COMMENT_MAP.put("chip_control", "主控芯片");
        FIELD_TO_COMMENT_MAP.put("version_software", "软件版本");
        FIELD_TO_COMMENT_MAP.put("version_hardware", "硬件版本");
        FIELD_TO_COMMENT_MAP.put("version", "版本号");
        FIELD_TO_COMMENT_MAP.put("test_number", "测试编号");
        FIELD_TO_COMMENT_MAP.put("supplier", "供应商");
        FIELD_TO_COMMENT_MAP.put("big_species", "大类");
        FIELD_TO_COMMENT_MAP.put("create_time", "报告创建时间");
        FIELD_TO_COMMENT_MAP.put("finish_time", "报告完成时间");
        FIELD_TO_COMMENT_MAP.put("result_judge", "判定结果");
        FIELD_TO_COMMENT_MAP.put("test_Overseas", "国内外测试");
        FIELD_TO_COMMENT_MAP.put("sample_schedule", "样品进度");
        FIELD_TO_COMMENT_MAP.put("sample_DQE", "DQE");
        FIELD_TO_COMMENT_MAP.put("sample_Developer", "电子工程师");
        FIELD_TO_COMMENT_MAP.put("tester", "测试人(当前)");
        FIELD_TO_COMMENT_MAP.put("sample_remark", "送样备注");
        FIELD_TO_COMMENT_MAP.put("planfinish_time", "预计完成时间");
        FIELD_TO_COMMENT_MAP.put("filepath", "文件存放路径");
        FIELD_TO_COMMENT_MAP.put("full_model", "完整编码");
        FIELD_TO_COMMENT_MAP.put("tester_teamwork", "合伙测试人(指编辑过此报告的人)");
        FIELD_TO_COMMENT_MAP.put("danger", "失责，事故");
        FIELD_TO_COMMENT_MAP.put("sample_leader", "项目负责人");
        FIELD_TO_COMMENT_MAP.put("priority", "优先级(加急，同步，优先，普通)");
        FIELD_TO_COMMENT_MAP.put("sample_quantity", "送样数量");
        FIELD_TO_COMMENT_MAP.put("sample_frequency", "送样次数");
        FIELD_TO_COMMENT_MAP.put("small_species", "小类");
        FIELD_TO_COMMENT_MAP.put("high_frequency", "是否高频");
        FIELD_TO_COMMENT_MAP.put("questStats", "任务属性：无");
        FIELD_TO_COMMENT_MAP.put("uuid", "设备唯一UUID");
        FIELD_TO_COMMENT_MAP.put("testDuration", "测试时长，单位：天");
        FIELD_TO_COMMENT_MAP.put("planTestDuration", "预计测试时长，单位：天");
        FIELD_TO_COMMENT_MAP.put("rd_result_judge", "研发判定结果");
        FIELD_TO_COMMENT_MAP.put("problemCounts", "问题点数量");
        FIELD_TO_COMMENT_MAP.put("scheduleStartTime", "排期开始时间，具体时间：时分秒");
        FIELD_TO_COMMENT_MAP.put("scheduleEndTime", "排期结束时间，具体时间：时分秒");
        FIELD_TO_COMMENT_MAP.put("scheduleTestCycle", "排期测试周期，天数");
    }

    // 显式定义列的顺序
    private static final List<String> COLUMN_ORDER = Arrays.asList(
            "sample_id", "sample_model", "sample_coding", "sample_name", "sample_category",
            "chip_control", "version_software", "version_hardware", "version", "test_number",
            "supplier", "big_species", "create_time", "finish_time", "result_judge",
            "test_Overseas", "sample_schedule", "sample_DQE", "sample_Developer", "tester",
            "sample_remark", "planfinish_time", "filepath", "full_model", "tester_teamwork",
            "danger", "sample_leader", "priority", "sample_quantity", "sample_frequency",
            "small_species", "high_frequency", "questStats", "uuid", "testDuration",
            "planTestDuration", "rd_result_judge", "problemCounts", "scheduleStartTime",
            "scheduleEndTime", "scheduleTestCycle"
    );

    // 这样就可以确保列顺序是固定的
    @PostMapping("/problemMoudle/exportBtn")
    @ResponseBody
    public ResponseEntity<byte[]> exportProblem(@RequestBody List<Map<String, Object>> data) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Samples");

            System.out.println("data是啊:" + data);

            // 使用显式定义的列顺序
            List<String> columns = new ArrayList<>(COLUMN_ORDER);

            // 创建列标题
            Row headerRow = sheet.createRow(0);

            // 创建样式
            CellStyle style = workbook.createCellStyle();
            style.setAlignment(HorizontalAlignment.CENTER); // 水平居中
            style.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中

            // 创建列标题并使用注释
            for (int i = 0; i < columns.size(); i++) {
                String column = columns.get(i);
                String columnComment = FIELD_TO_COMMENT_MAP.getOrDefault(column, column); // 获取对应的注释，如果没有则使用字段名
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnComment);
                cell.setCellStyle(style);
                sheet.setColumnWidth(i, 20 * 256); // 设置列宽
            }

            // 填充数据
// 填充数据
            for (int i = 0; i < data.size(); i++) {
                Map<String, Object> item = data.get(i);
                Row row = sheet.createRow(i + 1);

                for (int j = 0; j < columns.size(); j++) {
                    String column = columns.get(j);
                    Object value = item.get(column);

                    Cell cell = row.createCell(j);

                    if ("result_judge".equals(column) || "rd_result_judge".equals(column)) { // 判断是否是“判定结果”列
                        if (value != null) {
                            // 根据判定结果的值进行判断并设置描述
                            String resultDescription = "";
                            switch (value.toString()) {
                                case "0":
                                    resultDescription = "签样";
                                    break;
                                case "1":
                                    resultDescription = "退样";
                                    break;
                                case "2":
                                    resultDescription = "限收";
                                    break;
                                case "3":
                                    resultDescription = "特采";
                                    break;
                                case "4":
                                    resultDescription = "会议评审接受";
                                    break;
                                case "5":
                                    resultDescription = "验证样品合格";
                                    break;
                                case "6":
                                    resultDescription = "验证样品不合格";
                                    break;
                                default:
                                    resultDescription = "";
                                    break;
                            }
                            cell.setCellValue(resultDescription); // 设置对应的描述
                        } else {
                            cell.setCellValue(""); // 如果值为 null，则设置为空字符串
                        }
                    }else if("sample_schedule".equals(column)){
                        if (value != null) {
                            // 根据判定结果的值进行判断并设置描述
                            String resultDescription = "";
                            switch (value.toString()) {
                                case "0":
                                    resultDescription = "测试中";
                                    break;
                                case "1":
                                    resultDescription = "待DQE和研发审核";
                                    break;
                                case "2":
                                    resultDescription = "审核完成";
                                    break;
                                case "9":
                                    resultDescription = "测试人员退样";
                                    break;
                                case "10":
                                    resultDescription = "测试人员竞品完成";
                                    break;
                                default:
                                    resultDescription = "";
                                    break;
                            }
                            cell.setCellValue(resultDescription); // 设置对应的描述
                        } else {
                            cell.setCellValue(""); // 如果值为 null，则设置为空字符串
                        }
                    }else {
                        // 其他列处理
                        if (value != null) {
                            // 设置单元格的值
                            if (value instanceof Double || value instanceof Integer) {
                                cell.setCellValue(Double.parseDouble(value.toString()));
                            } else {
                                cell.setCellValue(value.toString());
                            }
                        } else {
                            cell.setCellValue(""); // 如果值为 null，则设置为空字符串
                        }
                    }

                    cell.setCellStyle(style); // 应用样式
                }
            }

            // 将工作簿写入字节数组输出流
            workbook.write(outputStream);
            byte[] bytes = outputStream.toByteArray();

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=samples.xlsx");
            headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}