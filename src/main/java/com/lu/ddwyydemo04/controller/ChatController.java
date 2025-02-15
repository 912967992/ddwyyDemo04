package com.lu.ddwyydemo04.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class ChatController {
    @Autowired
    private RestTemplate restTemplate;


    // 将用户请求重定向到 Flask 应用的页面
    @GetMapping("/chatDeepseek")
    public String chatPage() {
        // 返回一个 Thymeleaf 页面，使用 iframe 嵌套 Flask 页面
        return "chatDeepseek";  // chat.html 页面
    }

    @RequestMapping(method = RequestMethod.POST, path = "/chat")
    public String handleChatRequest(@RequestBody String chatMessage) {
        // 调用app.py进行处理
        String response = callAppPy(chatMessage);
        return response;
    }

    private String callAppPy(String chatMessage) {
        // 配置app.py的路径和参数
        // 假设app.py在本地运行，或者可以通过URL访问
        return executeRequest(chatMessage);
    }

    private String executeRequest(String input) {
        try {
            // 使用RestTemplate发送HTTP POST请求到app.py
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://localhost:5000",  // app.py的端口和路径
                    input,
                    String.class
            );
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            System.out.println("HTTP请求失败：" + e.getMessage());
            return "错误：" + e.getMessage();
        }
    }




}
