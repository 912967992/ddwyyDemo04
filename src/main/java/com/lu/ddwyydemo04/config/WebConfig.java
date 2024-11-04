package com.lu.ddwyydemo04.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.storage.imagepath}")
    private String imagepath;

    @Value("${file.storage.issuespath}")
    private String issuespath;

    @Value("${file.storage.savepath}")
    private String savepath;


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源的路径
//        registry.addResourceHandler("/imageDirectory/**")
//                .addResourceLocations("file:"+imagepath +"/");
        registry.addResourceHandler("/imageDirectory/**")
                .addResourceLocations("file:" + imagepath + "/");
        registry.addResourceHandler("/issuespath/**")
                .addResourceLocations("file:" + issuespath + "/");

    }
}
