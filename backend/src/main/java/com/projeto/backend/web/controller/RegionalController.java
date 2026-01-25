package com.projeto.backend.web.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.backend.domain.regional.RegionalService;
import com.projeto.backend.infrastructure.sync.RegionalSyncService;
import com.projeto.backend.web.dto.regional.RegionalResponse;
import com.projeto.backend.web.openapi.RegionalControllerOpenApi;

@RestController
@RequestMapping("/api/v1/regionais")
public class RegionalController implements RegionalControllerOpenApi {
	
	private static final Logger logger = LoggerFactory.getLogger(RegionalController.class);
	
	@Autowired
	private RegionalService regionalService;
	
	@Autowired
	private RegionalSyncService regionalSyncService;
	
    @GetMapping
    public ResponseEntity<Page<RegionalResponse>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nome") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        logger.info("GET /api/v1/regionais - nome: {}, page: {}, size: {}", nome, page, size);

        Page<RegionalResponse> regionais = regionalService.listar(nome, page, size, sortBy, sortDir);
        return ResponseEntity.ok(regionais);
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> sincronizar() {
        logger.info("POST /api/v1/regionais/sync");

        if (regionalSyncService.isSyncInProgress()) {
            return ResponseEntity.status(409).body(Map.of(
                    "success", false,
                    "message", "Sincronização já em andamento"
            ));
        }

        RegionalSyncService.SyncResult result = regionalSyncService.sincronizar();

        return ResponseEntity.ok(Map.of(
                "success", result.success(),
                "total", result.total(),
                "novos", result.novos(),
                "atualizados", result.atualizados(),
                "message", result.message()
        ));
    }

    @GetMapping("/todas")
    public ResponseEntity<List<RegionalResponse>> listarTodas() {
        logger.info("GET /api/v1/regionais/todas");

        List<RegionalResponse> regionais = regionalService.listarTodas();
        return ResponseEntity.ok(regionais);
    }
    
    @PostMapping("/sync/async")
    public ResponseEntity<Map<String, Object>> sincronizarAsync() {
        logger.info("POST /api/v1/regionais/sync/async");

        if (regionalSyncService.isSyncInProgress()) {
            return ResponseEntity.status(409).body(Map.of(
                    "success", false,
                    "message", "Sincronização já em andamento"
            ));
        }

        regionalSyncService.sincronizarAsync();

        return ResponseEntity.accepted().body(Map.of(
                "success", true,
                "message", "Sincronização iniciada em background. Acompanhe via WebSocket em /topic/sync"
        ));
    }

    @GetMapping("/sync/status")
    public ResponseEntity<RegionalSyncService.SyncStatus> getStatus() {
        logger.info("GET /api/v1/regionais/sync/status");

        RegionalSyncService.SyncStatus status = regionalSyncService.getStatus();
        return ResponseEntity.ok(status);
    }
}
