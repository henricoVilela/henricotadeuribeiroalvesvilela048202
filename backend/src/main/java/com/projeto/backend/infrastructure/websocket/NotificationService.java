package com.projeto.backend.infrastructure.websocket;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
	
	private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
	
    public void notifyArtistaCreated(Long artistaId, String nome) {
        NotificationMessage message = new NotificationMessage(
                NotificationType.ARTISTA_CREATED,
                "Novo artista cadastrado: " + nome,
                new ArtistaPayload(artistaId, nome),
                LocalDateTime.now()
        );
        send("/topic/artistas", message);
        logger.info("Notificação enviada: Artista criado - {}", nome);
    }

    public void notifyArtistaUpdated(Long artistaId, String nome) {
        NotificationMessage message = new NotificationMessage(
                NotificationType.ARTISTA_UPDATED,
                "Artista atualizado: " + nome,
                new ArtistaPayload(artistaId, nome),
                LocalDateTime.now()
        );
        send("/topic/artistas", message);
        logger.info("Notificação enviada: Artista atualizado - {}", nome);
    }

    public void notifyArtistaDeleted(Long artistaId) {
        NotificationMessage message = new NotificationMessage(
                NotificationType.ARTISTA_DELETED,
                "Artista removido",
                new ArtistaPayload(artistaId, null),
                LocalDateTime.now()
        );
        send("/topic/artistas", message);
        logger.info("Notificação enviada: Artista removido - ID {}", artistaId);
    }
    
    /**
     * Envia notificação de álbum criado.
     */
    public void notifyAlbumCreated(Long albumId, String nome, String artistaNome) {
        NotificationMessage message = new NotificationMessage(
                NotificationType.ALBUM_CREATED,
                "Novo álbum cadastrado: " + nome + " - " + artistaNome,
                new AlbumPayload(albumId, nome, artistaNome),
                LocalDateTime.now()
        );
        send("/topic/albuns", message);
        logger.info("Notificação enviada: Álbum criado - {}", nome);
    }

    public void notifyAlbumUpdated(Long albumId, String nome, String artistaNome) {
        NotificationMessage message = new NotificationMessage(
                NotificationType.ALBUM_UPDATED,
                "Álbum atualizado: " + nome,
                new AlbumPayload(albumId, nome, artistaNome),
                LocalDateTime.now()
        );
        send("/topic/albuns", message);
        logger.info("Notificação enviada: Álbum atualizado - {}", nome);
    }

    public void notifyAlbumDeleted(Long albumId) {
        NotificationMessage message = new NotificationMessage(
                NotificationType.ALBUM_DELETED,
                "Álbum removido",
                new AlbumPayload(albumId, null, null),
                LocalDateTime.now()
        );
        send("/topic/albuns", message);
        logger.info("Notificação enviada: Álbum removido - ID {}", albumId);
    }
    
    private void send(String destination, NotificationMessage message) {
        try {
            messagingTemplate.convertAndSend(destination, message);
        } catch (Exception e) {
            logger.error("Erro ao enviar notificação para {}: {}", destination, e.getMessage());
        }
    }
    
    public enum NotificationType {
        ARTISTA_CREATED,
        ARTISTA_UPDATED,
        ARTISTA_DELETED,
        ALBUM_CREATED,
        ALBUM_UPDATED,
        ALBUM_DELETED,
    }

    public record NotificationMessage(
            NotificationType type,
            String message,
            Object payload,
            LocalDateTime timestamp
    ) {}
    
    public record ArtistaPayload(Long id, String nome) {}
    
    public record AlbumPayload(Long id, String nome, String artistaNome) {}
}
