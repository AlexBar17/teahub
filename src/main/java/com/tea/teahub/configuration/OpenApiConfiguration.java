package com.tea.teahub.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Tea Hub",
        version = "0.1.0",
        description = "Контракт для работы с бэкендом каталога чаев"
    )
)
public class OpenApiConfiguration {
}
