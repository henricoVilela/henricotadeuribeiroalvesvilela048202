import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WebSocketService, ConnectionStatus } from '../../../core/services/websocket.service';

@Component({
  selector: 'app-ws-indicator',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div 
      class="flex items-center gap-2 text-xs"
      [title]="getStatusTitle()"
    >
      <span 
        class="w-2 h-2 rounded-full transition-colors duration-300"
        [class]="getStatusClass()"
      ></span>
      <span class="text-gray-500 hidden sm:inline">{{ getStatusText() }}</span>
    </div>
  `
})
export class WsIndicator {
  private wsService = inject(WebSocketService);

  get status(): ConnectionStatus {
    return this.wsService.connectionStatus();
  }

  getStatusClass(): string {
    switch (this.status) {
      case 'connected':
        return 'bg-green-500';
      case 'connecting':
        return 'bg-yellow-500 animate-pulse';
      case 'error':
        return 'bg-red-500';
      default:
        return 'bg-gray-400';
    }
  }

  getStatusText(): string {
    switch (this.status) {
      case 'connected':
        return 'Online';
      case 'connecting':
        return 'Conectando...';
      case 'error':
        return 'Offline';
      default:
        return 'Desconectado';
    }
  }

  getStatusTitle(): string {
    switch (this.status) {
      case 'connected':
        return 'Notificações em tempo real ativas';
      case 'connecting':
        return 'Conectando ao servidor de notificações...';
      case 'error':
        return 'Erro na conexão. Tentando reconectar...';
      default:
        return 'Notificações em tempo real desativadas';
    }
  }
}
