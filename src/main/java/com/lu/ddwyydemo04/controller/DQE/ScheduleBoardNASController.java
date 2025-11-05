package com.lu.ddwyydemo04.controller.DQE;

import com.lu.ddwyydemo04.Service.DQE.ScheduleBoardNasService;
import com.lu.ddwyydemo04.Service.DQE.ScheduleBoardService;
import com.lu.ddwyydemo04.pojo.PassbackData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ScheduleBoardNASController {

    @Autowired
    private ScheduleBoardNasService scheduleBoardNasService;

    @Autowired
    private ScheduleBoardService scheduleBoardService;

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








}
