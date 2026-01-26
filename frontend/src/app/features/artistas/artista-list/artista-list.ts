import { Component, inject, OnInit, signal } from '@angular/core';
import { Layout } from '../../../shared/components/layout/layout.component';
import { Loading } from '../../../shared/components/loaging/loading.component';
import { Pagination } from '../../../shared/components/pagination/pagination.component';
import { ConfirmDialog } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ToastService } from '../../../core/services/toast.service';
import { ArtistaService } from '../../../core/services/artista.service';
import { Artista } from '../../../core/models/artista.model';

@Component({
  selector: 'app-artista-list',
  imports: [
    Layout,
    Loading,
    Pagination,
    ConfirmDialog,
    RouterLink,
    FormsModule,
  ],
  templateUrl: './artista-list.html',
})
export class ArtistaList implements OnInit {
  private artistaService = inject(ArtistaService);
  private toastService = inject(ToastService);

  loading = signal(true);
  artistas = signal<Artista[]>([]);
  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
  pageSize = 10;
  searchTerm = '';

  showDeleteDialog = signal(false);
  artistaToDelete = signal<Artista | null>(null);

  ngOnInit(): void {
    this.loadArtistas();
  }

  loadArtistas(): void {
    this.loading.set(true);
    
    this.artistaService.listar(
      this.searchTerm || undefined,
      this.currentPage(),
      this.pageSize
    ).subscribe({
      next: (page) => {
        this.artistas.set(page.content);
        this.totalPages.set(page.totalPages);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  search(): void {
    this.currentPage.set(0);
    this.loadArtistas();
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.search();
  }

  onPageChange(page: number): void {
    this.currentPage.set(page);
    this.loadArtistas();
  }

  confirmDelete(artista: Artista): void {
    this.artistaToDelete.set(artista);
    this.showDeleteDialog.set(true);
  }

  deleteArtista(): void {
    const artista = this.artistaToDelete();
    if (!artista) return;

    this.artistaService.deletar(artista.id).subscribe({
      next: () => {
        this.toastService.success('Artista excluído com sucesso!');
        this.showDeleteDialog.set(false);
        this.artistaToDelete.set(null);
        this.loadArtistas();
      },
      error: () => {
        this.showDeleteDialog.set(false);
      }
    });
  }
}
