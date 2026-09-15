package com.biyesheji.pms.framework.config;

import com.biyesheji.pms.framework.security.AccessDeniedHandlerImpl;
import com.biyesheji.pms.framework.security.AuthenticationEntryPointImpl;
import com.biyesheji.pms.framework.security.JwtAuthenticationTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

/**
 * Spring Security 配置
 * <p>
 * 采用「无状态 + JWT」模式：不创建 Session，每个请求靠令牌识别身份。
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /** 免认证路径白名单 */
    private static final String[] WHITE_LIST = {
            "/login", "/logout", "/captcha",
            "/doc.html", "/webjars/**", "/v3/api-docs/**",
            "/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**",
            "/favicon.ico", "/error",
            "/actuator/**", "/druid/**",
            // WebSocket 端点自行通过路径参数识别用户，交由端点内部处理
            "/ws/**"
    };

    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    private final AuthenticationEntryPointImpl authenticationEntryPoint;
    private final AccessDeniedHandlerImpl accessDeniedHandler;

    /**
     * 密码编码器：BCrypt 自带随机盐，同一个密码每次加密结果都不同
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证提供者
     * <p>
     * 显式把 UserDetailsService 与 PasswordEncoder 组装为 DaoAuthenticationProvider。
     * 不依赖自动装配，避免认证链路上缺少可用 Provider 导致始终返回"账号或密码错误"。
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService,
                                                               PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        // 用户不存在时同样提示"账号或密码错误"，防止账号被枚举
        provider.setHideUserNotFoundExceptions(true);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider daoAuthenticationProvider) {
        return new ProviderManager(daoAuthenticationProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 前后端分离 + 无状态令牌，不需要 CSRF 防护
                .csrf().disable()
                // 关闭 Spring Security 默认登出过滤：
                // 默认实现会把 POST /logout 重定向到 GET /login?logout，与自定义登出接口冲突
                .logout().disable()
                // 跨域交给 Spring Security 统一处理，避免与 MVC 层重复写响应头
                .cors().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers(WHITE_LIST).permitAll()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
                .and()
                // 允许 iframe 嵌入，便于 Knife4j / Druid 控制台在页面内展示
                .headers().frameOptions().disable()
                .and()
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setExposedHeaders(Collections.singletonList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
