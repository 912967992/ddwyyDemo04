package com.lu.ddwyydemo04.Service.DQE;

import com.lu.ddwyydemo04.dao.DQE.DQEDao;
import com.lu.ddwyydemo04.pojo.Samples;
import com.lu.ddwyydemo04.pojo.TestIssues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class DQEproblemMoudleService {

    @Autowired
    private DQEDao dqeDao;

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
                                          String sample_schedule, String result_judge, String problemTimeStart, String  problemTimeEnd){
        return dqeDao.searchSamplesDQE(sample_id, full_model, questStats, sample_category, version,
                big_species, small_species, supplier, test_Overseas,
                sample_DQE, sample_Developer, tester, priority,
                sample_schedule, result_judge, problemTimeStart, problemTimeEnd);
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


    @Transactional
    public void updateTestIssues(TestIssues testIssues){
        dqeDao.updateTestIssues(testIssues);
    }

    @Transactional
    public void insertTestIssues(TestIssues testIssues){
        dqeDao.insertTestIssues(testIssues);
    }

    @Transactional
    public boolean deleteProblemById(Long id) {
        int rowsAffected = dqeDao.deleteProblemById(id);
        return rowsAffected > 0; // 如果删除成功，返回 true
    }


    @Transactional
    public int uploadImage(Long id,String problem_image ){
        return dqeDao.uploadImage(id,problem_image);
    }

    public Map<String, String> searchScheduleAndResultJudge(String sample_id){
        return dqeDao.searchScheduleAndResultJudge(sample_id);
    }


    public int updateSampleWithSchAndResult(String sampleId, String sampleSchedule, String job, String selectedOption) {
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

}
