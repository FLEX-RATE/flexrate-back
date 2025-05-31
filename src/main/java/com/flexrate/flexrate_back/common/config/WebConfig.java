package com.flexrate.flexrate_back.common.config;

import com.flexrate.flexrate_back.auth.resolver.CurrentMemberIdArgumentResolver;
import com.flexrate.flexrate_back.common.interceptor.TraceIdInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final TraceIdInterceptor traceIdInterceptor;
    private final CurrentMemberIdArgumentResolver currentMemberIdArgumentResolver;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(traceIdInterceptor)
                .addPathPatterns("/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentMemberIdArgumentResolver);
    }
}
