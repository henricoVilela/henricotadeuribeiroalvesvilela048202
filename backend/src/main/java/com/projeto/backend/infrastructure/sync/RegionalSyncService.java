package com.projeto.backend.infrastructure.sync;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.projeto.backend.domain.regional.Regional;
import com.projeto.backend.domain.regional.RegionalRepository;
import com.projeto.backend.infrastructure.websocket.NotificationService;

/**
 * Serviço para sincronização de dados com API externa.
 * 
 * Sincroniza regionais de: https://www.frfrj.sp.gov.br/api/regionais
 * 
 * Funcionalidades:
 * - Sincronização manual via endpoint
 * - Sincronização agendada (configurável)
 * - Notificações via WebSocket sobre o progresso
 */
@Service
public class RegionalSyncService {
	
	private static final Logger logger = LoggerFactory.getLogger(RegionalSyncService.class);
	
	@Autowired
	private RegionalRepository regionalRepository;
	
	@Autowired
    private NotificationService notificationService;

	@Autowired
	private WebClient webClient;
	
	@Value("${sync.regional.url:https://integrador-argus-api.geia.vip/v1/regionais}")
    private String apiUrl;

    @Value("${sync.regional.timeout:30}")
    private int timeoutSeconds;
    
    private LocalDateTime lastSyncTime;
    private boolean syncInProgress = false;
    
	
	/**
     * Executa sincronização com a API externa.
     * 
     * @return Resultado da sincronização
     */
    @Transactional
    public SyncResult sincronizar() {
        if (syncInProgress) {
            logger.warn("Sincronização já em andamento");
            return new SyncResult(false, 0, 0, 0, "Sincronização já em andamento");
        }

        syncInProgress = true;
        notificationService.notifySyncStarted();

        try {
            logger.info("Iniciando sincronização de regionais de: {}", apiUrl);

            // Busca dados da API externa
            List<RegionalExternalDto> externals = fetchFromApi();

            if (externals == null || externals.isEmpty()) {
                logger.warn("Nenhum dado retornado da API externa");
                notificationService.notifySyncError("Nenhum dado retornado da API");
                return new SyncResult(false, 0, 0, 0, "Nenhum dado retornado da API");
            }

            int novos = 0;
            int atualizados = 0;

            for (RegionalExternalDto external : externals) {
                Optional<Regional> existente = regionalRepository.findByExternalId(external.id());

                if (existente.isPresent()) {
                    Regional regional = existente.get();
                    if (!regional.getNome().equals(external.nome())) {
                        regional.setNome(external.nome());
                        regionalRepository.save(regional);
                        atualizados++;
                    }
                } else {
                    Regional novaRegional = new Regional(external.nome(), external.id());
                    regionalRepository.save(novaRegional);
                    novos++;
                }
            }

            int total = novos + atualizados;
            lastSyncTime = LocalDateTime.now();

            logger.info("Sincronização concluída: {} total ({} novos, {} atualizados)", total, novos, atualizados);
            notificationService.notifySyncCompleted(total, novos, atualizados);

            return new SyncResult(true, total, novos, atualizados, "Sincronização concluída com sucesso");

        } catch (WebClientResponseException e) {
            String error = "Erro ao acessar API externa: " + e.getStatusCode();
            logger.error(error, e);
            notificationService.notifySyncError(error);
            return new SyncResult(false, 0, 0, 0, error);

        } catch (Exception e) {
            String error = "Erro durante sincronização: " + e.getMessage();
            logger.error(error, e);
            notificationService.notifySyncError(error);
            return new SyncResult(false, 0, 0, 0, error);

        } finally {
            syncInProgress = false;
        }
    }
    
    private List<RegionalExternalDto> fetchFromApi() {
        try {
            return webClient.get()
                    .uri(apiUrl)
                    .retrieve()
                    .bodyToFlux(RegionalExternalDto.class)
                    .collectList()
                    .block(Duration.ofSeconds(timeoutSeconds));
        } catch (Exception e) {
            logger.error("Erro ao buscar dados da API: {}", e.getMessage());
            throw e;
        }
    }

	/**
     * Verifica se a sincronização está em andamento.
     */
    public boolean isSyncInProgress() {
        return syncInProgress;
    }
    
    /**
     * Retorna status da última sincronização.
     */
    public SyncStatus getStatus() {
        long totalRegionais = regionalRepository.countByAtivoTrue();
        return new SyncStatus(
                lastSyncTime,
                syncInProgress,
                totalRegionais,
                apiUrl
        );
    }
    
    @Async
    public void sincronizarAsync() {
        sincronizar();
    }
    
    public record RegionalExternalDto(
        Long id,
        String nome
    ) {}
    
    public record SyncResult(
        boolean success,
        int total,
        int novos,
        int atualizados,
        String message
    ) {}
    
    public record SyncStatus(
            LocalDateTime lastSync,
            boolean inProgress,
            long totalRegionais,
            String apiUrl
    ) {}
}
