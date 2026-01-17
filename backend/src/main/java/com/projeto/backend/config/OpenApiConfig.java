package com.projeto.backend.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Configuração do OpenAPI/Swagger para documentação da API.
 * 
 * Acesso à documentação:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 * - OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                // Informações da API
                .info(new Info()
                        .title("API de Gerenciamento de Artistas e Álbuns")
                        .description("""
                                API REST para gerenciamento de artistas musicais e seus álbuns.
                                
                                ## Funcionalidades
                                
                                - **Autenticação**: JWT com expiração de 5 minutos e refresh token
                                - **Artistas**: CRUD completo com busca e ordenação
                                - **Álbuns**: CRUD completo com paginação
                                - **Capas**: Upload de imagens para MinIO com presigned URLs
                                - **Regionais**: Sincronização com API externa
                                
                                ## Autenticação
                                
                                Para acessar os endpoints protegidos, você deve:
                                1. Fazer login em `/api/v1/auth/login`
                                2. Copiar o `access_token` retornado
                                3. Clicar no botão "Authorize" acima
                                4. Inserir o token no formato: `Bearer {seu_token}`
                                
                                ## Usuário de Teste
                                
                                - **Username**: admin
                                - **Password**: admin123
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe de Desenvolvimento")
                                .email("henricovilela@gmail.com")
                                .url("https://github.com/henricoVilela/projeto-artistas-albuns")))
                
                // Servidores
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor de Desenvolvimento"),
                        new Server()
                                .url("http://artistas-api:8080")
                                .description("Servidor Docker")
                ))
                
                // Configuração de Segurança JWT
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Insira o token JWT no formato: Bearer {token}")
                        ));
    }
}

