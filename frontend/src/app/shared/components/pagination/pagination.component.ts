import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-pagination',
  standalone: true,
  template: `
    <div class="flex items-center justify-between px-4 py-3 bg-white border-t border-gray-200 sm:px-6">
      <div class="flex-1 flex justify-between sm:hidden">
        <button
          class="btn btn-outline"
          [disabled]="currentPage === 0"
          (click)="onPageChange(currentPage - 1)"
        >
          Anterior
        </button>
        <button
          class="btn btn-outline"
          [disabled]="currentPage >= totalPages - 1"
          (click)="onPageChange(currentPage + 1)"
        >
          Próxima
        </button>
      </div>
      
      <div class="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
        <div>
          <p class="text-sm text-gray-700">
            Mostrando 
            <span class="font-medium">{{ startItem }}</span>
            a 
            <span class="font-medium">{{ endItem }}</span>
            de 
            <span class="font-medium">{{ totalElements }}</span>
            resultados
          </p>
        </div>
        <div>
          <nav class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px">
            <!-- Primeira página -->
            <button
              class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              [disabled]="currentPage === 0"
              (click)="onPageChange(0)"
            >
              <span class="sr-only">Primeira</span>
              ««
            </button>
            
            <!-- Página anterior -->
            <button
              class="relative inline-flex items-center px-2 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              [disabled]="currentPage === 0"
              (click)="onPageChange(currentPage - 1)"
            >
              <span class="sr-only">Anterior</span>
              «
            </button>
            
            <!-- Números de página -->
            @for (page of visiblePages; track page) {
              <button
                class="relative inline-flex items-center px-4 py-2 border text-sm font-medium"
                [class.bg-primary-50]="page === currentPage"
                [class.border-primary-500]="page === currentPage"
                [class.text-primary-600]="page === currentPage"
                [class.bg-white]="page !== currentPage"
                [class.border-gray-300]="page !== currentPage"
                [class.text-gray-700]="page !== currentPage"
                [class.hover:bg-gray-50]="page !== currentPage"
                (click)="onPageChange(page)"
              >
                {{ page + 1 }}
              </button>
            }
            
            <!-- Próxima página -->
            <button
              class="relative inline-flex items-center px-2 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              [disabled]="currentPage >= totalPages - 1"
              (click)="onPageChange(currentPage + 1)"
            >
              <span class="sr-only">Próxima</span>
              »
            </button>
            
            <!-- Última página -->
            <button
              class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              [disabled]="currentPage >= totalPages - 1"
              (click)="onPageChange(totalPages - 1)"
            >
              <span class="sr-only">Última</span>
              »»
            </button>
          </nav>
        </div>
      </div>
    </div>
  `
})
export class Pagination {
  @Input() currentPage = 0;
  @Input() totalPages = 0;
  @Input() totalElements = 0;
  @Input() pageSize = 10;
  
  @Output() pageChange = new EventEmitter<number>();

  get startItem(): number {
    return this.totalElements === 0 ? 0 : this.currentPage * this.pageSize + 1;
  }

  get endItem(): number {
    const end = (this.currentPage + 1) * this.pageSize;
    return Math.min(end, this.totalElements);
  }

  get visiblePages(): number[] {
    const pages: number[] = [];
    const maxVisible = 5;
    let start = Math.max(0, this.currentPage - Math.floor(maxVisible / 2));
    let end = Math.min(this.totalPages, start + maxVisible);

    if (end - start < maxVisible) {
      start = Math.max(0, end - maxVisible);
    }

    for (let i = start; i < end; i++) {
      pages.push(i);
    }

    return pages;
  }

  onPageChange(page: number): void {
    if (page >= 0 && page < this.totalPages && page !== this.currentPage) {
      this.pageChange.emit(page);
    }
  }
}
