package com.tutoring.Tutorverse.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI tutorverseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tutorverse API")
                        .description("Online Tutoring Platform API Documentation")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Tutorverse Team")
                                .email("support@tutorverse.com")
                        )
                );
    }
}
