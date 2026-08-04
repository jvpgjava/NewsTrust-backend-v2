package com.newstrust.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadados globais da documentacao OpenAPI/Swagger. A documentacao de cada
 * endpoint (operacoes, respostas, exemplos) fica nas interfaces do pacote
 * {@code infrastructure.adapter.in.web.openapi}, nunca nos controllers.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI newsTrustOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("NewsTrust API")
                        .description("Analise de credibilidade de noticias e fontes digitais: "
                                + "score interpretavel (0-100), faixas de risco, razoes auditaveis "
                                + "e grafo de credibilidade (rede de fontes e rede de noticias).")
                        .version("v1")
                        .license(new License().name("Ver LICENSE no repositorio")));
    }
}
