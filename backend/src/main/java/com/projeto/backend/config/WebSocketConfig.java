package com.projeto.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuração do WebSocket com STOMP.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configura o message broker.
     * 
     * - /topic: Para broadcasts (todos os clientes)
     * - /queue: Para mensagens privadas (usuário específico)
     * - /app: Prefixo para mensagens enviadas pelos clientes
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Registra os endpoints STOMP.
     * 
     * Clientes se conectam em /ws com suporte a SockJS para fallback.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // Endpoint sem SockJS (para clientes que suportam WebSocket nativo)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
}
