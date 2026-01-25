package com.projeto.backend.infrastructure.ratelimit;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

@Service
public class RateLimitService {

	private static final Logger logger = LoggerFactory.getLogger(RateLimitService.class);
	
    /**
     * Cache de buckets por identificador (usuário ou IP).
     */
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    /**
     * Configuração padrão: 10 requisições por minuto.
     */
    private static final int REQUESTS_PER_MINUTE = 10;
    
    public record RateLimitInfo(
        long limit,
        long remaining,
        long resetInSeconds
    ) {}

    /**
     * Obtém ou cria um bucket para o identificador.
     *
     * @param identifier Identificador único (username ou IP)
     * @return Bucket configurado
     */
    public Bucket resolveBucket(String identifier) {
        return bucketCache.computeIfAbsent(identifier, this::createNewBucket);
    }

    /**
     * Verifica se uma requisição pode ser processada.
     *
     * @param identifier Identificador único
     * @return true se permitido, false se limite excedido
     */
    public boolean tryConsume(String identifier) {
        Bucket bucket = resolveBucket(identifier);
        return bucket.tryConsume(1);
    }

    /**
     * Retorna informações sobre o rate limit atual.
     *
     * @param identifier Identificador único
     * @return RateLimitInfo com tokens disponíveis e tempo para reset
     */
    public RateLimitInfo getRateLimitInfo(String identifier) {
        Bucket bucket = resolveBucket(identifier);
        long availableTokens = bucket.getAvailableTokens();
        
        logger.info("[Rate Limit] identificador {}. tokens disponiveis {}", identifier, availableTokens);
        
        return new RateLimitInfo(
                REQUESTS_PER_MINUTE,
                availableTokens,
                60 // segundos até reset completo
        );
    }

    /**
     * Cria um novo bucket com a configuração padrão.
     *
     * @param identifier Identificador (não usado, mas necessário para computeIfAbsent)
     * @return Novo Bucket configurado
     */
    private Bucket createNewBucket(String identifier) {
    	
        // Configuração: 10 tokens, repostos gradualmente ao longo de 1 minuto
    	Bandwidth limit = Bandwidth.builder()
	        .capacity(REQUESTS_PER_MINUTE)
	        .refillGreedy(REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
	        .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Remove um bucket do cache (útil para testes ou reset manual).
     *
     * @param identifier Identificador único
     */
    public void removeBucket(String identifier) {
        bucketCache.remove(identifier);
    }

    /**
     * Limpa todo o cache de buckets.
     */
    public void clearAllBuckets() {
        bucketCache.clear();
    }
}