package com.lu.ddwyydemo04.dao.DQE;


import com.lu.ddwyydemo04.pojo.Samples;
import com.lu.ddwyydemo04.pojo.TestIssues;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DQEDao {

    public List<TestIssues> searchTestissues();
    public List<TestIssues> selectTestIssues(List<Integer> selectedIds);
    public List<TestIssues> selectTestIssuesFromSampleid(int sampleId);
    public List<Samples> searchSamplesDQE();

}
