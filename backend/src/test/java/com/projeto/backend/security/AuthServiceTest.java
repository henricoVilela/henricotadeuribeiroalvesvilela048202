package com.projeto.backend.security;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.projeto.backend.domain.usuario.Usuario;
import com.projeto.backend.domain.usuario.UsuarioRepository;
import com.projeto.backend.security.jwt.JwtService;
import com.projeto.backend.web.dto.auth.AuthRequest;
import com.projeto.backend.web.dto.auth.AuthResponse;
import com.projeto.backend.web.dto.auth.RefreshRequest;
import com.projeto.backend.web.dto.auth.RegisterRequest;

/**
 * Testes unitários para AuthService.
 * 
 * Cobertura:
 * - Login com credenciais válidas/inválidas
 * - Registro de novos usuários
 * - Refresh token
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;
    private AuthRequest authRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("admin");
        usuario.setEmail("admin@teste.com");
        usuario.setPasswordHash("$2a$10$encodedPassword");
        usuario.setNomeCompleto("Administrador");
        usuario.setAtivo(true);

        authRequest = new AuthRequest();
        authRequest.setUsername("admin");
        authRequest.setPassword("admin123");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("novouser");
        registerRequest.setEmail("novo@teste.com");
        registerRequest.setPassword("senha123");
        registerRequest.setNomeCompleto("Novo Usuário");
    }

    @Nested
    @DisplayName("Login")
    class Login {

        @Test
        @DisplayName("Deve realizar login com sucesso")
        void deveRealizarLoginComSucesso() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null); // autenticação bem sucedida
            when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
            when(jwtService.generateToken(usuario)).thenReturn("access_token_123");
            when(jwtService.generateRefreshToken(usuario)).thenReturn("refresh_token_456");
            when(jwtService.getJwtExpiration()).thenReturn(300000L);

            // Act
            AuthResponse resultado = authService.login(authRequest);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getAccessToken()).isEqualTo("access_token_123");
            assertThat(resultado.getRefreshToken()).isEqualTo("refresh_token_456");
            assertThat(resultado.getTokenType()).isEqualTo("Bearer");
            assertThat(resultado.getUsername()).isEqualTo("admin");
            assertThat(resultado.getEmail()).isEqualTo("admin@teste.com");
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("Deve lançar exceção com credenciais inválidas")
        void deveLancarExcecaoComCredenciaisInvalidas() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Credenciais inválidas"));

            // Act & Assert
            assertThatThrownBy(() -> authService.login(authRequest))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Credenciais inválidas");
            
            verify(usuarioRepository, never()).findByUsername(anyString());
        }
        
        @Test
        @DisplayName("Deve lançar exceção quando usuário não encontrado após autenticação")
        void deveLancarExcecaoQuandoUsuarioNaoEncontradoAposAutenticacao() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.login(authRequest))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("Usuário não encontrado");
        }
    }

    @Nested
    @DisplayName("Registro")
    class Registro {

        @Test
        @DisplayName("Deve registrar novo usuário com sucesso")
        void deveRegistrarNovoUsuarioComSucesso() {
            // Arrange
            when(usuarioRepository.existsByUsername("novouser")).thenReturn(false);
            when(usuarioRepository.existsByEmail("novo@teste.com")).thenReturn(false);
            when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$encodedNewPassword");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
                Usuario saved = invocation.getArgument(0);
                saved.setId(2L);
                return saved;
            });
            when(jwtService.generateToken(any(Usuario.class))).thenReturn("new_access_token");
            when(jwtService.generateRefreshToken(any(Usuario.class))).thenReturn("new_refresh_token");
            when(jwtService.getJwtExpiration()).thenReturn(300000L);

            // Act
            AuthResponse resultado = authService.register(registerRequest);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getAccessToken()).isEqualTo("new_access_token");
            assertThat(resultado.getRefreshToken()).isEqualTo("new_refresh_token");
            assertThat(resultado.getUsername()).isEqualTo("novouser");
            verify(usuarioRepository).existsByUsername("novouser");
            verify(usuarioRepository).existsByEmail("novo@teste.com");
            verify(passwordEncoder).encode("senha123");
            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando username já existe")
        void deveLancarExcecaoQuandoUsernameJaExiste() {
            // Arrange
            when(usuarioRepository.existsByUsername("novouser")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Username já está em uso");
            
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando email já existe")
        void deveLancarExcecaoQuandoEmailJaExiste() {
            // Arrange
            when(usuarioRepository.existsByUsername("novouser")).thenReturn(false);
            when(usuarioRepository.existsByEmail("novo@teste.com")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("E-mail já está em uso");
            
            verify(usuarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Refresh Token")
    class RefreshToken {

        @Test
        @DisplayName("Deve renovar token com sucesso")
        void deveRenovarTokenComSucesso() {
            // Arrange
            RefreshRequest refreshRequest = new RefreshRequest();
            refreshRequest.setRefreshToken("valid_refresh_token");

            when(jwtService.isRefreshToken("valid_refresh_token")).thenReturn(true);
            when(jwtService.extractUsername("valid_refresh_token")).thenReturn("admin");
            when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
            when(jwtService.validateToken("valid_refresh_token", usuario)).thenReturn(true);
            when(jwtService.generateToken(usuario)).thenReturn("new_access_token");
            when(jwtService.getJwtExpiration()).thenReturn(300000L);

            // Act
            AuthResponse resultado = authService.refresh(refreshRequest);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getAccessToken()).isEqualTo("new_access_token");
            assertThat(resultado.getRefreshToken()).isEqualTo("valid_refresh_token");
            assertThat(resultado.getUsername()).isEqualTo("admin");
            verify(jwtService).isRefreshToken("valid_refresh_token");
            verify(jwtService).validateToken("valid_refresh_token", usuario);
        }

        @Test
        @DisplayName("Deve lançar exceção quando não é refresh token")
        void deveLancarExcecaoQuandoNaoERefreshToken() {
            // Arrange
            RefreshRequest refreshRequest = new RefreshRequest();
            refreshRequest.setRefreshToken("access_token_not_refresh");

            when(jwtService.isRefreshToken("access_token_not_refresh")).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> authService.refresh(refreshRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Token inválido. Não é um refresh token");
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não encontrado")
        void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
            // Arrange
            RefreshRequest refreshRequest = new RefreshRequest();
            refreshRequest.setRefreshToken("valid_token");

            when(jwtService.isRefreshToken("valid_token")).thenReturn(true);
            when(jwtService.extractUsername("valid_token")).thenReturn("inexistente");
            when(usuarioRepository.findByUsername("inexistente")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.refresh(refreshRequest))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("Usuário não encontrado");
        }
        
        @Test
        @DisplayName("Deve lançar exceção quando refresh token expirado")
        void deveLancarExcecaoQuandoRefreshTokenExpirado() {
            // Arrange
            RefreshRequest refreshRequest = new RefreshRequest();
            refreshRequest.setRefreshToken("expired_refresh_token");

            when(jwtService.isRefreshToken("expired_refresh_token")).thenReturn(true);
            when(jwtService.extractUsername("expired_refresh_token")).thenReturn("admin");
            when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
            when(jwtService.validateToken("expired_refresh_token", usuario)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> authService.refresh(refreshRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Refresh token expirado ou inválido");
        }
    }
}
