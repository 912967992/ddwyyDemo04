package com.lu.ddwyydemo04.controller.ProjectLeader;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProjectLeaderProblemMoudleController {

    @GetMapping("/ProjectLeaderMoudle") // 处理页面跳转请求
    public String loginDQEIndex() {
        // 返回跳转页面的视图名称
        return "ProjectLeader/projectLeaderProblemMoudle";
    }



}
