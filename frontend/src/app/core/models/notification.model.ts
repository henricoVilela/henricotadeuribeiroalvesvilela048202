/**
 * Tipos de notificação do WebSocket (espelha o backend)
 */
export enum NotificationType {
  ARTISTA_CREATED = 'ARTISTA_CREATED',
  ARTISTA_UPDATED = 'ARTISTA_UPDATED',
  ARTISTA_DELETED = 'ARTISTA_DELETED',
  ALBUM_CREATED = 'ALBUM_CREATED',
  ALBUM_UPDATED = 'ALBUM_UPDATED',
  ALBUM_DELETED = 'ALBUM_DELETED',
  SYNC_STARTED = 'SYNC_STARTED',
  SYNC_COMPLETED = 'SYNC_COMPLETED',
  SYNC_ERROR = 'SYNC_ERROR',
  SYSTEM = 'SYSTEM'
}

export interface ArtistaPayload {
  id: number;
  nome: string | null;
}

export interface AlbumPayload {
  id: number;
  nome: string | null;
  artistaNome: string | null;
}

export interface SyncPayload {
  total: number;
  novos: number;
  atualizados: number;
}

export interface NotificationMessage {
  type: NotificationType;
  message: string;
  payload: ArtistaPayload | AlbumPayload | SyncPayload | null;
  timestamp: string;
}

export enum WebSocketTopic {
  ARTISTAS = '/topic/artistas',
  ALBUNS = '/topic/albuns',
  SYNC = '/topic/sync',
  SYSTEM = '/topic/system'
}
