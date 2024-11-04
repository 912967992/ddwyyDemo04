package com.lu.ddwyydemo04.Service.DQE;

import com.lu.ddwyydemo04.dao.DQE.DQEDao;
import com.lu.ddwyydemo04.pojo.TaskNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DQEIndexService {


    @Autowired
    private DQEDao dqeDao;

    public int updateSetting(String roleManager,String warn_days){
        return dqeDao.updateSetting(roleManager,warn_days);
    }

    public List<TaskNode> findFirstOverdueSamplesOnce(){
        return dqeDao.findFirstOverdueSamplesOnce();
    }

}
