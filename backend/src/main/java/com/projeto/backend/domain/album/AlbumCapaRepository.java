package com.projeto.backend.domain.album;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumCapaRepository extends JpaRepository<AlbumCapa, Long> {
	/**
     * Conta capas de um álbum.
     *
     * @param albumId ID do álbum
     * @return Quantidade de capas
     */
    long countByAlbumId(Long albumId);
}
