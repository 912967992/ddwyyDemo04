package com.lu.ddwyydemo04.Service.DQE;

import com.lu.ddwyydemo04.dao.DQE.DQEDao;
import com.lu.ddwyydemo04.pojo.SystemInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LabModuleService {
    @Autowired
    private DQEDao dqeDao;

    public int saveSys(SystemInfo systemInfo){
        return dqeDao.saveSys(systemInfo);
    }



}
