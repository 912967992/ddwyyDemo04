package com.lu.ddwyydemo04.Service.DQE;

import com.lu.ddwyydemo04.dao.DQE.DQEDao;
import com.lu.ddwyydemo04.dao.DQE.ScheduleBoardDao;
import com.lu.ddwyydemo04.pojo.Group;
import com.lu.ddwyydemo04.pojo.TestEngineerInfo;
import com.lu.ddwyydemo04.pojo.TesterInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ScheduleBoardService {

    @Autowired
    private ScheduleBoardDao scheduleBoardDao;

    public List<TestEngineerInfo> getTestEngineers() {
        return scheduleBoardDao.getTestEngineers();
    }

    public void updateTesterInfo(TesterInfo testerInfo){
        scheduleBoardDao.updateTesterInfo(testerInfo);
    }

    public int queryExistTester(TesterInfo testerInfo){
        return scheduleBoardDao.queryExistTester(testerInfo);
    }

    public Map<String, String> queryJobnumber(TesterInfo testerInfo){
        return scheduleBoardDao.queryJobnumber(testerInfo);
    }

    public boolean insertTesterInfo(TesterInfo testerInfo){
        return scheduleBoardDao.insertTesterInfo(testerInfo);
    }

    public boolean deleteTester(String test_engineer_name){
        return scheduleBoardDao.deleteTester(test_engineer_name);
    }


    public List<Group> getGroupData(){
        return scheduleBoardDao.getGroupData();
    }

    public void updateGroup(Group group){
        scheduleBoardDao.updateGroup(group);
    }

    public List<Group> getAllGroupsOrderByDisplayOrder(){
        return scheduleBoardDao.getAllGroupsOrderByDisplayOrder();
    }

    public void addGroup(Group group){
        scheduleBoardDao.addGroup(group);
    }
}
