package com.lu.ddwyydemo04.dao;

import com.lu.ddwyydemo04.pojo.QuestData;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface QuestDao {
    public List<QuestData> getQuestById(@Param("id") String id);

    public Map<String,Integer> getindexPanel(@Param("name") String name);

    public List<QuestData> getQuestPanel();

    public List<QuestData> searchQuest(@Param("keyword") String keyword,@Param("category") String category,@Param("stage") String stage);
}
