package com.projeto.backend.web.openapi;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Health Check", description = "Endpoints para verificação de saúde da aplicação")
public interface HealthControllerOpenApi {
	@Operation(
            summary = "Health check básico",
            description = "Verifica se a API está online e respondendo"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API está online")
    })
    public ResponseEntity<Map<String, Object>> health();
	
	@Operation(
            summary = "Health check do banco de dados",
            description = "Verifica a conexão com o PostgreSQL"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conexão com banco OK"),
            @ApiResponse(responseCode = "503", description = "Falha na conexão com o banco")
    })
    public ResponseEntity<Map<String, Object>> healthDatabase();
}
