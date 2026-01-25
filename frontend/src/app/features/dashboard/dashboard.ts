import { Component, inject, OnInit, signal } from '@angular/core';
import { Layout } from '../../shared/components/layout/layout.component';
import { Loading } from '../../shared/components/loaging/loading.component';
import { ArtistaService } from '../../core/services/artista.service';
import { AlbumService } from '../../core/services/album.service';
import { Artista } from '../../core/models/artista.model';
import { Album } from '../../core/models/album.model';
import { forkJoin } from 'rxjs';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  imports: [Layout, Loading, RouterLink],
  templateUrl: './dashboard.html',
})
export class Dashboard implements OnInit {
  private artistaService = inject(ArtistaService);
  private albumService = inject(AlbumService);

  loading = signal(true);
  artistas = signal<Artista[]>([]);
  albuns = signal<Album[]>([]);
  generos = signal<string[]>([]);
  totalArtistas = signal(0);
  totalAlbuns = signal(0);
  totalCapas = signal(0);

  ngOnInit(): void {
    this.loadData();
  }

  private loadData(): void {
    forkJoin({
      artistas: this.artistaService.listar(undefined, 0, 10),
      albuns: this.albumService.listar(undefined, undefined, undefined, 0, 10),
      generos: this.albumService.listarGeneros()
    }).subscribe({
      next: ({ artistas, albuns, generos }) => {
        this.artistas.set(artistas.content);
        this.totalArtistas.set(artistas.totalElements);
        this.albuns.set(albuns.content);
        this.totalAlbuns.set(albuns.totalElements);
        this.generos.set(generos);
        
        // Calcular total de capas
        const capas = albuns.content.reduce((acc, album) => acc + album.totalCapas, 0);
        this.totalCapas.set(capas);
        
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }
}
