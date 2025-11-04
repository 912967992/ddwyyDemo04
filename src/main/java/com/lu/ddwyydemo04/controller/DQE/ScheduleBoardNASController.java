package com.lu.ddwyydemo04.controller.DQE;

import com.lu.ddwyydemo04.Service.DQE.ScheduleBoardNasService;
import com.lu.ddwyydemo04.pojo.PassbackData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ScheduleBoardNASController {

    @Autowired
    private ScheduleBoardNasService scheduleBoardNasService;

    @GetMapping("/passback/getReceivedDataNAS")
    @ResponseBody
    public ResponseEntity<List<PassbackData>> getReceivedDataNAS() {
        List<PassbackData> receivedData =  scheduleBoardNasService.getReceivedDataNAS();
//        System.out.println("receivedData:"+receivedData);
        return ResponseEntity.ok(receivedData);
    }








}
