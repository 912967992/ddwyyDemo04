package com.lu.ddwyydemo04.controller;

import com.lu.ddwyydemo04.Service.QuestService;
import com.lu.ddwyydemo04.pojo.QuestData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
public class QuestController {

    @Autowired
    private QuestService questService;

    @GetMapping("/loginQuestIndex") // 处理页面跳转请求
    public String loginQuestIndex() {
        // 返回跳转页面的视图名称
        return "questPanel";
    }

    @GetMapping("/home") // 处理页面跳转请求
    public String loginHome() {
        // 返回跳转页面的视图名称
        return "testManIndex";
    }


    @GetMapping("/cloud") // 处理页面跳转请求
    public String loginCloud() {
        // 返回跳转页面的视图名称
        return "questPanel";
    }

    @GetMapping("/getQuestById")
    @ResponseBody
    public List<QuestData> getQuestById(@RequestParam("id") String id){

        return questService.getQuestById(id);
    }

    @GetMapping("/getQuestPanel")
    @ResponseBody
    public List<QuestData> getQuestPanel(){
        return questService.getQuestPanel();
    }

    @PostMapping("/searchQuest")
    @ResponseBody
    public List<QuestData> searchQuest(@RequestBody Map<String, Object> data){

        String keyword = (String) data.get("keyword");
        String categoryFilter = (String) data.get("categoryFilter");
        String stageFilter = (String) data.get("stageFilter");
        if(Objects.equals(categoryFilter, "all")){
            categoryFilter = "";
        }
        if(Objects.equals(stageFilter, "all")){
            stageFilter = "";
        }

        return questService.searchQuest(keyword,categoryFilter,stageFilter);
    }

}