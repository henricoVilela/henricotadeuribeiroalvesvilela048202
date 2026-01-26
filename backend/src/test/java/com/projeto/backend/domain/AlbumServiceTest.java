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
import org.springframework.data.jpa.domain.Specification;

import com.projeto.backend.domain.album.Album;
import com.projeto.backend.domain.album.AlbumRepository;
import com.projeto.backend.domain.album.AlbumService;
import com.projeto.backend.domain.artista.Artista;
import com.projeto.backend.domain.artista.ArtistaService;
import com.projeto.backend.domain.artista.TipoArtista;
import com.projeto.backend.infrastructure.websocket.NotificationService;
import com.projeto.backend.web.dto.album.AlbumRequest;
import com.projeto.backend.web.dto.album.AlbumResponse;

import jakarta.persistence.EntityNotFoundException;

/**
 * Testes unitários para AlbumService.
 * 
 * Cobertura:
 * - Listar álbuns com paginação e filtros
 * - Listar álbuns por artista
 * - Buscar por ID
 * - Criar álbum
 * - Atualizar álbum
 * - Deletar álbum (soft delete)
 * - Listar gêneros
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AlbumService")
class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private ArtistaService artistaService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AlbumService albumService;

    private Album album;
    private Artista artista;
    private AlbumRequest albumRequest;

    @BeforeEach
    void setUp() {
        artista = new Artista();
        artista.setId(1L);
        artista.setNome("Legião Urbana");
        artista.setTipo(TipoArtista.BANDA);
        artista.setAtivo(true);

        album = new Album();
        album.setId(1L);
        album.setArtista(artista);
        album.setNome("Dois");
        album.setAnoLancamento(1986);
        album.setGravadora("EMI");
        album.setGenero("Rock");
        album.setTotalFaixas(10);
        album.setDuracaoTotal(2400);
        album.setAtivo(true);

        albumRequest = new AlbumRequest();
        albumRequest.setArtistaId(1L);
        albumRequest.setNome("Dois");
        albumRequest.setAnoLancamento(1986);
        albumRequest.setGravadora("EMI");
        albumRequest.setGenero("Rock");
        albumRequest.setTotalFaixas(10);
        albumRequest.setDuracaoTotal(2400);
    }

    @Nested
    @DisplayName("Listar Álbuns")
    class ListarAlbuns {

        @Test
        @DisplayName("Deve listar álbuns com paginação usando Specification")
        @SuppressWarnings("unchecked")
        void deveListarAlbunsComPaginacao() {
            // Arrange
            Page<Album> page = new PageImpl<>(List.of(album));
            when(albumRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            // Act
            Page<AlbumResponse> resultado = albumService.listar(null, null, null, 0, 10, "nome", "asc");

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(1);
            assertThat(resultado.getContent().get(0).getNome()).isEqualTo("Dois");
            verify(albumRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Deve filtrar álbuns por nome")
        @SuppressWarnings("unchecked")
        void deveFiltrarAlbunsPorNome() {
            // Arrange
            Page<Album> page = new PageImpl<>(List.of(album));
            when(albumRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            // Act
            Page<AlbumResponse> resultado = albumService.listar("dois", null, null, 0, 10, "nome", "asc");

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(1);
            verify(albumRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Deve filtrar álbuns por gênero")
        @SuppressWarnings("unchecked")
        void deveFiltrarAlbunsPorGenero() {
            // Arrange
            Page<Album> page = new PageImpl<>(List.of(album));
            when(albumRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            // Act
            Page<AlbumResponse> resultado = albumService.listar(null, null, "Rock", 0, 10, "nome", "asc");

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(1);
            verify(albumRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Deve filtrar álbuns por artista")
        @SuppressWarnings("unchecked")
        void deveFiltrarAlbunsPorArtista() {
            // Arrange
            Page<Album> page = new PageImpl<>(List.of(album));
            when(albumRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            // Act
            Page<AlbumResponse> resultado = albumService.listar(null, "legiao", null, 0, 10, "nome", "asc");

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(1);
            verify(albumRepository).findAll(any(Specification.class), any(Pageable.class));
        }
        
        @Test
        @DisplayName("Deve retornar página vazia quando não houver álbuns")
        @SuppressWarnings("unchecked")
        void deveRetornarPaginaVaziaQuandoNaoHouverAlbuns() {
            // Arrange
            Page<Album> page = new PageImpl<>(List.of());
            when(albumRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            // Act
            Page<AlbumResponse> resultado = albumService.listar(null, null, null, 0, 10, "nome", "asc");

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Listar Álbuns por Artista")
    class ListarAlbunsPorArtista {

        @Test
        @DisplayName("Deve retornar álbuns do artista")
        void deveRetornarAlbunsDoArtista() {
            // Arrange
            when(artistaService.buscarEntidadePorId(1L)).thenReturn(artista);
            when(albumRepository.findByArtistaIdAndAtivoTrueOrderByAnoLancamentoDesc(1L))
                    .thenReturn(List.of(album));

            // Act
            List<AlbumResponse> resultado = albumService.listarTodosPorArtista(1L);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getNome()).isEqualTo("Dois");
            verify(artistaService).buscarEntidadePorId(1L);
            verify(albumRepository).findByArtistaIdAndAtivoTrueOrderByAnoLancamentoDesc(1L);
        }
        
        @Test
        @DisplayName("Deve retornar lista vazia se artista não tem álbuns")
        void deveRetornarListaVaziaSeArtistaNaoTemAlbuns() {
            // Arrange
            when(artistaService.buscarEntidadePorId(1L)).thenReturn(artista);
            when(albumRepository.findByArtistaIdAndAtivoTrueOrderByAnoLancamentoDesc(1L))
                    .thenReturn(List.of());

            // Act
            List<AlbumResponse> resultado = albumService.listarTodosPorArtista(1L);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("Buscar por ID")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar álbum quando encontrado")
        void deveRetornarAlbumQuandoEncontrado() {
            // Arrange
            when(albumRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(album));

            // Act
            AlbumResponse resultado = albumService.buscarPorId(1L);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNome()).isEqualTo("Dois");
            assertThat(resultado.getArtistaNome()).isEqualTo("Legião Urbana");
            verify(albumRepository).findByIdAndAtivoTrue(1L);
        }

        @Test
        @DisplayName("Deve lançar exceção quando álbum não encontrado")
        void deveLancarExcecaoQuandoAlbumNaoEncontrado() {
            // Arrange
            when(albumRepository.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> albumService.buscarPorId(99L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Álbum não encontrado");
        }
    }

    @Nested
    @DisplayName("Criar Álbum")
    class CriarAlbum {

        @Test
        @DisplayName("Deve criar álbum com sucesso")
        void deveCriarAlbumComSucesso() {
            // Arrange
            when(artistaService.buscarEntidadePorId(1L)).thenReturn(artista);
            when(albumRepository.save(any(Album.class))).thenAnswer(invocation -> {
                Album saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            // Act
            AlbumResponse resultado = albumService.criar(albumRequest);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getNome()).isEqualTo("Dois");
            assertThat(resultado.getArtistaNome()).isEqualTo("Legião Urbana");
            verify(artistaService).buscarEntidadePorId(1L);
            verify(albumRepository).save(any(Album.class));
            verify(notificationService).notifyAlbumCreated(anyLong(), eq("Dois"), eq("Legião Urbana"));
        }

        @Test
        @DisplayName("Deve lançar exceção quando artista não existe")
        void deveLancarExcecaoQuandoArtistaNaoExiste() {
            // Arrange
            when(artistaService.buscarEntidadePorId(99L))
                    .thenThrow(new EntityNotFoundException("Artista não encontrado"));
            albumRequest.setArtistaId(99L);

            // Act & Assert
            assertThatThrownBy(() -> albumService.criar(albumRequest))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Artista não encontrado");
            
            verify(albumRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve criar álbum apenas com campos obrigatórios")
        void deveCriarAlbumApenasComCamposObrigatorios() {
            // Arrange
            AlbumRequest requestMinimo = new AlbumRequest();
            requestMinimo.setArtistaId(1L);
            requestMinimo.setNome("Álbum Teste");

            when(artistaService.buscarEntidadePorId(1L)).thenReturn(artista);
            when(albumRepository.save(any(Album.class))).thenAnswer(invocation -> {
                Album saved = invocation.getArgument(0);
                saved.setId(2L);
                return saved;
            });

            // Act
            AlbumResponse resultado = albumService.criar(requestMinimo);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getNome()).isEqualTo("Álbum Teste");
            assertThat(resultado.getGravadora()).isNull();
            assertThat(resultado.getGenero()).isNull();
        }
    }

    @Nested
    @DisplayName("Atualizar Álbum")
    class AtualizarAlbum {

        @Test
        @DisplayName("Deve atualizar álbum com sucesso mantendo mesmo artista")
        void deveAtualizarAlbumComSucessoMantendoMesmoArtista() {
            // Arrange
            when(albumRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(album));
            when(albumRepository.save(any(Album.class))).thenReturn(album);

            // Act
            AlbumResponse resultado = albumService.atualizar(1L, albumRequest);

            // Assert
            assertThat(resultado).isNotNull();
            verify(albumRepository).findByIdAndAtivoTrue(1L);
            verify(albumRepository).save(any(Album.class));
            verify(notificationService).notifyAlbumUpdated(eq(1L), anyString(), anyString());
        }
        
        @Test
        @DisplayName("Deve atualizar álbum e trocar de artista")
        void deveAtualizarAlbumETrocarDeArtista() {
            // Arrange
            Artista novoArtista = new Artista();
            novoArtista.setId(2L);
            novoArtista.setNome("Titãs");
            novoArtista.setTipo(TipoArtista.BANDA);
            
            AlbumRequest requestNovoArtista = new AlbumRequest();
            requestNovoArtista.setArtistaId(2L);
            requestNovoArtista.setNome("Dois");
            
            when(albumRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(album));
            when(artistaService.buscarEntidadePorId(2L)).thenReturn(novoArtista);
            when(albumRepository.save(any(Album.class))).thenReturn(album);

            // Act
            AlbumResponse resultado = albumService.atualizar(1L, requestNovoArtista);

            // Assert
            assertThat(resultado).isNotNull();
            verify(artistaService).buscarEntidadePorId(2L);
            verify(albumRepository).save(any(Album.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar álbum inexistente")
        void deveLancarExcecaoAoAtualizarAlbumInexistente() {
            // Arrange
            when(albumRepository.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> albumService.atualizar(99L, albumRequest))
                    .isInstanceOf(EntityNotFoundException.class);
            
            verify(albumRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Deletar Álbum")
    class DeletarAlbum {

        @Test
        @DisplayName("Deve realizar soft delete do álbum")
        void deveRealizarSoftDeleteDoAlbum() {
            // Arrange
            when(albumRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(album));
            when(albumRepository.save(any(Album.class))).thenReturn(album);

            // Act
            albumService.deletar(1L);

            // Assert
            verify(albumRepository).findByIdAndAtivoTrue(1L);
            verify(albumRepository).save(argThat(a -> !a.getAtivo()));
            verify(notificationService).notifyAlbumDeleted(1L);
        }
        
        @Test
        @DisplayName("Deve lançar exceção ao deletar álbum inexistente")
        void deveLancarExcecaoAoDeletarAlbumInexistente() {
            // Arrange
            when(albumRepository.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> albumService.deletar(99L))
                    .isInstanceOf(EntityNotFoundException.class);
            
            verify(albumRepository, never()).save(any());
            verify(notificationService, never()).notifyAlbumDeleted(anyLong());
        }
    }

    @Nested
    @DisplayName("Listar Gêneros")
    class ListarGeneros {

        @Test
        @DisplayName("Deve listar todos os gêneros distintos")
        void deveListarTodosOsGenerosDistintos() {
            // Arrange
            List<String> generos = List.of("MPB", "Pop", "Rock", "Sertanejo");
            when(albumRepository.findDistinctGeneros()).thenReturn(generos);

            // Act
            List<String> resultado = albumService.listarGeneros();

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado).hasSize(4);
            assertThat(resultado).containsExactly("MPB", "Pop", "Rock", "Sertanejo");
            verify(albumRepository).findDistinctGeneros();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não houver gêneros")
        void deveRetornarListaVaziaQuandoNaoHouverGeneros() {
            // Arrange
            when(albumRepository.findDistinctGeneros()).thenReturn(List.of());

            // Act
            List<String> resultado = albumService.listarGeneros();

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado).isEmpty();
        }
    }
}
