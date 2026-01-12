package com.lu.ddwyydemo04.controller.DQE;

import com.lu.ddwyydemo04.Service.DQE.ScheduleBoardNasService;
import com.lu.ddwyydemo04.Service.DQE.ScheduleBoardService;
import com.lu.ddwyydemo04.Service.TestManIndexService;
import com.lu.ddwyydemo04.controller.TestEnvironmentController;
import com.lu.ddwyydemo04.pojo.*;
import com.lu.ddwyydemo04.pojo.Holiday;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

// Excel导出相关导入
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
public class ScheduleBoardNASController {

    @Autowired
    private ScheduleBoardNasService scheduleBoardNasService;

    @Autowired
    private ScheduleBoardService scheduleBoardService;

    @Autowired
    private TestManIndexService testManIndexService;

    @Autowired
    private TestEnvironmentController testEnvironmentController;

    @GetMapping("/passback/getReceivedDataNAS")
    @ResponseBody
    public ResponseEntity<List<PassbackData>> getReceivedDataNAS() {
        List<PassbackData> receivedData =  scheduleBoardNasService.getReceivedDataNAS();
        return ResponseEntity.ok(receivedData);
    }

    @PostMapping("/scheduleBoardNASController/refreshColorNAS")
    @ResponseBody
    public Map<String, Object> refreshColorNAS() {
        Map<String, Object> result = new HashMap<>();
        String sample_category = "NAS服务器";
        try {
            int grayCount = scheduleBoardService.updateScheduleColorGray(sample_category);
            int greenCount = scheduleBoardService.updateScheduleColorGreen(sample_category);

            result.put("success", true);
            result.put("message", "刷新完成，灰色更新：" + grayCount + " 条，绿色更新：" + greenCount + " 条");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "刷新颜色失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 导出NAS组排期面板到Excel
     */
    @GetMapping("/scheduleBoardNASController/exportScheduleToExcelNAS")
    public void exportScheduleToExcelNAS(@RequestParam("startDate") String startDate,
                                        @RequestParam("endDate") String endDate,
                                        HttpServletResponse response) throws IOException {
        
        // 获取NAS组的测试人员列表
        List<TestEngineerInfo> nasGroupTesters = getNASGroupTesters();
        
        // 获取所有排期数据
        List<Map<String, Object>> allScheduleData = testEnvironmentController.getScheduleBoardWithTime(startDate, endDate);
        
        // 过滤出NAS组测试人员的排期数据
        List<String> nasGroupTesterNames = nasGroupTesters.stream()
                .map(TestEngineerInfo::getTest_engineer_name)
                .collect(Collectors.toList());
        
        List<Map<String, Object>> scheduleData = allScheduleData.stream()
                .filter(item -> {
                    String tester = (String) item.get("tester");
                    return tester != null && nasGroupTesterNames.contains(tester);
                })
                .collect(Collectors.toList());
        
        List<TestEngineerInfo> testers = nasGroupTesters;
        
        // 获取节假日数据
        List<Holiday> holidays = testManIndexService.getAllHolidays();
        
        // 创建Excel工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("NAS组排期面板");
        
        // 创建样式
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        CellStyle projectStyle = createProjectStyle(workbook);
        
        // 生成日期列表
        List<LocalDate> dateList = generateDateList(startDate, endDate);
        
        // 创建表头
        int rowIndex = 0;
        Row headerRow = sheet.createRow(rowIndex++);
        
        // 第一列：组别
        Cell groupHeaderCell = headerRow.createCell(0);
        groupHeaderCell.setCellValue("组别");
        groupHeaderCell.setCellStyle(headerStyle);
        
        // 第二列：测试人员
        Cell testerHeaderCell = headerRow.createCell(1);
        testerHeaderCell.setCellValue("测试人员");
        testerHeaderCell.setCellStyle(headerStyle);
        
        // 日期列
        for (int i = 0; i < dateList.size(); i++) {
            Cell dateCell = headerRow.createCell(i + 2);
            LocalDate currentDate = dateList.get(i);
            
            // 获取日期显示文本（包含周末和节假日信息）
            String displayText = getDateDisplayText(currentDate, holidays);
            dateCell.setCellValue(displayText);
            
            // 根据日期类型设置不同的样式
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.cloneStyleFrom(dateStyle);
            
            if (isHoliday(currentDate, holidays)) {
                // 节假日：红色背景
                cellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            } else if (isWeekend(currentDate)) {
                // 周末：橙色背景
                cellStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }
            
            dateCell.setCellStyle(cellStyle);
        }
        
        // 创建数据行，保持原有顺序，但按组别分组显示
        String currentGroup = null;
        int groupStartRow = 0;
        
        for (TestEngineerInfo tester : testers) {
            String testerGroup = tester.getResponsible_category();
            if (testerGroup == null || testerGroup.trim().isEmpty()) {
                testerGroup = "未分组";
            }
            
            // 如果组别发生变化，合并上一个组别的单元格
            if (currentGroup != null && !currentGroup.equals(testerGroup)) {
                if (rowIndex - 1 > groupStartRow) {
                    CellRangeAddress groupMergedRegion = new CellRangeAddress(groupStartRow, rowIndex - 1, 0, 0);
                    sheet.addMergedRegion(groupMergedRegion);
                }
                groupStartRow = rowIndex;
            }
            
            // 如果是第一个测试人员，设置起始行
            if (currentGroup == null) {
                groupStartRow = rowIndex;
            }
            
            currentGroup = testerGroup;
            
            Row dataRow = sheet.createRow(rowIndex++);
            
            // 第一列：组别（只在组别变化时设置）
            if (rowIndex - 1 == groupStartRow) {
                Cell groupCell = dataRow.createCell(0);
                groupCell.setCellValue(testerGroup);
                
                // 创建组别样式
                CellStyle groupStyle = workbook.createCellStyle();
                groupStyle.cloneStyleFrom(headerStyle);
                groupStyle.setFillForegroundColor(getGroupBackgroundColor(testerGroup).getIndex());
                groupStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                groupCell.setCellStyle(groupStyle);
            }
            
            // 第二列：测试人员姓名
            Cell testerCell = dataRow.createCell(1);
            testerCell.setCellValue(tester.getTest_engineer_name());
            
            // 创建测试人员样式（使用与组别相同的颜色）
            CellStyle testerStyle = workbook.createCellStyle();
            testerStyle.cloneStyleFrom(headerStyle);
            testerStyle.setFillForegroundColor(getGroupBackgroundColor(testerGroup).getIndex());
            testerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            testerCell.setCellStyle(testerStyle);
            
            // 为每个日期创建单元格，并处理合并单元格
            boolean[] processedCells = new boolean[dateList.size()]; // 标记已处理的单元格
            
            for (int i = 0; i < dateList.size(); i++) {
                if (processedCells[i]) {
                    continue; // 跳过已处理的单元格
                }
                
                Cell cell = dataRow.createCell(i + 2);
                cell.setCellStyle(projectStyle);
                
                // 查找该测试人员在该日期的排期项目
                ProjectScheduleInfo projectInfo = findProjectForTesterAndDate(scheduleData, tester.getTest_engineer_name(), dateList.get(i));
                if (projectInfo != null && !projectInfo.content.isEmpty()) {
                    cell.setCellValue(projectInfo.content);
                    
                    // 根据项目颜色设置单元格背景色
                    setCellBackgroundColor(cell, projectInfo.color, workbook);
                    
                    // 如果项目横跨多天，创建合并单元格
                    if (projectInfo.scheduleDays > 1) {
                        int endColIndex = i + (int) projectInfo.scheduleDays;
                        if (endColIndex > dateList.size()) {
                            endColIndex = dateList.size();
                        }
                        
                        // 创建合并单元格（从当前列到结束列）
                        if (endColIndex > i + 2) {
                            CellRangeAddress mergedRegion = new CellRangeAddress(rowIndex - 1, rowIndex - 1, i + 2, endColIndex + 1);
                            sheet.addMergedRegion(mergedRegion);
                            
                            // 设置合并单元格的样式
                            CellStyle mergedStyle = workbook.createCellStyle();
                            mergedStyle.cloneStyleFrom(cell.getCellStyle());
                            setCellBackgroundColorForStyle(mergedStyle, projectInfo.color, workbook);
                            cell.setCellStyle(mergedStyle);
                            
                            // 标记所有被合并的单元格为已处理
                            for (int j = i; j < i + (int) projectInfo.scheduleDays; j++) {
                                if (j < processedCells.length) {
                                    processedCells[j] = true;
                                }
                            }
                        }
                    } else {
                        processedCells[i] = true;
                    }
                } else {
                    processedCells[i] = true;
                }
            }
        }
        
        // 合并最后一个组别的单元格
        if (currentGroup != null && rowIndex - 1 > groupStartRow) {
            CellRangeAddress groupMergedRegion = new CellRangeAddress(groupStartRow, rowIndex - 1, 0, 0);
            sheet.addMergedRegion(groupMergedRegion);
        }
        
        // 设置列宽（单位：1/256字符宽度）
        sheet.setColumnWidth(0, 3000); // 组别列
        sheet.setColumnWidth(1, 5000); // 测试人员列
        for (int i = 2; i <= dateList.size() + 1; i++) {
            sheet.setColumnWidth(i, 3840); // 日期列 - 15个字符宽度 (15 * 256 = 3840)
        }
        
        // 设置行高（单位：1/20点）
        // 表头行高
        Row headerRowForHeight = sheet.getRow(0);
        if (headerRowForHeight != null) {
            headerRowForHeight.setHeightInPoints(45); // 表头行高45点
        }
        
        // 数据行高
        for (int i = 1; i < rowIndex; i++) {
            Row dataRow = sheet.getRow(i);
            if (dataRow != null) {
                dataRow.setHeightInPoints(45); // 数据行高45点
            }
        }
        
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        
        // 生成文件名并正确编码
        String fileName = "NAS组排期面板_" + startDate + "_" + endDate + ".xlsx";
        try {
            // 对文件名进行URL编码，确保中文字符正确显示
            fileName = java.net.URLEncoder.encode(fileName, "UTF-8");
        } catch (Exception e) {
            // 如果编码失败，使用原始文件名
            fileName = "NAS组排期面板_" + startDate + "_" + endDate + ".xlsx";
        }
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        
        // 写入响应
        workbook.write(response.getOutputStream());
        workbook.close();
    }
    
    /**
     * 生成日期列表
     */
    private List<LocalDate> generateDateList(String startDate, String endDate) {
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        
        LocalDate current = start;
        while (!current.isAfter(end)) {
            dateList.add(current);
            current = current.plusDays(1);
        }
        return dateList;
    }
    
    /**
     * 查找测试人员在指定日期的项目
     */
    private ProjectScheduleInfo findProjectForTesterAndDate(List<Map<String, Object>> scheduleData, String tester, LocalDate date) {
        for (Map<String, Object> item : scheduleData) {
            if (tester.equals(item.get("tester"))) {
                LocalDate startDate = convertToLocalDate(item.get("schedule_start_date"));
                LocalDate endDate = convertToLocalDate(item.get("schedule_end_date"));
                
                if (startDate != null && endDate != null && !date.isBefore(startDate) && !date.isAfter(endDate)) {
                    String sampleName = (String) item.get("sample_name");
                    String sampleModel = (String) item.get("sample_model");
                    String materialCode = (String) item.get("materialCode");
                    String sampleLeader = (String) item.get("sample_leader");
                    String remark = (String) item.get("remark");
                    String color = (String) item.get("schedule_color");
                    
                    // 获取排期天数
                    double scheduleDays = 1.0; // 默认1天
                    Object scheduleDaysObj = item.get("scheduleDays");
                    if (scheduleDaysObj != null) {
                        if (scheduleDaysObj instanceof Number) {
                            scheduleDays = ((Number) scheduleDaysObj).doubleValue();
                        } else if (scheduleDaysObj instanceof String) {
                            try {
                                scheduleDays = Double.parseDouble((String) scheduleDaysObj);
                            } catch (NumberFormatException e) {
                                scheduleDays = 1.0;
                            }
                        }
                    }
                    
                    StringBuilder content = new StringBuilder();
                    if (sampleName != null) content.append(sampleName).append(" ");
                    if (sampleModel != null) content.append(sampleModel).append(" ");
                    if (materialCode != null) content.append(materialCode).append(" ");
                    if (sampleLeader != null) content.append(sampleLeader.charAt(0)).append(" ");
                    if (remark != null) content.append(remark);
                    
                    return new ProjectScheduleInfo(content.toString().trim(), color, scheduleDays);
                }
            }
        }
        return null;
    }
    
    /**
     * 设置单元格背景颜色
     */
    private void setCellBackgroundColor(Cell cell, String color, Workbook workbook) {
        if (color == null || "null".equals(color)) {
            return;
        }
        
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(cell.getCellStyle());
        
        IndexedColors indexedColor;
        switch (color.toLowerCase()) {
            case "gray":
                indexedColor = IndexedColors.GREY_25_PERCENT;
                break;
            case "green":
                indexedColor = IndexedColors.LIGHT_GREEN;
                break;
            case "yellow":
                indexedColor = IndexedColors.YELLOW;
                break;
            case "red":
                indexedColor = IndexedColors.RED;
                break;
            default:
                return;
        }
        
        style.setFillForegroundColor(indexedColor.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cell.setCellStyle(style);
    }
    
    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14); // 增大字体
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.MEDIUM); // 加粗边框
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        // 设置背景色
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
    
    /**
     * 创建日期样式
     */
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12); // 增大字体
        font.setBold(true); // 加粗
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.MEDIUM); // 加粗边框
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        // 设置背景色
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
    
    /**
     * 创建项目样式
     */
    private CellStyle createProjectStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11); // 增大字体
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.MEDIUM); // 加粗边框
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setWrapText(true); // 自动换行
        // 设置内边距
        style.setIndention((short) 2); // 左缩进
        return style;
    }
    
    /**
     * 获取NAS组的测试人员列表
     */
    private List<TestEngineerInfo> getNASGroupTesters() {
        // 获取所有测试人员
        List<TestEngineerInfo> allTesters = scheduleBoardService.getTestEngineers();
        
        // 过滤出组别为"NAS组"的测试人员
        return allTesters.stream()
                .filter(tester -> {
                    String category = tester.getResponsible_category();
                    return category != null && category.equals("NAS组");
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 按照前端相同的顺序获取测试人员列表（保留此方法以防其他地方需要）
     */
    private List<TestEngineerInfo> getTestersInOrder() {
        // 获取组别顺序，只获取is_displayed=1的组别
        List<Group> groups = scheduleBoardService.getAllGroupsOrderByDisplayOrder();
        List<String> groupOrder = groups.stream()
                .filter(group -> group.isIs_displayed()) // 只获取is_displayed=1的组别
                .sorted((a, b) -> Integer.compare(a.getDisplay_order(), b.getDisplay_order()))
                .map(Group::getName)
                .collect(Collectors.toList());
        
        // 获取所有测试人员
        List<TestEngineerInfo> allTesters = scheduleBoardService.getTestEngineers();
        
        // 按组别分组
        Map<String, List<TestEngineerInfo>> groupedTesters = new HashMap<>();
        for (TestEngineerInfo tester : allTesters) {
            String category = tester.getResponsible_category();
            if (category == null || category.trim().isEmpty()) {
                category = "未分组";
            }
            groupedTesters.computeIfAbsent(category, k -> new ArrayList<>()).add(tester);
        }
        
        // 按照groupOrder的顺序重新排列，只包含is_displayed=1的组别
        List<TestEngineerInfo> orderedTesters = new ArrayList<>();
        for (String groupName : groupOrder) {
            List<TestEngineerInfo> groupTesters = groupedTesters.get(groupName);
            if (groupTesters != null) {
                orderedTesters.addAll(groupTesters);
            }
        }
        
        return orderedTesters;
    }
    
    /**
     * 将各种日期类型转换为LocalDate
     */
    private LocalDate convertToLocalDate(Object dateObj) {
        if (dateObj == null) {
            return null;
        }
        
        if (dateObj instanceof LocalDate) {
            return (LocalDate) dateObj;
        } else if (dateObj instanceof java.sql.Date) {
            return ((java.sql.Date) dateObj).toLocalDate();
        } else if (dateObj instanceof java.util.Date) {
            return ((java.util.Date) dateObj).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
        } else if (dateObj instanceof String) {
            try {
                return LocalDate.parse((String) dateObj);
            } catch (Exception e) {
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * 项目排期信息内部类
     */
    private static class ProjectScheduleInfo {
        public final String content;
        public final String color;
        public final double scheduleDays;
        
        public ProjectScheduleInfo(String content, String color, double scheduleDays) {
            this.content = content;
            this.color = color;
            this.scheduleDays = scheduleDays;
        }
    }
    
    /**
     * 为样式设置单元格背景颜色
     */
    private void setCellBackgroundColorForStyle(CellStyle style, String color, Workbook workbook) {
        if (color == null || "null".equals(color)) {
            return;
        }
        
        IndexedColors indexedColor;
        switch (color.toLowerCase()) {
            case "gray":
                indexedColor = IndexedColors.GREY_25_PERCENT;
                break;
            case "green":
                indexedColor = IndexedColors.LIGHT_GREEN;
                break;
            case "yellow":
                indexedColor = IndexedColors.YELLOW;
                break;
            case "red":
                indexedColor = IndexedColors.RED;
                break;
            default:
                return;
        }
        
        style.setFillForegroundColor(indexedColor.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }
    
    /**
     * 获取组别背景颜色
     */
    private IndexedColors getGroupBackgroundColor(String groupName) {
        if (groupName == null) {
            groupName = "未分组";
        }
        
        // 使用组别名称的哈希值来选择颜色，确保相同组别总是相同颜色
        int hash = Math.abs(groupName.hashCode());
        IndexedColors[] groupColors = {
            IndexedColors.LIGHT_BLUE,      // 浅蓝色
            IndexedColors.LIGHT_GREEN,     // 浅绿色
            IndexedColors.LIGHT_ORANGE,    // 浅橙色
            IndexedColors.LIGHT_YELLOW,    // 浅黄色
            IndexedColors.LIGHT_TURQUOISE, // 浅青绿色
            IndexedColors.LIGHT_CORNFLOWER_BLUE, // 浅矢车菊蓝
            IndexedColors.ROSE,            // 玫瑰色
            IndexedColors.LAVENDER         // 薰衣草色
        };
        
        return groupColors[hash % groupColors.length];
    }
    
    /**
     * 判断是否为周末
     */
    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY || 
               date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY;
    }
    
    /**
     * 判断是否为节假日
     */
    private boolean isHoliday(LocalDate date, List<Holiday> holidays) {
        if (holidays == null || holidays.isEmpty()) {
            return false;
        }
        
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return holidays.stream()
                .anyMatch(holiday -> dateStr.equals(holiday.getHoliday_date()));
    }
    
    /**
     * 获取日期显示文本（只显示周末和节假日信息）
     * 如果周六周日跟节假日碰在一起，只显示节假日的名字
     */
    private String getDateDisplayText(LocalDate date, List<Holiday> holidays) {
        String monthDay = date.format(DateTimeFormatter.ofPattern("MM-dd"));
        
        if (isHoliday(date, holidays)) {
            // 查找节假日名称
            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String holidayName = holidays.stream()
                    .filter(holiday -> dateStr.equals(holiday.getHoliday_date()))
                    .map(Holiday::getHoliday_name)
                    .findFirst()
                    .orElse("节假日");
            return monthDay + "\n" + holidayName;
        } else if (isWeekend(date)) {
            String dayOfWeek = getDayOfWeekChinese(date.getDayOfWeek());
            return monthDay + "\n" + dayOfWeek;
        } else {
            // 工作日只显示日期
            return monthDay;
        }
    }
    
    /**
     * 获取中文星期
     */
    private String getDayOfWeekChinese(java.time.DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY: return "周一";
            case TUESDAY: return "周二";
            case WEDNESDAY: return "周三";
            case THURSDAY: return "周四";
            case FRIDAY: return "周五";
            case SATURDAY: return "周六";
            case SUNDAY: return "周日";
            default: return "";
        }
    }

}