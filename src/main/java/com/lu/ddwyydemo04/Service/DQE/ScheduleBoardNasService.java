package com.lu.ddwyydemo04.Service.DQE;

import com.lu.ddwyydemo04.Service.TestManIndexService;
import com.lu.ddwyydemo04.dao.DQE.ScheduleBoardDao;
import com.lu.ddwyydemo04.dao.DQE.ScheduleBoardNASDao;
import com.lu.ddwyydemo04.pojo.MaterialItem;
import com.lu.ddwyydemo04.pojo.PassbackData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleBoardNasService {

    @Autowired
    private ScheduleBoardNASDao scheduleBoardNASDao;

    public List<PassbackData> getReceivedDataNAS(){
        return scheduleBoardNASDao.getReceivedDataNAS();
    }









}
