package com.biyesheji.pms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;

/**
 * 云协同项目管理系统 - 启动类
 */
@Slf4j
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class PmsApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(PmsApplication.class, args);
        Environment env = context.getEnvironment();
        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        String host = "localhost";
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ignored) {
        }
        log.info("\n----------------------------------------------------------\n" +
                        "  云协同 PMS 启动成功！\n" +
                        "  本地地址：http://localhost:{}{}\n" +
                        "  接口文档：http://localhost:{}{}/doc.html\n" +
                        "  外部地址：http://{}:{}{}\n" +
                        "----------------------------------------------------------",
                port, contextPath, port, contextPath, host, port, contextPath);
    }
}
