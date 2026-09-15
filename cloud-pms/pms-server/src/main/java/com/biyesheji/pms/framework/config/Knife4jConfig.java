package com.biyesheji.pms.framework.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 3 接口文档配置
 * <p>
 * 文档地址：http://localhost:8080/doc.html
 */
@Configuration
public class Knife4jConfig {

    private static final String SECURITY_SCHEME_NAME = "Authorization";

    @Bean
    public OpenAPI pmsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("云协同 PMS 接口文档")
                        .description("中小企业项目全生命周期管理系统 - 后端接口。"
                                + "登录接口获取 token 后，点击右上角「Authorize」填入 token 即可调试需要鉴权的接口。")
                        .version("1.0.0")
                        .contact(new Contact().name("云协同研发中心").email("dev@yunxiao.com"))
                        .license(new License().name("MIT")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
