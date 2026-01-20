package com.projeto.backend.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.projeto.backend.domain.album.AlbumCapaService;
import com.projeto.backend.domain.album.TipoCapa;
import com.projeto.backend.web.dto.album.AlbumCapaResponse;

@RestController
@RequestMapping("/api/v1")
public class AlbumCapaController {
	
	private static final Logger logger = LoggerFactory.getLogger(AlbumCapaController.class);

	@Autowired
    private AlbumCapaService albumCapaService;
	
	/**
     * Faz upload de uma capa para um álbum.
     */
    @PostMapping(value = "/albuns/{albumId}/capas", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AlbumCapaResponse> upload(
		@PathVariable Long albumId,
        @RequestParam("file") MultipartFile file,
        @RequestParam(defaultValue = "FRENTE") TipoCapa tipoCapa
    ) {
        logger.info("POST /api/v1/albuns/{}/capas - tipo: {}", albumId, tipoCapa);

        AlbumCapaResponse response = albumCapaService.upload(albumId, file, tipoCapa);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
