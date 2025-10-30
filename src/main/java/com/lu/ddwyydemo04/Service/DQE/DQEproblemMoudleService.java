package com.lu.ddwyydemo04.Service.DQE;

import com.lu.ddwyydemo04.dao.DQE.DQEDao;
import com.lu.ddwyydemo04.dao.TestManDao;
import com.lu.ddwyydemo04.pojo.Samples;
import com.lu.ddwyydemo04.pojo.TestIssues;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class DQEproblemMoudleService {

    @Autowired
    private DQEDao dqeDao;


    @Autowired
    private TestManDao testManDao;

    public List<TestIssues> searchTestissues(){
        return dqeDao.searchTestissues();
    }
    public List<TestIssues> selectTestIssues(List<Integer> selectedIds){
        return dqeDao.selectTestIssues(selectedIds);
    }

    public List<TestIssues> selectTestIssuesFromSampleid(int sampleId){
        return dqeDao.selectTestIssuesFromSampleid(sampleId);
    }


    public List<Samples> searchSamplesDQE(String sample_id, String full_model, String questStats, String sample_category, String version,
                                          String big_species, String small_species, String supplier, String test_Overseas,
                                          String sample_DQE, String sample_Developer, String tester, String priority,
                                          String sample_schedule, String result_judge,String rd_result_judge, String problemTimeStart, String  problemTimeEnd,
                                          String sample_leader, String key){
        return dqeDao.searchSamplesDQE(sample_id, full_model, questStats, sample_category, version,
                big_species, small_species, supplier, test_Overseas,
                sample_DQE, sample_Developer, tester, priority,
                sample_schedule, result_judge,rd_result_judge, problemTimeStart, problemTimeEnd,sample_leader,key);
    }

    public Map<String, Object> addNewRow(int sampleId){
        return dqeDao.addNewRow(sampleId);
    }

    public Map<String, Object> addNewRowHistroyidZero(int sampleId){
        return dqeDao.addNewRowHistroyidZero(sampleId);
    }


    public int searchTestIssuesHistroyidFromId(int sampleId){
        return dqeDao.searchTestIssuesHistroyidFromId(sampleId);
    }



    public void updateTestIssues(TestIssues testIssues){
        dqeDao.updateTestIssues(testIssues);
    }


    public void insertTestIssues(TestIssues testIssues){
        dqeDao.insertTestIssues(testIssues);
    }


    public boolean deleteProblemById(Long id) {
        int rowsAffected = dqeDao.deleteProblemById(id);
        return rowsAffected > 0; // 如果删除成功，返回 true
    }

    public Long getSampleIdByProblemId(Long problemId) {
        return dqeDao.getSampleIdByProblemId(problemId);
    }


    public int uploadImage(Long id,String problem_image ){
        return dqeDao.uploadImage(id,problem_image);
    }

    public Map<String, String> searchScheduleAndResultJudge(String sample_id){
        return dqeDao.searchScheduleAndResultJudge(sample_id);
    }


    public int updateSampleWithSchAndResult(String sampleId, String sampleSchedule, String job, String selectedOption) {
        // 2. 如果是 DQE，同步更新 electric_info 表
        if ("DQE".equals(job)) {

            dqeDao.updateElectricInfoScheduleBySampleId(sampleSchedule,sampleId);
        }
        return dqeDao.updateSampleWithSchAndResult(sampleId, sampleSchedule, job, selectedOption);
    }

    public String getFilePathBySampleId(String sample_id){
        return dqeDao.getFilePathBySampleId(sample_id);
    }

    public List<Samples> querySamples(String sample_id){
        return dqeDao.querySamples(sample_id);
    }

    public String queryWarnDays(String setting_role ){
        return dqeDao.queryWarnDays(setting_role);
    }

    public List<Map<String, Object>> countDefectLevelsBySampleId( String sampleId){
        return dqeDao.countDefectLevelsBySampleId(sampleId);
    }


    public String formatDefectLevels(List<Map<String, Object>> countDefectLevel) {
        // 创建一个预定义的缺陷等级映射，初始化为 0
        Map<String, Integer> levelCounts = new HashMap<>();
        levelCounts.put("S", 0);
        levelCounts.put("A", 0);
        levelCounts.put("B", 0);
        levelCounts.put("C", 0);
        levelCounts.put("D", 0);
        levelCounts.put("待确定", 0);

        // 遍历 countDefectLevel 列表，将每个等级的计数更新到 levelCounts 中
        for (Map<String, Object> levelData : countDefectLevel) {
            String level = (String) levelData.get("defect_level_upper");
            Integer count = ((Number) levelData.get("count")).intValue();
            levelCounts.put(level, count);
        }

        // 格式化输出字符串
        return String.format("S:%d A:%d B:%d C:%d D:%d 待确定:%d",
                levelCounts.get("S"),
                levelCounts.get("A"),
                levelCounts.get("B"),
                levelCounts.get("C"),
                levelCounts.get("D"),
                levelCounts.get("待确定"));
    }

    public int updatepProblemCounts(String sampleId,String problemCounts){
        return dqeDao.updatepProblemCounts(sampleId,problemCounts);
    }

    public int updateResult(String sampleId , String job, String selectedOption) {
        return dqeDao.updateResult(sampleId , job, selectedOption);
    }

    public int queryResults(String sampleId ) {
        return dqeDao.queryResults(sampleId );
    }
    public int updateNodeAsFinishWithDQE(String sampleId) {
        return dqeDao.updateNodeAsFinishWithDQE(sampleId );
    }

    public int updateNodeAsFinishWithRD(String sampleId) {
        return dqeDao.updateNodeAsFinishWithRD(sampleId );
    }

    public String queryResultJudge(String sample_id){
        return dqeDao.queryResultJudge(sample_id);
    }

    public String queryPassbakConfirm(String sample_id){
        return dqeDao.queryPassbakConfirm(sample_id);
    }

}
