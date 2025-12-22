package com.lu.ddwyydemo04;

import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@MapperScan("com.lu.ddwyydemo04.dao")
@SpringBootApplication
@EnableTransactionManagement // 启用事务管理
@EnableScheduling
public class DdwyyDemo04Application {

    public static void main(String[] args) {
        // 设置HTTP连接超时时间（单位：毫秒）
        // 连接超时：30秒（避免用户等待过久）
        System.setProperty("sun.net.client.defaultConnectTimeout", "30000");
        // 读取超时：30秒
        System.setProperty("sun.net.client.defaultReadTimeout", "30000");
        
        // 配置 Apache POI ZipSecureFile 以允许处理包含嵌入 OLE 对象的 Excel 文件
        // 默认的 MIN_INFLATE_RATIO 是 0.01，当压缩比等于或低于此值时会被拒绝
        // 设置为 0.001 以允许更低的压缩比（例如包含大量图片的 Excel 文件）
        // 注意：这会降低对 Zip bomb 攻击的防护，但允许处理正常的业务文件
        ZipSecureFile.setMinInflateRatio(0.001);
        
        SpringApplication.run(DdwyyDemo04Application.class, args);
    }

}
//