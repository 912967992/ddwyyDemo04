package com.lu.ddwyydemo04.controller.DQE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lu.ddwyydemo04.Service.DQE.LabModuleService;
import com.lu.ddwyydemo04.pojo.Samples;
import com.lu.ddwyydemo04.pojo.SystemInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class LabModuleController {

    @Autowired
    private LabModuleService labModuleService;

    //禁用这个方法，现在改成让大哥手动上传管理好点
    @PostMapping("/uploadSysStop")
    @ResponseBody
    public ResponseEntity<String> uploadSystemInfo(@RequestBody SystemInfo systemInfo) {

        // 判断 Computer Name 是否在数据库中存在
        int exists = labModuleService.checkIfComputerNameExists(systemInfo.getComputerName());

        // 如果存在，执行更新逻辑；如果不存在，执行插入逻辑
        int result;
        if (exists>0) {
            // 执行更新操作
            result = labModuleService.updateSys(systemInfo);
            System.out.println("Computer Name exists, updating system information...");
        } else {
            // 执行插入操作
            result = labModuleService.insertSys(systemInfo);
            System.out.println("Computer Name does not exist, inserting system information...");
        }

        if (result > 0) {
            System.out.println("系统信息处理成功");
        } else {
            System.out.println("系统信息处理失败");
        }

        return ResponseEntity.ok("System Information Processed Successfully");
    }


    @GetMapping("/labModule/performSysInfo")
    @ResponseBody
    public List<SystemInfo> performSysInfo(@RequestParam(required = false) String id,
                                           @RequestParam(required = false) String personCharge,
                                          @RequestParam(required = false) String computerName,
                                          @RequestParam(required = false) String brand,
                                          @RequestParam(required = false) String area,
                                          @RequestParam(required = false) String deviceType,
                                          @RequestParam(required = false) String version,
                                          @RequestParam(required = false) String osVersion,
                                          @RequestParam(required = false) String fullOS,
                                          @RequestParam(required = false) String architecture,
                                          @RequestParam(required = false) String cpu,
                                          @RequestParam(required = false) String memory,
                                          @RequestParam(required = false) String displays,
                                          @RequestParam(required = false) String maxResolution,
                                          @RequestParam(required = false) String maxRefreshRate,
                                          @RequestParam(required = false) String interfaceInfo
                                          ) {

        // 将空字符串转换为 null
        id = (id != null && !id.isEmpty()) ? id : null;
        computerName = (computerName != null && !computerName.isEmpty()) ? computerName : null;
        personCharge = (personCharge != null && !personCharge.isEmpty()) ? personCharge : null;
        brand = (brand != null && !brand.isEmpty()) ? brand : null;
        area = (area != null && !area.isEmpty()) ? area : null;
        deviceType = (deviceType != null && !deviceType.isEmpty()) ? deviceType : null;
        version = (version != null && !version.isEmpty()) ? version : null;
        osVersion = (osVersion != null && !osVersion.isEmpty()) ? osVersion : null;
        fullOS = (fullOS != null && !fullOS.isEmpty()) ? fullOS : null;

        architecture = (architecture != null && !architecture.isEmpty()) ? architecture : null;

        cpu = (cpu != null && !cpu.isEmpty()) ? cpu : null;
        memory = (memory != null && !memory.isEmpty()) ? memory : null;
        displays = (displays != null && !displays.isEmpty()) ? displays : null;

        maxResolution = (maxResolution != null && !maxResolution.isEmpty()) ? maxResolution : null;
        maxRefreshRate = (maxRefreshRate != null && !maxRefreshRate.isEmpty()) ? maxRefreshRate : null;

        interfaceInfo = (interfaceInfo != null && !interfaceInfo.isEmpty()) ? interfaceInfo : null;


        List<SystemInfo> systemInfos = labModuleService.performSysInfo(id,personCharge,computerName,brand,area,deviceType,version,osVersion,fullOS,
                architecture,cpu,memory,displays,maxResolution,maxRefreshRate,interfaceInfo);
//        System.out.println(systemInfos);

        return systemInfos;
    }

}
