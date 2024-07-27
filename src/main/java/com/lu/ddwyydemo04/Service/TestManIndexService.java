package com.lu.ddwyydemo04.Service;

import com.lu.ddwyydemo04.dao.QuestDao;
import com.lu.ddwyydemo04.dao.TestManDao;
import com.lu.ddwyydemo04.pojo.Samples;
import com.lu.ddwyydemo04.pojo.TotalData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Service("TestManIndexService")
public class TestManIndexService {
    @Autowired
    private QuestDao questDao;

    @Autowired
    private TestManDao testManDao;

    public Map<String, Integer> getindexPanel(String name){
        return questDao.getindexPanel(name);
    }

    public List<Samples> getTestManPanel(String tester){
        return testManDao.getTestManPanel(tester);
    }

    public int queryCountTotal(String name){ return testManDao.queryCountTotal(name);}

    public void createTotal(TotalData totalData){
        testManDao.createTotal(totalData);
    }

    public void updateTotal(String name){
        testManDao.updateTotal(name);
    }

    public List<Samples> searchSamples(String tester,String keyword){
        return testManDao.searchSamples(tester,keyword);
    }

    public List<Samples> searchSamplesByAsc(String tester,String keyword){
        return testManDao.searchSamplesByAsc(tester,keyword);
    }

    public List<Samples> searchSamplesByDesc(String tester,String keyword){
        return testManDao.searchSamplesByDesc(tester,keyword);
    }


    public void updateSample(Samples sample) {
        testManDao.updateSample(sample);
    }

    public void updateSampleTeamWork(Samples sample){
        testManDao.updateSampleTeamWork(sample);
    }

    public void finishTest(String schedule,String model,String coding,String category,String version,String big_species,String small_species,String high_frequency,int sample_frequency,
                           String questStats){
        testManDao.finishTest(schedule,model,coding,category,version,big_species,small_species,high_frequency,sample_frequency,questStats);
    }
    public void finishTestWithoutTime(String schedule,String model,String coding,String category,String version,String finish_time,
                                      String big_species,String small_species,String high_frequency,int sample_frequency,String questStats){
        testManDao.finishTestWithoutTime(schedule,model,coding,category,version,finish_time,big_species,small_species,high_frequency,sample_frequency,questStats);
    }



//    public String queryFinishtime(String model, String coding, String category, String version,String big_species,String small_species,String high_frequency){
//        return testManDao.queryFinishtime(model,coding,category,version,big_species,small_species,high_frequency);
//    }
    public String queryTester_teamwork(Samples sample){
        return testManDao.queryTester_teamwork(sample);
    }

    public String querySample_name(Samples sample){
        return testManDao.querySample_name(sample);
    }

    public String queryFilepath(Samples sample){
        return testManDao.queryFilepath(sample);
    }

    public String queryTester(Samples sample){
        return testManDao.queryTester(sample);
    }

    public int deleteFilepath(String filepath){
        return testManDao.deleteFilepath(filepath);
    }


}
