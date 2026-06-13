package com.ecomarket.pedidos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuracion central de OpenAPI / Swagger para el microservicio.
 * Expone la documentacion interactiva en:
 *  - Swagger UI:    http://localhost:8086/swagger-ui.html
 *  - OpenAPI JSON:  http://localhost:8086/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS Pedidos y Ventas API")
                        .version("1.0.0")
                        .description("API del microservicio de Pedidos y Ventas. Gestiona carritos, pedidos, ventas, pagos, cupones, devoluciones y facturación.")
                        .contact(new Contact()
                                .name("Equipo EcoMarket SPA")
                                .email("ecomarket@duocuc.cl")
                                .url("https://github.com/ecomarket-spa"))
                        .license(new License()
                                .name("Proyecto Academico DUOC UC")
                                .url("https://www.duoc.cl")))
                .servers(List.of(
                        new Server().url("http://localhost:8086").description("Servidor local"),
                        new Server().url("http://localhost:8081").description("API Gateway")));
    }
}
