package com.projeto.backend.domain.artista;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

}