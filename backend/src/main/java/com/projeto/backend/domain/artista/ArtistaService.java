package com.projeto.backend.domain.artista;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.backend.infrastructure.websocket.NotificationService;
import com.projeto.backend.web.dto.artista.ArtistaRequest;
import com.projeto.backend.web.dto.artista.ArtistaResponse;

import jakarta.persistence.EntityNotFoundException;

/**
 * Serviço para operações de negócio relacionadas a artistas.
 */
@Service
public class ArtistaService {

    private static final Logger logger = LoggerFactory.getLogger(ArtistaService.class);

    @Autowired
    private ArtistaRepository artistaRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Lista artistas com paginação, filtro e ordenação.
     *
     * @param nome Filtro por nome (opcional)
     * @param page Número da página
     * @param size Tamanho da página
     * @param sortBy Campo para ordenação
     * @param sortDir Direção da ordenação (asc/desc)
     * @return Página de artistas
     */
    @Transactional(readOnly = true)
    public Page<ArtistaResponse> listar(String nome, int page, int size, String sortBy, String sortDir) {
        logger.info("Listando artistas - nome: {}, page: {}, size: {}, sortBy: {}, sortDir: {}",
                nome, page, size, sortBy, sortDir);

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Object[]> pageResult;

        if (nome != null && !nome.trim().isEmpty()) {
            pageResult = artistaRepository.findByNomeWithAlbumCount(nome.trim(), pageable);
        } else {
            pageResult = artistaRepository.findAllWithAlbumCount(pageable);
        }

        return pageResult.map(obj -> {
            Artista artista = (Artista) obj[0];
            Long totalAlbuns = (Long) obj[1];
            return ArtistaResponse.fromEntityWithAlbumCount(artista, totalAlbuns);
        });
    }
    
    /**
     * Busca artista por ID.
     *
     * @param id ID do artista
     * @return ArtistaResponse
     * @throws EntityNotFoundException Se não encontrar
     */
    @Transactional(readOnly = true)
    public ArtistaResponse buscarPorId(Long id) {
        logger.info("Buscando artista por ID: {}", id);

        Artista artista = artistaRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Artista não encontrado com ID: " + id));

        return ArtistaResponse.fromEntity(artista);
    }

    /**
     * Cria um novo artista.
     *
     * @param request Dados do artista
     * @return ArtistaResponse do artista criado
     */
    @Transactional
    public ArtistaResponse criar(ArtistaRequest request) {
        logger.info("Criando artista: {}", request.getNome());

        if (artistaRepository.existsByNomeIgnoreCase(request.getNome())) {
            throw new IllegalArgumentException("Já existe um artista com este nome: " + request.getNome());
        }

        Artista artista = new Artista();
        artista.setNome(request.getNome());
        artista.setTipo(request.getTipo());
        artista.setPaisOrigem(request.getPaisOrigem());
        artista.setAnoFormacao(request.getAnoFormacao());
        artista.setBiografia(request.getBiografia());
        artista.setAtivo(true);

        artista = artistaRepository.save(artista);
        logger.info("Artista criado com ID: {}", artista.getId());

        notificationService.notifyArtistaCreated(artista.getId(), artista.getNome());
        
        return ArtistaResponse.fromEntity(artista);
    }
    
    /**
     * Atualiza um artista existente.
     *
     * @param id ID do artista
     * @param request Novos dados
     * @return ArtistaResponse atualizado
     * @throws EntityNotFoundException Se não encontrar
     */
    @Transactional
    public ArtistaResponse atualizar(Long id, ArtistaRequest request) {
        logger.info("Atualizando artista ID: {}", id);

        Artista artista = artistaRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Artista não encontrado com ID: " + id));

        if (!artista.getNome().equalsIgnoreCase(request.getNome()) &&
                artistaRepository.existsByNomeIgnoreCase(request.getNome())) {
            throw new IllegalArgumentException("Já existe um artista com este nome: " + request.getNome());
        }

        artista.setNome(request.getNome());
        artista.setTipo(request.getTipo());
        artista.setPaisOrigem(request.getPaisOrigem());
        artista.setAnoFormacao(request.getAnoFormacao());
        artista.setBiografia(request.getBiografia());

        artista = artistaRepository.save(artista);
        logger.info("Artista atualizado: {}", artista.getId());

        var response = ArtistaResponse.fromEntity(artista);
        response.setUpdatedAt(LocalDateTime.now());
        
        notificationService.notifyArtistaUpdated(artista.getId(), artista.getNome());
        
        return response;
    }
    
    /**
     * Remove (inativa) um artista.
     *
     * @param id ID do artista
     * @throws EntityNotFoundException Se não encontrar
     */
    @Transactional
    public void deletar(Long id) {
        logger.info("Deletando artista ID: {}", id);

        Artista artista = artistaRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Artista não encontrado com ID: " + id));

        // Soft delete
        artista.setAtivo(false);
        artistaRepository.save(artista);
        
        notificationService.notifyArtistaDeleted(id);
        
        logger.info("Artista inativado: {}", id);
    }
    
    /**
     * Busca a entidade Artista por ID (uso interno).
     *
     * @param id ID do artista
     * @return Entidade Artista
     * @throws EntityNotFoundException Se não encontrar
     */
    @Transactional(readOnly = true)
    public Artista buscarEntidadePorId(Long id) {
        return artistaRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Artista não encontrado com ID: " + id));
    }
}