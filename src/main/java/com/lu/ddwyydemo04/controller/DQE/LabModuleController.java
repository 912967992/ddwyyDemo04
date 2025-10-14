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

        // 判断实验室名称是否在数据库中存在
        int exists = labModuleService.checkIfComputerNameExists(systemInfo.getDeviceName());

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
                                          @RequestParam(required = false) String deviceCategory,
                                          @RequestParam(required = false) String storageArea,
                                          @RequestParam(required = false) String brand,
                                          @RequestParam(required = false) String deviceName,
                                          @RequestParam(required = false) String modelNumber,
                                          @RequestParam(required = false) String interfaceTypeAndQuantity,
                                          @RequestParam(required = false) String graphicsInterfaceTypeAndQuantity,
                                          @RequestParam(required = false) String maxOutputSpec,
                                          @RequestParam(required = false) String screenSize,
                                          @RequestParam(required = false) String screenRatio,
                                          @RequestParam(required = false) String releaseDate,
                                          @RequestParam(required = false) String devicePurchaseDate,
                                          @RequestParam(required = false) String deviceRepairHistory,
                                          @RequestParam(required = false) String source,
                                          @RequestParam(required = false) String graphicsSource
                                          ) {

        // 将空字符串转换为 null
        id = (id != null && !id.isEmpty()) ? id : null;
        deviceCategory = (deviceCategory != null && !deviceCategory.isEmpty()) ? deviceCategory : null;
        storageArea = (storageArea != null && !storageArea.isEmpty()) ? storageArea : null;
        brand = (brand != null && !brand.isEmpty()) ? brand : null;
        deviceName = (deviceName != null && !deviceName.isEmpty()) ? deviceName : null;
        modelNumber = (modelNumber != null && !modelNumber.isEmpty()) ? modelNumber : null;
        interfaceTypeAndQuantity = (interfaceTypeAndQuantity != null && !interfaceTypeAndQuantity.isEmpty()) ? interfaceTypeAndQuantity : null;
        graphicsInterfaceTypeAndQuantity = (graphicsInterfaceTypeAndQuantity != null && !graphicsInterfaceTypeAndQuantity.isEmpty()) ? graphicsInterfaceTypeAndQuantity : null;
        maxOutputSpec = (maxOutputSpec != null && !maxOutputSpec.isEmpty()) ? maxOutputSpec : null;
        screenSize = (screenSize != null && !screenSize.isEmpty()) ? screenSize : null;
        screenRatio = (screenRatio != null && !screenRatio.isEmpty()) ? screenRatio : null;
        releaseDate = (releaseDate != null && !releaseDate.isEmpty()) ? releaseDate : null;
        devicePurchaseDate = (devicePurchaseDate != null && !devicePurchaseDate.isEmpty()) ? devicePurchaseDate : null;
        deviceRepairHistory = (deviceRepairHistory != null && !deviceRepairHistory.isEmpty()) ? deviceRepairHistory : null;
        source = (source != null && !source.isEmpty()) ? source : null;
        graphicsSource = (graphicsSource != null && !graphicsSource.isEmpty()) ? graphicsSource : null;

        List<SystemInfo> systemInfos = labModuleService.performSysInfo(id, deviceCategory, storageArea, brand, deviceName, modelNumber,
                interfaceTypeAndQuantity, graphicsInterfaceTypeAndQuantity, maxOutputSpec, screenSize, screenRatio,
                releaseDate, devicePurchaseDate, deviceRepairHistory, source, graphicsSource);
//        System.out.println(systemInfos);

        return systemInfos;
    }

}
