package com.lu.ddwyydemo04.Service;

import com.lu.ddwyydemo04.dao.TestManDao;
import com.lu.ddwyydemo04.pojo.Samples;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DurationTableService {
    @Autowired
    private TestManDao testManDao;

    public List<Samples> searchSampleTestMan(String keyword,String problemTimeStart,String problemTimeEnd){
        return testManDao.searchSampleTestMan(keyword, problemTimeStart, problemTimeEnd);
    }

}
