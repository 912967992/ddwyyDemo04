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




    public List<SystemInfo> performSysInfo(String id, String computerName,String deviceType,String version,
                                           String osVersion, String fullOS,
                                           String architecture, String cpu,String memory,
                                           String displays,
                                           String maxResolution, String maxRefreshRate,String interfaceInfo){
        return dqeDao.performSysInfo(id,computerName,deviceType , version,osVersion, fullOS,
                architecture,cpu,memory,displays,
                maxResolution,maxRefreshRate,interfaceInfo);
    }


}
