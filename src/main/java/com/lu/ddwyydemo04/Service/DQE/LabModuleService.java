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




    public List<SystemInfo> performSysInfo(String id, String computerName,String version,
                                           String installationDate,String osVersion, String fullOS,
                                           String architecture, String systemModel,String cpu,String memory,
                                           String displays,String networkAdapters,
                                           String maxResolution, String maxRefreshRate,String interfaceInfo){
        return dqeDao.performSysInfo(id,computerName,version,installationDate,osVersion, fullOS,
                architecture,systemModel,cpu,memory,displays,networkAdapters,
                maxResolution,maxRefreshRate,interfaceInfo);
    }


}
