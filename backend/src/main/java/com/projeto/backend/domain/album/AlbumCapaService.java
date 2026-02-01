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
        String presignedUrl = storageService.getPublicUrl(capa.getObjectKey());

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
    
    /**
     * Lista as capas de um álbum com URLs pré-assinadas.
     *
     * @param albumId ID do álbum
     * @return Lista de capas com URLs
     */
    @Transactional(readOnly = true)
    public List<AlbumCapaResponse> listarPorAlbum(Long albumId) {
        logger.info("Listando capas do álbum ID: {}", albumId);

        // Verifica se o álbum existe
        if (!albumRepository.existsById(albumId)) {
            throw new EntityNotFoundException("Álbum não encontrado com ID: " + albumId);
        }

        List<AlbumCapa> capas = albumCapaRepository.findByAlbumIdOrderByOrdemAsc(albumId);

        return capas.stream()
                .map(capa -> {
                    String presignedUrl = storageService.getPublicUrl(capa.getObjectKey());
                    return AlbumCapaResponse.fromEntityWithUrl(capa, presignedUrl);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Busca uma capa por ID com URL pré-assinada.
     *
     * @param capaId ID da capa
     * @return AlbumCapaResponse com URL
     */
    @Transactional(readOnly = true)
    public AlbumCapaResponse buscarPorId(Long capaId) {
        logger.info("Buscando capa ID: {}", capaId);

        AlbumCapa capa = albumCapaRepository.findById(capaId)
                .orElseThrow(() -> new EntityNotFoundException("Capa não encontrada com ID: " + capaId));

        String presignedUrl = storageService.getPublicUrl(capa.getObjectKey());
        return AlbumCapaResponse.fromEntityWithUrl(capa, presignedUrl);
    }
    
    /**
     * Atualiza o tipo de uma capa.
     *
     * @param capaId ID da capa
     * @param tipoCapa Novo tipo
     * @return AlbumCapaResponse atualizado
     */
    @Transactional
    public AlbumCapaResponse atualizarTipo(Long capaId, TipoCapa tipoCapa) {
        logger.info("Atualizando tipo da capa ID: {} para {}", capaId, tipoCapa);

        AlbumCapa capa = albumCapaRepository.findById(capaId)
                .orElseThrow(() -> new EntityNotFoundException("Capa não encontrada com ID: " + capaId));

        capa.setTipoCapa(tipoCapa);
        capa = albumCapaRepository.save(capa);

        String presignedUrl = storageService.getPublicUrl(capa.getObjectKey());
        return AlbumCapaResponse.fromEntityWithUrl(capa, presignedUrl);
    }

    /**
     * Atualiza a ordem de uma capa.
     *
     * @param capaId ID da capa
     * @param novaOrdem Nova ordem
     * @return AlbumCapaResponse atualizado
     */
    @Transactional
    public AlbumCapaResponse atualizarOrdem(Long capaId, Integer novaOrdem) {
        logger.info("Atualizando ordem da capa ID: {} para {}", capaId, novaOrdem);

        AlbumCapa capa = albumCapaRepository.findById(capaId)
                .orElseThrow(() -> new EntityNotFoundException("Capa não encontrada com ID: " + capaId));

        capa.setOrdem(novaOrdem);
        capa = albumCapaRepository.save(capa);

        String presignedUrl = storageService.getPublicUrl(capa.getObjectKey());
        return AlbumCapaResponse.fromEntityWithUrl(capa, presignedUrl);
    }
    
    /**
     * Remove uma capa do álbum e do MinIO.
     *
     * @param capaId ID da capa
     */
    @Transactional
    public void deletar(Long capaId) {
        logger.info("Removendo capa ID: {}", capaId);

        AlbumCapa capa = albumCapaRepository.findById(capaId)
                .orElseThrow(() -> new EntityNotFoundException("Capa não encontrada com ID: " + capaId));

        // Remove do MinIO
        storageService.delete(capa.getObjectKey());

        // Remove do banco
        albumCapaRepository.delete(capa);

        logger.info("Capa removida com sucesso: {}", capaId);
    }
    
    /**
     * Remove todas as capas de um álbum.
     *
     * @param albumId ID do álbum
     */
    @Transactional
    public void deletarTodasPorAlbum(Long albumId) {
        logger.info("Removendo todas as capas do álbum ID: {}", albumId);

        List<AlbumCapa> capas = albumCapaRepository.findByAlbumIdOrderByOrdemAsc(albumId);

        for (AlbumCapa capa : capas) {
            try {
                storageService.delete(capa.getObjectKey());
            } catch (Exception e) {
                logger.warn("Erro ao remover arquivo do MinIO: {}", capa.getObjectKey());
            }
        }

        albumCapaRepository.deleteByAlbumId(albumId);
        logger.info("Todas as capas removidas do álbum: {}", albumId);
    }
}
