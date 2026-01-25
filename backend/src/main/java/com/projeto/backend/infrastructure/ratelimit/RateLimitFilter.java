package com.projeto.backend.infrastructure.ratelimit;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

	@Autowired
    private RateLimitService rateLimitService;
	
    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String[] EXCLUDED_PATHS = {
        "/api/v1/auth/",
        "/api/v1/health",
        "/swagger-ui",
        "/v3/api-docs",
        "/ws"
    };

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (isExcluded(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Obtém o identificador (username ou IP)
        String identifier = getIdentifier(request);

        // Obtém informações do rate limit
        RateLimitService.RateLimitInfo info = rateLimitService.getRateLimitInfo(identifier);

        // Adiciona headers de rate limit
        response.addHeader("X-RateLimit-Limit", String.valueOf(info.limit()));
        response.addHeader("X-RateLimit-Remaining", String.valueOf(info.remaining()));
        response.addHeader("X-RateLimit-Reset", String.valueOf(info.resetInSeconds()));

        // Tenta consumir um token
        if (rateLimitService.tryConsume(identifier)) {
            // Atualiza o header remaining após consumo
            response.setHeader("X-RateLimit-Remaining", 
                    String.valueOf(Math.max(0, info.remaining() - 1)));
            filterChain.doFilter(request, response);
        } else {
            // Rate limit excedido
            sendRateLimitExceededResponse(response, info);
        }
    }

    /**
     * Verifica se o path está na lista de exclusão.
     */
    private boolean isExcluded(String path) {
        for (String excluded : EXCLUDED_PATHS) {
            if (path.startsWith(excluded)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtém o identificador para rate limiting.
     * Usa username se autenticado, senão usa IP.
     */
    private String getIdentifier(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "user:" + authentication.getName();
        }

        // Fallback para IP
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return "ip:" + ip;
    }

    /**
     * Envia resposta de rate limit excedido (429).
     */
    private void sendRateLimitExceededResponse(
            HttpServletResponse response,
            RateLimitService.RateLimitInfo info
    ) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.addHeader("Retry-After", String.valueOf(info.resetInSeconds()));

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        errorResponse.put("error", "Too Many Requests");
        errorResponse.put("message", "Limite de requisições excedido. Tente novamente em " + info.resetInSeconds() + " segundos.");
        errorResponse.put("limit", info.limit());
        errorResponse.put("remaining", 0);
        errorResponse.put("retryAfter", info.resetInSeconds());

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
