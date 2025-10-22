package com.example.test.server.config;


import com.example.test.server.interceptor.JwtTokenAdminInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;


@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .excludePathPatterns(
                        "/auth/sendcode",
                        "/auth/register",
                        "/auth/login",
                        "/movies/**",
                        "/movieCover/**",
                        "/auth/getpost",
                        "/auth/getComment",
                        "/auth/movie/rooms",
                        "/auth/movie/movies",



                        // Knife4j文档接口
                        "/v2/api-docs",
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/swagger-ui.html"
                );
    }

    /**
     * 通过knife4j生成接口文档
     * @return
     */
    @Bean
    public Docket docket() {
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("WaitYou项目接口文档")
                .version("1.0")
                .description("WQ项目接口文档")
                .build();

        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example.test.server.controller"))
                .paths(PathSelectors.any())
                .build()
        // 新增以下两行
                .enable(true)
                .enableUrlTemplating(false); // 关闭 URL 模板模式
        return docket;
    }

    /**
     * 设置静态资源映射
     * @param registry
     */
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/avatar/**").addResourceLocations("file:C:/avatar/");
        registry.addResourceHandler("/movies/**").addResourceLocations("file:/www/wwwroot/movies/");
        registry.addResourceHandler("/movieCover/**").addResourceLocations("file:/www/wwwroot/movieCover/");
        registry.addResourceHandler("/postImages/**").addResourceLocations("file:C:/postImages/");

    }
}
