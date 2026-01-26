import { Component, inject, input, OnInit, signal } from '@angular/core';
import { Layout } from '../../../shared/components/layout/layout.component';
import { Loading } from '../../../shared/components/loaging/loading.component';
import { ConfirmDialog } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { Router, RouterLink } from '@angular/router';
import { ArtistaService } from '../../../core/services/artista.service';
import { ToastService } from '../../../core/services/toast.service';
import { Artista } from '../../../core/models/artista.model';
import { Album } from '../../../core/models/album.model';

@Component({
  selector: 'app-artista-detail',
  imports: [
    Layout,
    Loading,
    ConfirmDialog,
    RouterLink,
  ],
  templateUrl: './artista-detail.html',
})
export class ArtistaDetail implements OnInit {
  private artistaService = inject(ArtistaService);
  private toastService = inject(ToastService);
  private router = inject(Router);

  id = input.required<string>();

  loading = signal(true);
  loadingAlbuns = signal(true);
  artista = signal<Artista | null>(null);
  albuns = signal<Album[]>([]);
  showDeleteDialog = signal(false);

  ngOnInit(): void {
    this.loadArtista();
  }

  private loadArtista(): void {
    const artistaId = Number(this.id());

    this.artistaService.buscarPorId(artistaId).subscribe({
      next: (artista) => {
        this.artista.set(artista);
        this.loading.set(false);
        this.loadAlbuns(artistaId);
      },
      error: () => {
        this.loading.set(false);
        this.router.navigate(['/artistas']);
      }
    });
  }

  private loadAlbuns(artistaId: number): void {
    this.artistaService.buscarAlbuns(artistaId).subscribe({
      next: (albuns) => {
        this.albuns.set(albuns);
        this.loadingAlbuns.set(false);
      },
      error: () => {
        this.loadingAlbuns.set(false);
      }
    });
  }

  deleteArtista(): void {
    const artista = this.artista();
    if (!artista) return;

    this.artistaService.deletar(artista.id).subscribe({
      next: () => {
        this.toastService.success('Artista excluído com sucesso!');
        this.router.navigate(['/artistas']);
      },
      error: () => {
        this.showDeleteDialog.set(false);
      }
    });
  }
}
