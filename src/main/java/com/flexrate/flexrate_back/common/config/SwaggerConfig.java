package com.flexrate.flexrate_back.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
@OpenAPIDefinition(security = { @SecurityRequirement(name = "Bearer Authentication") })
public class SwaggerConfig {
    @Bean
    public GlobalOpenApiCustomizer globalErrorResponses() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {
                    ApiResponses responses = operation.getResponses();
                    ApiResponse error400 = new ApiResponse()
                            .description("잘못된 요청 - 필드 누락 또는 형식 오류");
                    ApiResponse error401 = new ApiResponse()
                            .description("인증 실패 - 토큰 누락/만료");
                    ApiResponse error500 = new ApiResponse()
                            .description("서버 내부 오류");
                    responses.addApiResponse("400", error400);
                    responses.addApiResponse("401", error401);
                    responses.addApiResponse("500", error500);
                }));
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Flexrate API")
                        .description("플렉스레이트 백엔드 API 명세서")
                        .version("v1.0.0"));
    }
}
