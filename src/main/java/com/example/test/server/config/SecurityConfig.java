package com.example.test.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests() // 开启请求授权配置
                .antMatchers(
                        "/avatar/**",
                        "/auth/**",
                        "/api/**",
                        "/movies/**",
                        "/movieCover/**",
                        "/postImages/**",
                        "/getpost/**",
                        "/getComment/**",
                        "/movie/rooms",
                        "/movie/movies",
                        "/error",
                        "/ws/**",
                        "/doc.html",          // 直接放行文档页面
                        "/doc.html/**",       // 放行文档页面的子路径
                        "/webjars/**",       // 放行 webjars 资源
                        "/swagger-resources/**", // Swagger 资源
                        "/v2/api-docs/**"     // API 描述文件
                ).permitAll() // 公开指定路径
                .anyRequest().authenticated() // 其他请求需认证
                .and() // 返回HttpSecurity对象，继续其他配置
                .csrf()
                .disable(); // 禁用CSRF

        return http.build();
    }
}
