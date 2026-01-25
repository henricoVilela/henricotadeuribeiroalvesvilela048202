package com.projeto.backend.domain.album;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.backend.domain.artista.Artista;
import com.projeto.backend.domain.artista.ArtistaService;
import com.projeto.backend.infrastructure.websocket.NotificationService;
import com.projeto.backend.web.dto.album.AlbumRequest;
import com.projeto.backend.web.dto.album.AlbumResponse;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

/**
 * Serviço para operações de negócio relacionadas a álbuns.
 */
@Service
public class AlbumService {

    private static final Logger logger = LoggerFactory.getLogger(AlbumService.class);

    @Autowired
    private AlbumRepository albumRepository;
    
    @Autowired
    private ArtistaService artistaService;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Lista todos os álbuns de um artista (sem paginação).
     *
     * @param artistaId ID do artista
     * @return Lista de álbuns
     */
    @Transactional(readOnly = true)
    public List<AlbumResponse> listarTodosPorArtista(Long artistaId) {
        logger.info("Listando todos os álbuns do artista ID: {}", artistaId);

        artistaService.buscarEntidadePorId(artistaId);

        List<Album> albuns = albumRepository.findByArtistaIdAndAtivoTrueOrderByAnoLancamentoDesc(artistaId);

        return albuns.stream()
                .map(AlbumResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista álbuns com paginação e filtros.
     *
     * @param nome Filtro por nome do álbum (opcional)
     * @param nomeArtista Filtro por nome do artista (opcional)
     * @param genero Filtro por gênero (opcional)
     * @param page Número da página
     * @param size Tamanho da página
     * @param sortBy Campo para ordenação
     * @param sortDir Direção da ordenação (asc/desc)
     * @return Página de álbuns
     */
    @Transactional(readOnly = true)
    public Page<AlbumResponse> listar(
            String nome,
            String nomeArtista,
            String genero,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        logger.info("Listando álbuns - nome: {}, artista: {}, genero: {}, page: {}, size: {}",
                nome, nomeArtista, genero, page, size);

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        String nomeParam = (nome != null && !nome.trim().isEmpty()) ? nome.trim().toLowerCase() : null;
        String artistaParam = (nomeArtista != null && !nomeArtista.trim().isEmpty()) ? nomeArtista.trim().toLowerCase() : null;
        String generoParam = (genero != null && !genero.trim().isEmpty()) ? genero.trim().toLowerCase() : null;

        var spec = AlbumService.comFiltros(nomeParam, artistaParam, generoParam);
        Page<Album> pageResult = albumRepository.findAll(spec, pageable);

        return pageResult.map(AlbumResponse::fromEntity);
    }
    
    /**
     * Busca álbum por ID.
     *
     * @param id ID do álbum
     * @return AlbumResponse
     * @throws EntityNotFoundException Se não encontrar
     */
    @Transactional(readOnly = true)
    public AlbumResponse buscarPorId(Long id) {
        logger.info("Buscando álbum por ID: {}", id);

        Album album = albumRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Álbum não encontrado com ID: " + id));

        return AlbumResponse.fromEntity(album);
    }
    
    /**
     * Lista gêneros distintos.
     *
     * @return Lista de gêneros
     */
    @Transactional(readOnly = true)
    public List<String> listarGeneros() {
        return albumRepository.findDistinctGeneros();
    }
    
    /**
     * Cria um novo álbum.
     *
     * @param request Dados do álbum
     * @return AlbumResponse do álbum criado
     */
    @Transactional
    public AlbumResponse criar(AlbumRequest request) {
        logger.info("Criando álbum: {} para artista ID: {}", request.getNome(), request.getArtistaId());

        Artista artista = artistaService.buscarEntidadePorId(request.getArtistaId());

        Album album = new Album();
        album.setArtista(artista);
        album.setNome(request.getNome());
        album.setAnoLancamento(request.getAnoLancamento());
        album.setGravadora(request.getGravadora());
        album.setGenero(request.getGenero());
        album.setTotalFaixas(request.getTotalFaixas());
        album.setDuracaoTotal(request.getDuracaoTotal());
        album.setDescricao(request.getDescricao());
        album.setAtivo(true);

        album = albumRepository.save(album);
        logger.info("Álbum criado com ID: {}", album.getId());
        
        notificationService.notifyAlbumCreated(album.getId(), album.getNome(), artista.getNome());
        
        return AlbumResponse.fromEntity(album);
    }
    
    /**
     * Atualiza um álbum existente.
     *
     * @param id ID do álbum
     * @param request Novos dados
     * @return AlbumResponse atualizado
     * @throws EntityNotFoundException Se não encontrar
     */
    @Transactional
    public AlbumResponse atualizar(Long id, AlbumRequest request) {
        logger.info("Atualizando álbum ID: {}", id);

        Album album = albumRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Álbum não encontrado com ID: " + id));

        if (!album.getArtista().getId().equals(request.getArtistaId())) {
            Artista novoArtista = artistaService.buscarEntidadePorId(request.getArtistaId());
            album.setArtista(novoArtista);
        }

        album.setNome(request.getNome());
        album.setAnoLancamento(request.getAnoLancamento());
        album.setGravadora(request.getGravadora());
        album.setGenero(request.getGenero());
        album.setTotalFaixas(request.getTotalFaixas());
        album.setDuracaoTotal(request.getDuracaoTotal());
        album.setDescricao(request.getDescricao());

        album = albumRepository.save(album);
        logger.info("Álbum atualizado: {}", album.getId());
        
        notificationService.notifyAlbumUpdated(album.getId(), album.getNome(), album.getArtista().getNome());
        
        return AlbumResponse.fromEntity(album);
    }
    
    /**
     * Remove (inativa) um álbum.
     *
     * @param id ID do álbum
     * @throws EntityNotFoundException Se não encontrar
     */
    @Transactional
    public void deletar(Long id) {
        logger.info("Deletando álbum ID: {}", id);

        Album album = albumRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Álbum não encontrado com ID: " + id));

        album.setAtivo(false);
        albumRepository.save(album);
        
        notificationService.notifyAlbumDeleted(id);

        logger.info("Álbum inativado: {}", id);
    }
    
    public static Specification<Album> comFiltros(String nome, String nomeArtista, String genero) {
        return (root, query, cb) -> {
            Join<Object, Object> artista = root.join("artista");

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isTrue(root.get("ativo")));
            predicates.add(cb.isTrue(artista.get("ativo")));

            if (nome != null) {
                predicates.add(
                    cb.like(
                        cb.lower(root.get("nome")),
                        "%" + nome.toLowerCase() + "%"
                    )
                );
            }

            if (nomeArtista != null) {
                predicates.add(
                    cb.like(
                        cb.lower(artista.get("nome")),
                        "%" + nomeArtista.toLowerCase() + "%"
                    )
                );
            }

            if (genero != null) {
                predicates.add(
                    cb.equal(
                        cb.lower(root.get("genero")),
                        genero.toLowerCase()
                    )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
