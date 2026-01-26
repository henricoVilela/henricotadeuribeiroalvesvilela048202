package com.projeto.backend.security;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.projeto.backend.domain.usuario.Usuario;
import com.projeto.backend.security.jwt.JwtService;

/**
 * Testes unitários para JwtService.
 * 
 * Cobertura:
 * - Geração de access token
 * - Geração de refresh token
 * - Validação de token
 * - Extração de username
 * - Verificação de tipo de token
 */
@DisplayName("JwtService")
class JwtServiceTest {

    private JwtService jwtService;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        
        // Configurar valores via reflection (simulando @Value)
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "test-secret-key-for-unit-tests");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 300000L); // 5 min
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 86400000L); // 24h

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("testuser");
        usuario.setEmail("test@teste.com");
        usuario.setPasswordHash("$2a$10$encodedPassword");
        usuario.setNomeCompleto("Usuário Teste");
        usuario.setAtivo(true);
    }

    @Nested
    @DisplayName("Geração de Token")
    class GeracaoToken {

        @Test
        @DisplayName("Deve gerar access token válido")
        void deveGerarAccessTokenValido() {
            // Act
            String token = jwtService.generateToken(usuario);

            // Assert
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // Header.Payload.Signature
        }

        @Test
        @DisplayName("Deve gerar refresh token válido")
        void deveGerarRefreshTokenValido() {
            // Act
            String token = jwtService.generateRefreshToken(usuario);

            // Assert
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("Access e refresh tokens devem ser diferentes")
        void accessERefreshTokensDevemSerDiferentes() {
            // Act
            String accessToken = jwtService.generateToken(usuario);
            String refreshToken = jwtService.generateRefreshToken(usuario);

            // Assert
            assertThat(accessToken).isNotEqualTo(refreshToken);
        }
    }

    @Nested
    @DisplayName("Extração de Username")
    class ExtracaoUsername {

        @Test
        @DisplayName("Deve extrair username do access token")
        void deveExtrairUsernameDoAccessToken() {
            // Arrange
            String token = jwtService.generateToken(usuario);

            // Act
            String username = jwtService.extractUsername(token);

            // Assert
            assertThat(username).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Deve extrair username do refresh token")
        void deveExtrairUsernameDoRefreshToken() {
            // Arrange
            String token = jwtService.generateRefreshToken(usuario);

            // Act
            String username = jwtService.extractUsername(token);

            // Assert
            assertThat(username).isEqualTo("testuser");
        }
    }

    @Nested
    @DisplayName("Extração de Expiração")
    class ExtracaoExpiracao {

        @Test
        @DisplayName("Deve extrair data de expiração do token")
        void deveExtrairDataExpiracaoDoToken() {
            // Arrange
            String token = jwtService.generateToken(usuario);

            // Act
            Date expiration = jwtService.extractExpiration(token);

            // Assert
            assertThat(expiration).isNotNull();
            assertThat(expiration).isAfter(new Date());
        }
    }

    @Nested
    @DisplayName("Validação de Token")
    class ValidacaoToken {

        @Test
        @DisplayName("Deve validar token com usuário correto")
        void deveValidarTokenComUsuarioCorreto() {
            // Arrange
            String token = jwtService.generateToken(usuario);

            // Act
            Boolean isValid = jwtService.validateToken(token, usuario);

            // Assert
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Deve rejeitar token com usuário diferente")
        void deveRejeitarTokenComUsuarioDiferente() {
            // Arrange
            String token = jwtService.generateToken(usuario);
            
            Usuario outroUsuario = new Usuario();
            outroUsuario.setUsername("outro_user");

            // Act
            Boolean isValid = jwtService.validateToken(token, outroUsuario);

            // Assert
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Deve rejeitar token com formato inválido")
        void deveRejeitarTokenComFormatoInvalido() {
            // Act
            Boolean isValid = jwtService.validateToken("invalid.token.format", usuario);

            // Assert
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("Verificação de Tipo de Token")
    class VerificacaoTipoToken {

        @Test
        @DisplayName("Deve identificar refresh token corretamente")
        void deveIdentificarRefreshTokenCorretamente() {
            // Arrange
            String refreshToken = jwtService.generateRefreshToken(usuario);

            // Act
            Boolean isRefresh = jwtService.isRefreshToken(refreshToken);

            // Assert
            assertThat(isRefresh).isTrue();
        }

        @Test
        @DisplayName("Deve identificar que access token não é refresh token")
        void deveIdentificarQueAccessTokenNaoERefreshToken() {
            // Arrange
            String accessToken = jwtService.generateToken(usuario);

            // Act
            Boolean isRefresh = jwtService.isRefreshToken(accessToken);

            // Assert
            assertThat(isRefresh).isFalse();
        }
        
        @Test
        @DisplayName("Deve retornar false para token inválido ao verificar tipo")
        void deveRetornarFalseParaTokenInvalidoAoVerificarTipo() {
            // Act
            Boolean isRefresh = jwtService.isRefreshToken("invalid_token");

            // Assert
            assertThat(isRefresh).isFalse();
        }
    }

    @Nested
    @DisplayName("Configurações de Expiração")
    class ConfiguracoesExpiracao {

        @Test
        @DisplayName("Deve retornar tempo de expiração do JWT")
        void deveRetornarTempoExpiracaoJwt() {
            // Act
            long expiration = jwtService.getJwtExpiration();

            // Assert
            assertThat(expiration).isEqualTo(300000L);
        }

        @Test
        @DisplayName("Deve retornar tempo de expiração do refresh token")
        void deveRetornarTempoExpiracaoRefreshToken() {
            // Act
            long expiration = jwtService.getRefreshExpiration();

            // Assert
            assertThat(expiration).isEqualTo(86400000L);
        }
    }
    
    @Nested
    @DisplayName("Tokens Diferentes para Usuários Diferentes")
    class TokensDiferentesParaUsuariosDiferentes {

        @Test
        @DisplayName("Deve gerar tokens diferentes para usuários diferentes")
        void deveGerarTokensDiferentesParaUsuariosDiferentes() {
            // Arrange
            Usuario outroUsuario = new Usuario();
            outroUsuario.setId(2L);
            outroUsuario.setUsername("outro_user");

            // Act
            String token1 = jwtService.generateToken(usuario);
            String token2 = jwtService.generateToken(outroUsuario);

            // Assert
            assertThat(token1).isNotEqualTo(token2);
        }
    }
}
