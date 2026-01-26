package com.projeto.backend.domain;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.projeto.backend.domain.artista.Artista;
import com.projeto.backend.domain.artista.ArtistaRepository;
import com.projeto.backend.domain.artista.ArtistaService;
import com.projeto.backend.domain.artista.TipoArtista;
import com.projeto.backend.infrastructure.websocket.NotificationService;
import com.projeto.backend.web.dto.artista.ArtistaRequest;
import com.projeto.backend.web.dto.artista.ArtistaResponse;

import jakarta.persistence.EntityNotFoundException;

/**
 * Testes unitários para ArtistaService.
 * 
 * Cobertura:
 * - Listar artistas com paginação
 * - Buscar por ID
 * - Criar artista
 * - Atualizar artista
 * - Deletar (soft delete)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ArtistaService")
class ArtistaServiceTest {

    @Mock
    private ArtistaRepository artistaRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ArtistaService artistaService;

    private Artista artista;
    private ArtistaRequest artistaRequest;

    @BeforeEach
    void setUp() {
        artista = new Artista();
        artista.setId(1L);
        artista.setNome("Legião Urbana");
        artista.setTipo(TipoArtista.BANDA);
        artista.setPaisOrigem("Brasil");
        artista.setAnoFormacao(1982);
        artista.setBiografia("Banda de rock brasileira");
        artista.setAtivo(true);

        artistaRequest = new ArtistaRequest();
        artistaRequest.setNome("Legião Urbana");
        artistaRequest.setTipo(TipoArtista.BANDA);
        artistaRequest.setPaisOrigem("Brasil");
        artistaRequest.setAnoFormacao(1982);
        artistaRequest.setBiografia("Banda de rock brasileira");
    }

    @Nested
    @DisplayName("Listar Artistas")
    class ListarArtistas {

        @Test
        @DisplayName("Deve listar artistas com paginação sem filtro")
        void deveListarArtistasComPaginacaoSemFiltro() {
            // Arrange
            Object[] artistaComCount = new Object[]{artista, 5L};
            List<Object[]> content = new ArrayList<>();
            content.add(artistaComCount);
            Page<Object[]> page = new PageImpl<Object[]>(content);
            
            when(artistaRepository.findAllWithAlbumCount(any(Pageable.class))).thenReturn(page);

            // Act
            Page<ArtistaResponse> resultado = artistaService.listar(null, 0, 10, "nome", "asc");

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(1);
            assertThat(resultado.getContent().get(0).getNome()).isEqualTo("Legião Urbana");
            assertThat(resultado.getContent().get(0).getTotalAlbuns()).isEqualTo(5);
            verify(artistaRepository).findAllWithAlbumCount(any(Pageable.class));
        }

        @Test
        @DisplayName("Deve listar artistas com filtro por nome")
        void deveListarArtistasComFiltroPorNome() {
            // Arrange
            Object[] artistaComCount = new Object[]{artista, 5L};
            List<Object[]> content = new ArrayList<>();
            content.add(artistaComCount);
            Page<Object[]> page = new PageImpl<Object[]>(content);
            
            when(artistaRepository.findByNomeWithAlbumCount(anyString(), any(Pageable.class))).thenReturn(page);

            // Act
            Page<ArtistaResponse> resultado = artistaService.listar("legiao", 0, 10, "nome", "asc");

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(1);
            verify(artistaRepository).findByNomeWithAlbumCount(eq("legiao"), any(Pageable.class));
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não houver artistas")
        void deveRetornarPaginaVaziaQuandoNaoHouverArtistas() {
            // Arrange
            Page<Object[]> page = new PageImpl<>(List.of());
            when(artistaRepository.findAllWithAlbumCount(any(Pageable.class))).thenReturn(page);

            // Act
            Page<ArtistaResponse> resultado = artistaService.listar(null, 0, 10, "nome", "asc");

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).isEmpty();
            assertThat(resultado.getTotalElements()).isZero();
        }
        
        @Test
        @DisplayName("Deve ordenar artistas em ordem descendente")
        void deveOrdenarArtistasEmOrdemDescendente() {
            // Arrange
            Object[] artistaComCount = new Object[]{artista, 5L};
            List<Object[]> content = new ArrayList<>();
            content.add(artistaComCount);
            Page<Object[]> page = new PageImpl<Object[]>(content);
            
            when(artistaRepository.findAllWithAlbumCount(any(Pageable.class))).thenReturn(page);

            // Act
            Page<ArtistaResponse> resultado = artistaService.listar(null, 0, 10, "nome", "desc");

            // Assert
            assertThat(resultado).isNotNull();
            verify(artistaRepository).findAllWithAlbumCount(argThat(pageable -> 
                pageable.getSort().getOrderFor("nome").getDirection().isDescending()
            ));
        }
    }

    @Nested
    @DisplayName("Buscar por ID")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar artista quando encontrado")
        void deveRetornarArtistaQuandoEncontrado() {
            // Arrange
            when(artistaRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(artista));

            // Act
            ArtistaResponse resultado = artistaService.buscarPorId(1L);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNome()).isEqualTo("Legião Urbana");
            assertThat(resultado.getTipo()).isEqualTo(TipoArtista.BANDA);
            verify(artistaRepository).findByIdAndAtivoTrue(1L);
        }

        @Test
        @DisplayName("Deve lançar exceção quando artista não encontrado")
        void deveLancarExcecaoQuandoArtistaNaoEncontrado() {
            // Arrange
            when(artistaRepository.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> artistaService.buscarPorId(99L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Artista não encontrado");
        }
    }

    @Nested
    @DisplayName("Criar Artista")
    class CriarArtista {

        @Test
        @DisplayName("Deve criar artista com sucesso")
        void deveCriarArtistaComSucesso() {
            // Arrange
            when(artistaRepository.existsByNomeIgnoreCase("Legião Urbana")).thenReturn(false);
            when(artistaRepository.save(any(Artista.class))).thenAnswer(invocation -> {
                Artista saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            // Act
            ArtistaResponse resultado = artistaService.criar(artistaRequest);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getNome()).isEqualTo("Legião Urbana");
            assertThat(resultado.getTipo()).isEqualTo(TipoArtista.BANDA);
            verify(artistaRepository).existsByNomeIgnoreCase("Legião Urbana");
            verify(artistaRepository).save(any(Artista.class));
            verify(notificationService).notifyArtistaCreated(anyLong(), eq("Legião Urbana"));
        }

        @Test
        @DisplayName("Deve lançar exceção quando nome já existe")
        void deveLancarExcecaoQuandoNomeJaExiste() {
            // Arrange
            when(artistaRepository.existsByNomeIgnoreCase("Legião Urbana")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> artistaService.criar(artistaRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Já existe um artista com este nome");
            
            verify(artistaRepository, never()).save(any());
            verify(notificationService, never()).notifyArtistaCreated(anyLong(), anyString());
        }

        @Test
        @DisplayName("Deve criar artista apenas com campos obrigatórios")
        void deveCriarArtistaApenasComCamposObrigatorios() {
            // Arrange
            ArtistaRequest requestMinimo = new ArtistaRequest();
            requestMinimo.setNome("Artista Teste");
            requestMinimo.setTipo(TipoArtista.CANTOR);

            when(artistaRepository.existsByNomeIgnoreCase("Artista Teste")).thenReturn(false);
            when(artistaRepository.save(any(Artista.class))).thenAnswer(invocation -> {
                Artista saved = invocation.getArgument(0);
                saved.setId(2L);
                return saved;
            });

            // Act
            ArtistaResponse resultado = artistaService.criar(requestMinimo);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getNome()).isEqualTo("Artista Teste");
            assertThat(resultado.getPaisOrigem()).isNull();
            assertThat(resultado.getAnoFormacao()).isNull();
        }
    }

    @Nested
    @DisplayName("Atualizar Artista")
    class AtualizarArtista {

        @Test
        @DisplayName("Deve atualizar artista com sucesso")
        void deveAtualizarArtistaComSucesso() {
            // Arrange
            when(artistaRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(artista));
            when(artistaRepository.save(any(Artista.class))).thenReturn(artista);

            // Act
            ArtistaResponse resultado = artistaService.atualizar(1L, artistaRequest);

            // Assert
            assertThat(resultado).isNotNull();
            verify(artistaRepository).findByIdAndAtivoTrue(1L);
            verify(artistaRepository).save(any(Artista.class));
            verify(notificationService).notifyArtistaUpdated(eq(1L), eq("Legião Urbana"));
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar artista inexistente")
        void deveLancarExcecaoAoAtualizarArtistaInexistente() {
            // Arrange
            when(artistaRepository.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> artistaService.atualizar(99L, artistaRequest))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Artista não encontrado");
            
            verify(artistaRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Deve lançar exceção ao alterar para nome já existente")
        void deveLancarExcecaoAoAlterarParaNomeJaExistente() {
            // Arrange
            ArtistaRequest novoNomeRequest = new ArtistaRequest();
            novoNomeRequest.setNome("Outro Artista");
            novoNomeRequest.setTipo(TipoArtista.BANDA);
            
            when(artistaRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(artista));
            when(artistaRepository.existsByNomeIgnoreCase("Outro Artista")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> artistaService.atualizar(1L, novoNomeRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Já existe um artista com este nome");
            
            verify(artistaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Deletar Artista")
    class DeletarArtista {

        @Test
        @DisplayName("Deve realizar soft delete do artista")
        void deveRealizarSoftDeleteDoArtista() {
            // Arrange
            when(artistaRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(artista));
            when(artistaRepository.save(any(Artista.class))).thenReturn(artista);

            // Act
            artistaService.deletar(1L);

            // Assert
            verify(artistaRepository).findByIdAndAtivoTrue(1L);
            verify(artistaRepository).save(argThat(a -> !a.getAtivo()));
            verify(notificationService).notifyArtistaDeleted(1L);
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar artista inexistente")
        void deveLancarExcecaoAoDeletarArtistaInexistente() {
            // Arrange
            when(artistaRepository.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> artistaService.deletar(99L))
                    .isInstanceOf(EntityNotFoundException.class);
            
            verify(artistaRepository, never()).save(any());
            verify(notificationService, never()).notifyArtistaDeleted(anyLong());
        }
    }

    @Nested
    @DisplayName("Buscar Entidade Por ID")
    class BuscarEntidadePorId {

        @Test
        @DisplayName("Deve retornar entidade artista quando encontrada")
        void deveRetornarEntidadeArtistaQuandoEncontrada() {
            // Arrange
            when(artistaRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(artista));

            // Act
            Artista resultado = artistaService.buscarEntidadePorId(1L);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNome()).isEqualTo("Legião Urbana");
        }
        
        @Test
        @DisplayName("Deve lançar exceção quando entidade não encontrada")
        void deveLancarExcecaoQuandoEntidadeNaoEncontrada() {
            // Arrange
            when(artistaRepository.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> artistaService.buscarEntidadePorId(99L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Artista não encontrado");
        }
    }
}
