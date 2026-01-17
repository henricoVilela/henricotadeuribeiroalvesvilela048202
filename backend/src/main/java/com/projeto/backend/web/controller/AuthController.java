package com.projeto.backend.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.backend.security.AuthService;
import com.projeto.backend.web.dto.auth.AuthRequest;
import com.projeto.backend.web.dto.auth.AuthResponse;
import com.projeto.backend.web.dto.auth.RefreshRequest;
import com.projeto.backend.web.dto.auth.RegisterRequest;
import com.projeto.backend.web.openapi.AuthControllerOpenApi;

import jakarta.validation.Valid;

/**
 * Controller para endpoints de autenticação.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthControllerOpenApi {
	
	 private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

	 @Autowired
	 private AuthService authService;
	 
	/**
     * Realiza o login do usuário.
     *
     * @param request Dados de login (username e password)
     * @return Tokens JWT e informações do usuário
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        logger.info("Requisição de login recebida para usuário: {}", request.getUsername());
        
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Registra um novo usuário.
     *
     * @param request Dados de registro
     * @return Tokens JWT e informações do usuário criado
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Requisição de registro recebida para usuário: {}", request.getUsername());
        
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Renova o token de acesso usando um refresh token válido.
     *
     * @param request Dados com refresh token
     * @return Novo token de acesso
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        logger.info("Requisição de refresh token recebida");
        
        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }
}
