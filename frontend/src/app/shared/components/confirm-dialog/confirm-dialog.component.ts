import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  template: `
    @if (isOpen) {
      <div class="fixed inset-0 z-50 overflow-y-auto">
        <div class="flex min-h-full items-center justify-center p-4">
          <!-- Backdrop -->
          <div 
            class="fixed inset-0 bg-black/50 transition-opacity"
            (click)="onCancel()"
          ></div>
          
          <!-- Dialog -->
          <div class="relative bg-white rounded-xl shadow-xl max-w-md w-full p-6 transform transition-all">
            <h3 class="text-lg font-semibold text-gray-900 mb-2">
              {{ title }}
            </h3>
            <p class="text-gray-600 mb-6">
              {{ message }}
            </p>
            <div class="flex justify-end gap-3">
              <button 
                class="btn btn-secondary"
                (click)="onCancel()"
              >
                {{ cancelText }}
              </button>
              <button 
                class="btn"
                [class.btn-danger]="type === 'danger'"
                [class.btn-primary]="type === 'primary'"
                (click)="onConfirm()"
              >
                {{ confirmText }}
              </button>
            </div>
          </div>
        </div>
      </div>
    }
  `
})
export class ConfirmDialogComponent {
  @Input() isOpen = false;
  @Input() title = 'Confirmar';
  @Input() message = 'Tem certeza que deseja continuar?';
  @Input() confirmText = 'Confirmar';
  @Input() cancelText = 'Cancelar';
  @Input() type: 'primary' | 'danger' = 'primary';

  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  onConfirm(): void {
    this.confirm.emit();
  }

  onCancel(): void {
    this.cancel.emit();
  }
}
