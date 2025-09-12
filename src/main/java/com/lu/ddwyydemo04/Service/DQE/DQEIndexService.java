package com.lu.ddwyydemo04.Service.DQE;

import com.lu.ddwyydemo04.dao.DQE.DQEDao;
import com.lu.ddwyydemo04.pojo.Samples;
import com.lu.ddwyydemo04.pojo.TaskNode;
import org.apache.ibatis.javassist.tools.rmi.Sample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public List<TaskNode> getRecentNotifications(){
        return dqeDao.getRecentNotifications();
    }

    public List<Samples> selectFromSampleId(int sample_id){
        return dqeDao.selectFromSampleId(sample_id);
    }

    public String getWarningDaysByManager(String manager){
        return dqeDao.getWarningDaysByManager(manager);
    }

    public boolean updateOverdueReason(String id, String overdueReason) {
        return dqeDao.updateOverdueReason(id,overdueReason);
    }


    public void updateAgents(String agent_name,String username){
        dqeDao.updateAgents(agent_name,username);
    }

    public void insertAgents(String agent_name,String username){
        dqeDao.insertAgents(agent_name,username);
    }

    public int queryAgents(String username){
        return dqeDao.queryAgents(username);
    }

    public String getAgents(String username){
        try {

            if (username == null || username.trim().isEmpty()) {
                System.out.println("Service层错误: 用户名为空");
                return null;
            }

            String result = dqeDao.getAgents(username);

            return result;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw e; // 重新抛出异常，让Controller层处理
        }
    }

    // 获取待审核的项目详情
    public List<Map<String, Object>> getRecentNotificationsDetails(String username) {
        return dqeDao.getRecentNotificationsDetails(username);
    }

    // 获取超期项目详情
    public List<Map<String, Object>> getOverdueDetails(String username) {
        return dqeDao.getOverdueDetails(username);
    }

    // 获取电气项目数据
    public List<Map<String, Object>> getElectricProjectData() {
        return dqeDao.getElectricProjectData();
    }

    // 获取DQE个人电气项目数据
    public List<Map<String, Object>> getDqeElectricProjectData(String username) {
        return dqeDao.getDqeElectricProjectData(username);
    }

    // 获取研发个人电气项目数据
    public List<Map<String, Object>> getRdElectricProjectData(String username) {
        return dqeDao.getRdElectricProjectData(username);
    }

    // 获取排期时间分布数据
    public Map<String, Object> getScheduleDistributionData() {
        List<Map<String, Object>> rawData = dqeDao.getScheduleDistributionData();
        Map<String, Object> result = new HashMap<>();
        
        // 将查询结果转换为前端需要的格式
        for (Map<String, Object> item : rawData) {
            String status = (String) item.get("schedule_status");
            Object count = item.get("count");
            result.put(status, count);
        }
        
        return result;
    }

    // 获取排期详情数据
    public List<Map<String, Object>> getScheduleDetails(String scheduleStatus) {
        return dqeDao.getScheduleDetails(scheduleStatus);
    }

}
