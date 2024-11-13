package com.lu.ddwyydemo04.controller.DQE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lu.ddwyydemo04.Service.DQE.LabModuleService;
import com.lu.ddwyydemo04.pojo.SystemInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LabModuleController {

    @Autowired
    private LabModuleService labModuleService;

    @PostMapping("/uploadSys")
    @ResponseBody
    public ResponseEntity<String> uploadSystemInfo(@RequestBody SystemInfo systemInfo) {
        // 打印接收到的系统信息
        System.out.println("Received System Info:");
        System.out.println("Version: " + systemInfo.getVersion());
        System.out.println("Installation Date: " + systemInfo.getInstallationDate());
        System.out.println("Operating System Version: " + systemInfo.getOsVersion());
        System.out.println("System Architecture: " + systemInfo.getArchitecture());
        System.out.println("Computer Name: " + systemInfo.getComputerName());
        System.out.println("System Model: " + systemInfo.getSystemModel());

        // 调用服务层方法保存系统信息
        int sys = labModuleService.saveSys(systemInfo);
        if (sys > 0) {
            System.out.println("系统信息获取成功");
        } else {
            System.out.println("系统信息保存失败");
        }

        return ResponseEntity.ok("System Information Received Successfully");
    }


}
