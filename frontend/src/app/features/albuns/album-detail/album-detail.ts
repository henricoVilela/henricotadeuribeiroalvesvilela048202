import { Component, inject, input, OnInit, signal } from '@angular/core';
import { AlbumService } from '../../../core/services/album.service';
import { ToastService } from '../../../core/services/toast.service';
import { Router, RouterLink } from '@angular/router';
import { Album, AlbumCapa, TipoCapa } from '../../../core/models/album.model';
import { Layout } from '../../../shared/components/layout/layout.component';
import { Loading } from '../../../shared/components/loaging/loading.component';
import { ConfirmDialog } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { FormsModule } from '@angular/forms';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-album-detail',
  imports: [
    Layout,
    Loading,
    ConfirmDialog,
    RouterLink,
    FormsModule,
  ],
  templateUrl: './album-detail.html',
})
export class AlbumDetail implements OnInit {
  private albumService = inject(AlbumService);
  private toastService = inject(ToastService);
  private router = inject(Router);

  id = input.required<string>();

  loading = signal(true);
  loadingCapas = signal(true);
  uploading = signal(false);
  album = signal<Album | null>(null);
  capas = signal<AlbumCapa[]>([]);
  
  showDeleteDialog = signal(false);
  showDeleteCapaDialog = signal(false);
  capaToDelete = signal<AlbumCapa | null>(null);
  
  selectedTipoCapa: TipoCapa = 'FRENTE';
  
  tiposCapa: { value: TipoCapa; label: string }[] = [
    { value: 'FRENTE', label: 'Capa Frontal' },
    { value: 'VERSO', label: 'Capa Traseira' },
    { value: 'ENCARTE', label: 'Encarte' },
    { value: 'DISCO', label: 'Disco' },
    { value: 'PROMOCIONAL', label: 'Promocional' },
    { value: 'OUTRO', label: 'Outro' }
  ];

  isProduction = environment.production;

  ngOnInit(): void {
    this.loadAlbum();
  }

  private loadAlbum(): void {
    const albumId = Number(this.id());
    
    this.albumService.buscarPorId(albumId).subscribe({
      next: (album) => {
        this.album.set(album);
        this.loading.set(false);
        this.loadCapas(albumId);
      },
      error: () => {
        this.loading.set(false);
        this.router.navigate(['/albuns']);
      }
    });
  }

  private loadCapas(albumId: number): void {
    this.albumService.listarCapas(albumId).subscribe({
      next: (capas) => {
        this.capas.set(capas);
        this.loadingCapas.set(false);
      },
      error: () => {
        this.loadingCapas.set(false);
      }
    });
  }

  formatDuration(seconds: number | null): string {
    if (!seconds) return '-';
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  }

  getTipoCapaLabel(tipo: TipoCapa): string {
    return this.tiposCapa.find(t => t.value === tipo)?.label || tipo;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.uploadCapa(input.files[0]);
      input.value = '';
    }
  }

  private uploadCapa(file: File): void {
    const album = this.album();
    if (!album) return;

    this.uploading.set(true);
    
    this.albumService.uploadCapa(album.id, file, this.selectedTipoCapa).subscribe({
      next: (capa) => {
        this.capas.update(capas => [...capas, capa]);
        this.toastService.success('Capa enviada com sucesso!');
        this.uploading.set(false);
      },
      error: () => {
        this.uploading.set(false);
      }
    });
  }

  confirmDeleteCapa(capa: AlbumCapa): void {
    this.capaToDelete.set(capa);
    this.showDeleteCapaDialog.set(true);
  }

  deleteCapa(): void {
    const capa = this.capaToDelete();
    if (!capa) return;

    this.albumService.deletarCapa(capa.id).subscribe({
      next: () => {
        this.capas.update(capas => capas.filter(c => c.id !== capa.id));
        this.toastService.success('Capa removida com sucesso!');
        this.showDeleteCapaDialog.set(false);
        this.capaToDelete.set(null);
      },
      error: () => {
        this.showDeleteCapaDialog.set(false);
      }
    });
  }

  deleteAlbum(): void {
    const album = this.album();
    if (!album) return;

    this.albumService.deletar(album.id).subscribe({
      next: () => {
        this.toastService.success('Álbum excluído com sucesso!');
        this.router.navigate(['/albuns']);
      },
      error: () => {
        this.showDeleteDialog.set(false);
      }
    });
  }
}
