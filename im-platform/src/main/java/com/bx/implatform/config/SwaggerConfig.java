package com.bx.implatform.config;


import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi userApi() {
        String[] paths = {"/**"};
        String[] packagedToMatch = {"com.bx"};
        return GroupedOpenApi.builder().group("IM-Platform")
            .pathsToMatch(paths)
            .packagesToScan(packagedToMatch).build();
    }
}
