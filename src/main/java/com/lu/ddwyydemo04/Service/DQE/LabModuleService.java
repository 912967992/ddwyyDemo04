package com.lu.ddwyydemo04.Service.DQE;

import com.lu.ddwyydemo04.dao.DQE.DQEDao;
import com.lu.ddwyydemo04.pojo.SystemInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LabModuleService {
    @Autowired
    private DQEDao dqeDao;

    public int insertSys(SystemInfo systemInfo){
        return dqeDao.insertSys(systemInfo);
    }
    public int checkIfComputerNameExists(String computerName){
        return dqeDao.checkIfComputerNameExists(computerName);
    }


    public int updateSys(SystemInfo systemInfo){
        return dqeDao.updateSys(systemInfo);
    }




    public List<SystemInfo> performSysInfo(String id, String deviceCategory, String storageArea, String brand, String deviceName,
                                           String modelNumber, String interfaceTypeAndQuantity, String graphicsInterfaceTypeAndQuantity,
                                           String maxOutputSpec, String screenSize, String screenRatio, String releaseDate,
                                           String devicePurchaseDate, String deviceRepairHistory, String source, String graphicsSource){
        return dqeDao.performSysInfo(id, deviceCategory, storageArea, brand, deviceName, modelNumber, interfaceTypeAndQuantity,
                graphicsInterfaceTypeAndQuantity, maxOutputSpec, screenSize, screenRatio, releaseDate, devicePurchaseDate,
                deviceRepairHistory, source, graphicsSource);
    }


}
