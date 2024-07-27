package com.lu.ddwyydemo04.Service;

import com.lu.ddwyydemo04.dao.QuestDao;
import com.lu.ddwyydemo04.pojo.QuestData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("QuestService")
public class QuestService {

    @Autowired
    private QuestDao questDao;
    public List<QuestData> getQuestById(String id){
        return questDao.getQuestById(id);
    }

    public List<QuestData> getQuestPanel(){
        return questDao.getQuestPanel();
    }

    public List<QuestData> searchQuest(String keyword,String category,String stage){
        return questDao.searchQuest(keyword,category,stage);
    }

}