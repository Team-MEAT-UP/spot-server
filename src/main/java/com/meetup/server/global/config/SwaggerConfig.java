package com.meetup.server.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.Collections;

@Slf4j
@OpenAPIDefinition(
        info = @Info(
                title = "MOISAM API Specification",
                description = "OpenAPI 3.0 specification for the MOISAM",
                version = "v1.1.2"
        ),
        servers = @Server(url = "${springdoc.server-url}", description = "Server URL")
)
@Configuration
public class SwaggerConfig {

    private static final String OAS_FILE_PATH = "static/swagger-ui/openapi3.json";
    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI openAPI() {
        OpenAPI openAPI = new OpenAPI();

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        ObjectMapper swaggerMapper = Json.mapper();
        ClassPathResource resource = new ClassPathResource(OAS_FILE_PATH);

        try (InputStream inputStream = resource.getInputStream()) {
            OpenAPI restDocsOpenAPI = swaggerMapper.readValue(inputStream, OpenAPI.class);

            Components components = restDocsOpenAPI.getComponents() != null
                    ? restDocsOpenAPI.getComponents()
                    : new Components();

            components.addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme);
            openAPI.components(components);

            if (restDocsOpenAPI.getPaths() != null) {
                openAPI.setPaths(restDocsOpenAPI.getPaths());
            }

            SecurityRequirement securityRequirement = new SecurityRequirement().addList(SECURITY_SCHEME_NAME);
            openAPI.security(Collections.singletonList(securityRequirement));

        } catch (Exception e) {
            log.warn("Not Exist an OpenAPI Specification", e);

            Components components = new Components();
            components.addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme);
            openAPI.components(components);

            SecurityRequirement securityRequirement = new SecurityRequirement().addList(SECURITY_SCHEME_NAME);
            openAPI.security(Collections.singletonList(securityRequirement));
        }

        return openAPI;
    }
}
