package com.jaeyeon.blackfriday.common.config

import com.jaeyeon.blackfriday.common.security.session.SecurityConstants
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun openAPI(): OpenAPI {
        GroupedOpenApi.builder()
            .group("api")
            .pathsToMatch("/api/**")
            .build()

        return OpenAPI()
            .info(
                Info()
                    .title("BlackFriday API")
                    .description("BlackFriday API Documentation")
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Jaeyeon Cho")
                            .email("cjyeon1022@gmail.com"),
                    ),
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        SecurityConstants.AUTH_HEADER,
                        SecurityScheme()
                            .type(SecurityScheme.Type.APIKEY)
                            .`in`(SecurityScheme.In.HEADER)
                            .name(SecurityConstants.AUTH_HEADER),
                    ),
            )
            .addSecurityItem(SecurityRequirement().addList(SecurityConstants.AUTH_HEADER))
    }
}
