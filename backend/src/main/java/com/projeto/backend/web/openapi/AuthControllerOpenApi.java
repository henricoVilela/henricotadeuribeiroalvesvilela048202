package com.projeto.backend.web.openapi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import com.projeto.backend.web.dto.auth.AuthRequest;
import com.projeto.backend.web.dto.auth.AuthResponse;
import com.projeto.backend.web.dto.auth.RefreshRequest;
import com.projeto.backend.web.dto.auth.RegisterRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Autenticação", description = "Endpoints para autenticação e gerenciamento de sessão")
public interface AuthControllerOpenApi {
	
	@Operation(
            summary = "Login de usuário",
            description = "Autentica o usuário e retorna os tokens JWT (access e refresh)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciais inválidas",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos",
                    content = @Content
            )
    })
	public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request);
	
	@Operation(
            summary = "Registro de novo usuário",
            description = "Cria um novo usuário no sistema e retorna os tokens JWT"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuário criado com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou usuário/email já existente",
                    content = @Content
            )
    })
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request);
	
	@Operation(
            summary = "Renovar token de acesso",
            description = "Gera um novo access token usando um refresh token válido"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token renovado com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Refresh token inválido ou expirado",
                    content = @Content
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request);
}
