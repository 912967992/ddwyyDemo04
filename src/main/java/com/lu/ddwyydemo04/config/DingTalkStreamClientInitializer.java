//package com.lu.ddwyydemo04.config;
//
//import com.dingtalk.open.app.api.GenericEventListener;
//import com.dingtalk.open.app.api.OpenDingTalkStreamClientBuilder;
//import com.dingtalk.open.app.api.message.GenericOpenDingTalkEvent;
//import com.dingtalk.open.app.api.security.AuthClientCredential;
//import com.dingtalk.open.app.stream.protocol.event.EventAckStatus;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import shade.com.alibaba.fastjson2.JSONObject;
//
//import javax.annotation.PostConstruct;
//
//
//
//@Component
//public class DingTalkStreamClientInitializer {
//
//
//    @Value("${dingtalk.client-id}")
//    private String clientId;
//
//    @Value("${dingtalk.client-secret}")
//    private String clientSecret;
//
//
//
//    @PostConstruct
//    public void initClient() {
//        try {
//            OpenDingTalkStreamClientBuilder
//                    .custom()
//                    .credential(new AuthClientCredential(clientId, clientSecret))
//                    //注册事件监听
//                    .registerAllEventListener(new GenericEventListener() {
//                        public EventAckStatus onEvent(GenericOpenDingTalkEvent event) {
//                            try {
//                                //事件唯一Id
//                                String eventId = event.getEventId();
//                                //事件类型
//                                String eventType = event.getEventType();
//                                //事件产生时间
//                                Long bornTime = event.getEventBornTime();
//                                //获取事件体
//                                JSONObject bizData = event.getData();
//                                //处理事件
//                                process(bizData);
//                                //消费成功
//                                return EventAckStatus.SUCCESS;
//                            } catch (Exception e) {
//                                //消费失败
//                                return EventAckStatus.LATER;
//                            }
//                        }
//                    })
//                    .build().start();
//            System.out.println("DingTalk Stream Client 启动成功！");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.err.println("DingTalk Stream Client 启动失败！");
//        }
//    }
//
//    public void process(JSONObject data) {
//        // 这里写你自己的事件处理逻辑
//        System.out.println("处理事件数据：" + data.toJSONString());
//    }
//}