import { Component, inject, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ToastComponent } from './shared/components/toast/toast.component';
import { takeUntil } from 'rxjs/internal/operators/takeUntil';
import { Subject } from 'rxjs';
import { AuthService } from './core/services/auth.service';
import { WebSocketService } from './core/services/websocket.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, ToastComponent],
  template: `
    <router-outlet />
    <app-toast />
  `
})
export class App implements OnInit {

  private webSocketService = inject(WebSocketService);
  private authService = inject(AuthService);
  private destroy$ = new Subject<void>();

  ngOnInit(): void {
    // Conecta ao WebSocket quando o usuário está logado
    this.authService.isAuthenticated$
      .pipe(takeUntil(this.destroy$))
      .subscribe(isAuthenticated => {
        if (isAuthenticated) {
          this.webSocketService.connect();
        } else {
          this.webSocketService.disconnect();
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}