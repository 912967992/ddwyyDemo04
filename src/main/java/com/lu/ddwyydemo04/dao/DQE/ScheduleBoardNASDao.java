package com.lu.ddwyydemo04.dao.DQE;

import com.lu.ddwyydemo04.pojo.PassbackData;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleBoardNASDao {


    List<PassbackData> getReceivedDataNAS();











}
