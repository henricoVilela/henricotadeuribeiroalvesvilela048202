package com.projeto.backend.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.backend.security.AuthService;
import com.projeto.backend.web.dto.auth.AuthRequest;
import com.projeto.backend.web.dto.auth.AuthResponse;

import jakarta.validation.Valid;

/**
 * Controller para endpoints de autenticação.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	
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
}
