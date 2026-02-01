import { Injectable, OnDestroy, signal, computed } from '@angular/core';
import { Subject, Observable, timer } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { 
  NotificationMessage, 
  NotificationType, 
  WebSocketTopic,
} from '../models/notification.model';
import { ToastService } from './toast.service';

// Importação dinâmica do SockJS e STOMP
declare var SockJS: any;
declare var Stomp: any;

export type ConnectionStatus = 'disconnected' | 'connecting' | 'connected' | 'error';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService implements OnDestroy {
  
  private stompClient: any = null;
  private destroy$ = new Subject<void>();
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 3000;
  
  // Signals para estado reativo
  private _connectionStatus = signal<ConnectionStatus>('disconnected');
  private _isConnected = computed(() => this._connectionStatus() === 'connected');
  
  // Subjects para emitir notificações por tópico
  private artistasSubject = new Subject<NotificationMessage>();
  private albunsSubject = new Subject<NotificationMessage>();
  private syncSubject = new Subject<NotificationMessage>();
  private systemSubject = new Subject<NotificationMessage>();
  private allNotificationsSubject = new Subject<NotificationMessage>();

  // Observables públicos
  readonly connectionStatus = this._connectionStatus.asReadonly();
  readonly isConnected = this._isConnected;
  
  readonly artistas$ = this.artistasSubject.asObservable();
  readonly albuns$ = this.albunsSubject.asObservable();
  readonly sync$ = this.syncSubject.asObservable();
  readonly system$ = this.systemSubject.asObservable();
  readonly allNotifications$ = this.allNotificationsSubject.asObservable();

  constructor(private toastService: ToastService) {
    // Carrega os scripts necessários
    this.loadScripts();
  }

  /**
   * Carrega SockJS e STOMP dinamicamente
   */
  private loadScripts(): void {
    // Verifica se já estão carregados
    if (typeof SockJS !== 'undefined' && typeof Stomp !== 'undefined') {
      return;
    }

    // SockJS
    const sockScript = document.createElement('script');
    sockScript.src = 'https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js';
    sockScript.async = true;
    document.head.appendChild(sockScript);

    // STOMP
    const stompScript = document.createElement('script');
    stompScript.src = 'https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js';
    stompScript.async = true;
    document.head.appendChild(stompScript);
  }

  /**
   * Conecta ao WebSocket server
   */
  connect(): void {
    if (this._connectionStatus() === 'connecting' || this._connectionStatus() === 'connected') {
      console.log('[WebSocket] Já conectado ou conectando...');
      return;
    }

    // Aguarda scripts carregarem
    if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
      console.log('[WebSocket] Aguardando scripts carregarem...');
      setTimeout(() => this.connect(), 500);
      return;
    }

    this._connectionStatus.set('connecting');
    console.log('[WebSocket] Conectando a', environment.wsUrl);

    try {
      const socket = new SockJS(environment.wsUrl);
      this.stompClient = Stomp.over(socket);
      
      // Desabilita logs do STOMP em produção
      if (environment.production) {
        this.stompClient.debug = null;
      }

      this.stompClient.connect(
        {}, // headers
        () => this.onConnected(),
        (error: any) => this.onError(error)
      );
    } catch (error) {
      console.error('[WebSocket] Erro ao criar conexão:', error);
      this._connectionStatus.set('error');
      this.scheduleReconnect();
    }
  }

  /**
   * Callback quando conectado com sucesso
   */
  private onConnected(): void {
    console.log('[WebSocket] Conectado com sucesso!');
    this._connectionStatus.set('connected');
    this.reconnectAttempts = 0;

    // Inscreve em todos os tópicos
    this.subscribeToTopic(WebSocketTopic.ARTISTAS, this.artistasSubject);
    this.subscribeToTopic(WebSocketTopic.ALBUNS, this.albunsSubject);
    this.subscribeToTopic(WebSocketTopic.SYNC, this.syncSubject);
    this.subscribeToTopic(WebSocketTopic.SYSTEM, this.systemSubject);

    this.toastService.info('Notificações em tempo real ativadas', 2000);
  }

  /**
   * Inscreve em um tópico específico
   */
  private subscribeToTopic(topic: WebSocketTopic, subject: Subject<NotificationMessage>): void {
    if (!this.stompClient || !this.stompClient.connected) {
      return;
    }

    this.stompClient.subscribe(topic, (message: any) => {
      try {
        const notification: NotificationMessage = JSON.parse(message.body);
        console.log(`[WebSocket] Mensagem recebida em ${topic}:`, notification);
        
        // Emite para o subject específico
        subject.next(notification);
        
        // Emite para o subject geral
        this.allNotificationsSubject.next(notification);
        
        // Mostra toast baseado no tipo
        this.showNotificationToast(notification);
      } catch (error) {
        console.error('[WebSocket] Erro ao processar mensagem:', error);
      }
    });

    console.log(`[WebSocket] Inscrito em ${topic}`);
  }

  /**
   * Mostra toast baseado na notificação
   */
  private showNotificationToast(notification: NotificationMessage): void {
    switch (notification.type) {
      case NotificationType.ARTISTA_CREATED:
      case NotificationType.ALBUM_CREATED:
        this.toastService.success(notification.message);
        break;
        
      case NotificationType.ARTISTA_UPDATED:
      case NotificationType.ALBUM_UPDATED:
        this.toastService.info(notification.message);
        break;
        
      case NotificationType.ARTISTA_DELETED:
      case NotificationType.ALBUM_DELETED:
        this.toastService.warning(notification.message);
        break;
        
      case NotificationType.SYNC_STARTED:
        this.toastService.info(notification.message, 2000);
        break;
        
      case NotificationType.SYNC_COMPLETED:
        this.toastService.success(notification.message, 5000);
        break;
        
      case NotificationType.SYNC_ERROR:
        this.toastService.error(notification.message);
        break;
        
      case NotificationType.SYSTEM:
        this.toastService.info(notification.message);
        break;
    }
  }

  /**
   * Callback de erro na conexão
   */
  private onError(error: any): void {
    console.error('[WebSocket] Erro na conexão:', error);
    this._connectionStatus.set('error');
    this.scheduleReconnect();
  }

  /**
   * Agenda tentativa de reconexão
   */
  private scheduleReconnect(): void {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('[WebSocket] Máximo de tentativas de reconexão atingido');
      this.toastService.error('Não foi possível conectar às notificações em tempo real');
      return;
    }

    this.reconnectAttempts++;
    const delay = this.reconnectDelay * this.reconnectAttempts;
    
    console.log(`[WebSocket] Tentando reconectar em ${delay/1000}s (tentativa ${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
    
    timer(delay)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        if (this._connectionStatus() !== 'connected') {
          this.connect();
        }
      });
  }

  /**
   * Desconecta do WebSocket server
   */
  disconnect(): void {
    if (this.stompClient) {
      try {
        this.stompClient.disconnect(() => {
          console.log('[WebSocket] Desconectado');
        });
      } catch (error) {
        console.error('[WebSocket] Erro ao desconectar:', error);
      }
      this.stompClient = null;
    }
    this._connectionStatus.set('disconnected');
  }

  /**
   * Retorna observable filtrado por tipo de notificação
   */
  onNotificationType(...types: NotificationType[]): Observable<NotificationMessage> {
    return this.allNotifications$.pipe(
      filter(notification => types.includes(notification.type))
    );
  }

  /**
   * Observable para mudanças em artistas (create, update, delete)
   */
  onArtistaChange(): Observable<NotificationMessage> {
    return this.onNotificationType(
      NotificationType.ARTISTA_CREATED,
      NotificationType.ARTISTA_UPDATED,
      NotificationType.ARTISTA_DELETED
    );
  }

  /**
   * Observable para mudanças em álbuns (create, update, delete)
   */
  onAlbumChange(): Observable<NotificationMessage> {
    return this.onNotificationType(
      NotificationType.ALBUM_CREATED,
      NotificationType.ALBUM_UPDATED,
      NotificationType.ALBUM_DELETED
    );
  }

  /**
   * Observable para eventos de sincronização
   */
  onSyncEvent(): Observable<NotificationMessage> {
    return this.onNotificationType(
      NotificationType.SYNC_STARTED,
      NotificationType.SYNC_COMPLETED,
      NotificationType.SYNC_ERROR
    );
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.disconnect();
  }
}
