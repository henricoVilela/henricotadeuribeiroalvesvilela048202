import { Component, inject, OnInit, signal } from '@angular/core';
import { Layout } from '../../../shared/components/layout/layout.component';
import { Loading } from '../../../shared/components/loaging/loading.component';
import { Pagination } from '../../../shared/components/pagination/pagination.component';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AlbumService } from '../../../core/services/album.service';
import { ToastService } from '../../../core/services/toast.service';
import { Album } from '../../../core/models/album.model';
import { ConfirmDialog } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-album-list',
  imports: [
    Layout,
    Loading,
    Pagination,
    RouterLink,
    FormsModule,
    ConfirmDialog,
  ],
  templateUrl: './album-list.html',
})
export class AlbumList implements OnInit {

  private albumService = inject(AlbumService);
  private toastService = inject(ToastService);

  loading = signal(true);
  albuns = signal<Album[]>([]);
  generos = signal<string[]>([]);
  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
  pageSize = 10;

  searchNome = '';
  searchArtista = '';
  searchGenero = '';

  showDeleteDialog = signal(false);
  albumToDelete = signal<Album | null>(null);

  ngOnInit(): void {
    this.loadGeneros();
    this.loadAlbuns();
  }

  private loadGeneros(): void {
    this.albumService.listarGeneros().subscribe({
      next: (generos) => this.generos.set(generos)
    });
  }

  loadAlbuns(): void {
    this.loading.set(true);
    
    this.albumService.listar(
      this.searchNome || undefined,
      this.searchArtista || undefined,
      this.searchGenero || undefined,
      this.currentPage(),
      this.pageSize
    ).subscribe({
      next: (page) => {
        this.albuns.set(page.content);
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
    this.loadAlbuns();
  }

  clearSearch(): void {
    this.searchNome = '';
    this.searchArtista = '';
    this.searchGenero = '';
    this.search();
  }

  onPageChange(page: number): void {
    this.currentPage.set(page);
    this.loadAlbuns();
  }

  confirmDelete(album: Album): void {
    this.albumToDelete.set(album);
    this.showDeleteDialog.set(true);
  }

  deleteAlbum(): void {
    const album = this.albumToDelete();
    if (!album) return;

    this.albumService.deletar(album.id).subscribe({
      next: () => {
        this.toastService.success('Álbum excluído com sucesso!');
        this.showDeleteDialog.set(false);
        this.albumToDelete.set(null);
        this.loadAlbuns();
      },
      error: () => {
        this.showDeleteDialog.set(false);
      }
    });
  }
}
