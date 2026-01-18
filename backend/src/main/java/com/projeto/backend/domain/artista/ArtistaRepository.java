package com.projeto.backend.domain.artista;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtistaRepository extends JpaRepository<Artista, Long> {
	/**
     * Busca artistas com contagem de álbuns.
     *
     * @param pageable Configuração de paginação
     * @return Página de artistas com total de álbuns
     */
    @Query("SELECT a, COUNT(al) as totalAlbuns FROM Artista a LEFT JOIN a.albuns al WHERE a.ativo = true GROUP BY a")
    Page<Object[]> findAllWithAlbumCount(Pageable pageable);

    /**
     * Busca artistas por nome com contagem de álbuns.
     *
     * @param nome Nome para busca
     * @param pageable Configuração de paginação
     * @return Página de artistas com total de álbuns
     */
    @Query("SELECT a, COUNT(al) as totalAlbuns FROM Artista a LEFT JOIN a.albuns al " +
           "WHERE a.ativo = true AND LOWER(a.nome) LIKE LOWER(CONCAT('%', :nome, '%')) GROUP BY a")
    Page<Object[]> findByNomeWithAlbumCount(@Param("nome") String nome, Pageable pageable);
    
    /**
     * Busca artista ativo por ID.
     *
     * @param id ID do artista
     * @return Optional com o artista
     */
    Optional<Artista> findByIdAndAtivoTrue(Long id);
}
