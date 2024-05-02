package org.example.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        servers = {
                @Server(url = "/", description = "Default Server url")
        },
        info = @Info(
                title = "Example API Docs",
                description = "Description",
                version = "v1"
        )
)
@Configuration
public class SwaggerConfig {
    private static final String BEARER_TOKEN_PREFIX = "Bearer";
    private static final String REFRESH_TOKEN = "refreshToken";

    @Bean
    public OpenAPI openAPI() {
        String securityJwtName = "JWT";
        String refreshKey = "Refresh Token";
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(securityJwtName)
                .addList(refreshKey);
        Components components = new Components()
                .addSecuritySchemes(securityJwtName, new SecurityScheme()
                        .name(securityJwtName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme(BEARER_TOKEN_PREFIX)
                        .bearerFormat(securityJwtName))
                .addSecuritySchemes(refreshKey, new SecurityScheme()
                        .name(REFRESH_TOKEN)
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER));

        return new OpenAPI()
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}
