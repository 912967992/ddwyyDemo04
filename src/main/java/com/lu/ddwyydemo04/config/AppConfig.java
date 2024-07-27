package com.lu.ddwyydemo04.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@Configuration
public class AppConfig {

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(50 * 1024 * 1024); // 50MB
        multipartResolver.setMaxUploadSizePerFile(50 * 1024 * 1024); // 50MB per file
        multipartResolver.setMaxInMemorySize(1024 * 1024); // 1MB
        return multipartResolver;
    }
}
