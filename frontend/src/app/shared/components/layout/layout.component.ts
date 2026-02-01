import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { WsIndicator } from '../ws-indicator/ws-indicator.component';


@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, WsIndicator],
  template: `
    <div class="min-h-screen bg-gray-50">
      <!-- Sidebar -->
      <aside 
        class="fixed inset-y-0 left-0 z-40 w-64 bg-white border-r border-gray-200 transform transition-transform duration-300 ease-in-out lg:translate-x-0"
        [class.-translate-x-full]="!sidebarOpen()"
        [class.translate-x-0]="sidebarOpen()"
      >
        <!-- Logo -->
        <div class="flex items-center h-16 px-6 border-b border-gray-200">
          <h1 class="text-xl font-bold text-primary-600">🎵 Artistas</h1>
        </div>
        
        <!-- Navigation -->
        <nav class="p-4 space-y-1">
          <a 
            routerLink="/dashboard" 
            routerLinkActive="bg-primary-50 text-primary-700"
            class="flex items-center px-4 py-3 text-gray-700 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <span class="mr-3">📊</span>
            Dashboard
          </a>
          <a 
            routerLink="/artistas" 
            routerLinkActive="bg-primary-50 text-primary-700"
            class="flex items-center px-4 py-3 text-gray-700 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <span class="mr-3">🎤</span>
            Artistas
          </a>
          <a 
            routerLink="/albuns" 
            routerLinkActive="bg-primary-50 text-primary-700"
            class="flex items-center px-4 py-3 text-gray-700 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <span class="mr-3">💿</span>
            Álbuns
          </a>
        </nav>
        
        <!-- User info -->
        <div class="absolute bottom-0 left-0 right-0 p-4 border-t border-gray-200">
          <div class="mb-3 pb-3 border-b border-gray-100">
            <app-ws-indicator />
          </div>
          <div class="flex items-center justify-between">
            <div class="flex items-center">
              <div class="w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center">
                <span class="text-primary-600 font-medium text-sm">
                  {{ authService.currentUser()?.charAt(0)?.toUpperCase() }}
                </span>
              </div>
              <span class="ml-3 text-sm font-medium text-gray-700">
                {{ authService.currentUser() }}
              </span>
            </div>
            <button 
              (click)="authService.logout()"
              class="text-red-500 hover:text-red-700 cursor-pointer"
              title="Sair"
            >
             Sair
            </button>
          </div>
        </div>
      </aside>
      
      <!-- Mobile header -->
      <div class="lg:hidden fixed top-0 left-0 right-0 z-30 h-16 bg-white border-b border-gray-200 flex items-center px-4">
        <button 
          (click)="toggleSidebar()"
          class="p-2 text-gray-600 hover:text-gray-900"
        >
          ☰
        </button>
        <h1 class="ml-4 text-lg font-bold text-primary-600">🎵 Artistas</h1>
        <div class="ml-auto">
          <app-ws-indicator />
        </div>
      </div>
      
      <!-- Backdrop for mobile -->
      @if (sidebarOpen()) {
        <div 
          class="fixed inset-0 z-30 bg-black/50 lg:hidden"
          (click)="closeSidebar()"
        ></div>
      }
      
      <!-- Main content -->
      <main class="lg:ml-64 min-h-screen pt-16 lg:pt-0">
        <div class="p-6">
          <ng-content></ng-content>
        </div>
      </main>
    </div>
  `
})
export class Layout {
  authService = inject(AuthService);
  sidebarOpen = signal(false);

  toggleSidebar(): void {
    this.sidebarOpen.update(v => !v);
  }

  closeSidebar(): void {
    this.sidebarOpen.set(false);
  }
}
