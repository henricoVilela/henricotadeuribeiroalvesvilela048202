package com.projeto.backend.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.backend.domain.artista.ArtistaService;
import com.projeto.backend.web.dto.artista.ArtistaResponse;
import com.projeto.backend.web.openapi.ArtistaControllerOpenApi;

@RestController
@RequestMapping("/api/v1/artistas")
public class ArtistaController implements ArtistaControllerOpenApi { 
	
	private static final Logger logger = LoggerFactory.getLogger(ArtistaController.class);
	
	@Autowired
	private ArtistaService artistaService;
	
    @GetMapping
    public ResponseEntity<Page<ArtistaResponse>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nome") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        logger.info("GET /api/v1/artistas - nome: {}, page: {}, size: {}", nome, page, size);

        Page<ArtistaResponse> artistas = artistaService.listar(nome, page, size, sortBy, sortDir);
        return ResponseEntity.ok(artistas);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ArtistaResponse> buscarPorId(@PathVariable Long id) {
        logger.info("GET /api/v1/artistas/{}", id);

        ArtistaResponse artista = artistaService.buscarPorId(id);
        return ResponseEntity.ok(artista);
    }
}
