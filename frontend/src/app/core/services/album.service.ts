import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Album, AlbumCapa, AlbumPage, AlbumRequest, TipoCapa } from '../models/album.model';

@Injectable({
  providedIn: 'root'
})
export class AlbumService {
  private readonly apiUrl = `${environment.apiUrl}/albuns`;
  private readonly capasUrl = `${environment.apiUrl}/capas`;

  constructor(private http: HttpClient) {}

  listar(
    nome?: string,
    artista?: string,
    genero?: string,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'nome',
    sortDir: string = 'asc'
  ): Observable<AlbumPage> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);

    if (nome) params = params.set('nome', nome);
    if (artista) params = params.set('artista', artista);
    if (genero) params = params.set('genero', genero);

    return this.http.get<AlbumPage>(this.apiUrl, { params });
  }

  buscarPorId(id: number): Observable<Album> {
    return this.http.get<Album>(`${this.apiUrl}/${id}`);
  }

  listarGeneros(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/generos`);
  }

  criar(album: AlbumRequest): Observable<Album> {
    return this.http.post<Album>(this.apiUrl, album);
  }

  atualizar(id: number, album: AlbumRequest): Observable<Album> {
    return this.http.put<Album>(`${this.apiUrl}/${id}`, album);
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Capas
  listarCapas(albumId: number): Observable<AlbumCapa[]> {
    return this.http.get<AlbumCapa[]>(`${this.apiUrl}/${albumId}/capas`);
  }

  uploadCapa(albumId: number, file: File, tipoCapa: TipoCapa = 'FRENTE'): Observable<AlbumCapa> {
    const formData = new FormData();
    formData.append('file', file);
    
    const params = new HttpParams().set('tipoCapa', tipoCapa);
    
    return this.http.post<AlbumCapa>(`${this.apiUrl}/${albumId}/capas`, formData, { params });
  }

  deletarCapa(capaId: number): Observable<void> {
    return this.http.delete<void>(`${this.capasUrl}/${capaId}`);
  }

  deletarTodasCapas(albumId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${albumId}/capas`);
  }
}
