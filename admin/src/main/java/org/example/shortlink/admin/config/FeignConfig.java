package org.example.shortlink.admin.config;

import feign.RequestInterceptor;
import org.example.shortlink.admin.common.biz.user.UserContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    /**
     * Feign 请求拦截器（自动传递用户信息）
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("username", UserContext.getUsername());
            template.header("userId", UserContext.getUserId());
            template.header("realName", UserContext.getRealName());
        };
    }
}