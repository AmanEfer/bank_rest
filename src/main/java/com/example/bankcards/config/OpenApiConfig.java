package com.example.bankcards.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SCHEMA_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(SCHEMA_NAME))
                .components(new Components()
                        .addSecuritySchemes(SCHEMA_NAME,
                                new SecurityScheme()
                                        .name(SCHEMA_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Введите JWT токен (получите через /api/v1/auth/login)")
                        )
                )
                .info(new Info()
                        .title("Bank Card Management System API")
                        .version("1.0.0")
                        .description("""
                                API представляет собой систему управления картами пользователя.
                                Отдельный функционал для пользователя и для администратора.
                                
                                Администратор:
                                - Создает, блокирует, активирует, удаляет карты
                                - Управляет пользователями(CRUD)
                                - Видит все карты
                                
                                Пользователь:
                                - Просматривает свои карты (поиск + пагинация)
                                - Запрашивает блокировку карты
                                - Делает переводы между своими картами
                                - Смотрит баланс
                                - Делает переводы между своими картами
                                
                                Отдельная API для регистрации и логина по номеру телефона и паролю.""")
                );
    }
}
