package com.projeto.backend.domain.album;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.projeto.backend.infrastructure.storage.StorageService;
import com.projeto.backend.web.dto.album.AlbumCapaResponse;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AlbumCapaService {

	private static final Logger logger = LoggerFactory.getLogger(AlbumCapaService.class);

	@Autowired
	private StorageService storageService;
	
	@Autowired
    private AlbumCapaRepository albumCapaRepository;
	
	@Autowired
    private AlbumRepository albumRepository;
	
	/**
     * Faz upload de uma capa para um álbum.
     *
     * @param albumId ID do álbum
     * @param file Arquivo de imagem
     * @param tipoCapa Tipo da capa
     * @return AlbumCapaResponse com dados da capa
     */
    @Transactional
    public AlbumCapaResponse upload(Long albumId, MultipartFile file, TipoCapa tipoCapa) {
        logger.info("Iniciando upload de capa para álbum ID: {}, tipo: {}", albumId, tipoCapa);

        Album album = albumRepository.findByIdAndAtivoTrue(albumId)
    		.orElseThrow(() -> new EntityNotFoundException("Álbum não encontrado com ID: " + albumId));

        // Define a pasta no MinIO: artista-{id}/album-{id}
        String folder = String.format("artista-%d/album-%d", album.getArtista().getId(), albumId);

        StorageService.StorageResult result = storageService.upload(file, folder);

        // Calcula a próxima ordem
        long countCapas = albumCapaRepository.countByAlbumId(albumId);
        int ordem = (int) countCapas;

        // Salva os metadados no banco
        AlbumCapa capa = new AlbumCapa();
        capa.setAlbum(album);
        capa.setObjectKey(result.objectKey());
        capa.setNomeArquivo(result.originalFilename());
        capa.setContentType(result.contentType());
        capa.setTamanhoBytes(result.size());
        capa.setTipoCapa(tipoCapa);
        capa.setOrdem(ordem);
        capa.setHashMd5(result.md5Hash());

        capa = albumCapaRepository.save(capa);
        logger.info("Capa salva com ID: {}", capa.getId());

        // Gera URL pré-assinada
        String presignedUrl = storageService.getPresignedUrl(capa.getObjectKey());

        return AlbumCapaResponse.fromEntityWithUrl(capa, presignedUrl);
    }
    
    /**
     * Faz upload de múltiplas capas para um álbum.
     *
     * @param albumId ID do álbum
     * @param files Lista de arquivos
     * @param tipoCapa Tipo das capas
     * @return Lista de AlbumCapaResponse
     */
    @Transactional
    public List<AlbumCapaResponse> uploadMultiple(Long albumId, List<MultipartFile> files, TipoCapa tipoCapa) {
        logger.info("Iniciando upload de {} capas para álbum ID: {}", files.size(), albumId);

        return files.stream()
                .map(file -> upload(albumId, file, tipoCapa))
                .collect(Collectors.toList());
    }
}
