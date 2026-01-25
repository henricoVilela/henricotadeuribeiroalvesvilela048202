import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Artista, ArtistaPage, ArtistaRequest } from '../models/artista.model';
import { Album } from '../models/album.model';

@Injectable({
  providedIn: 'root'
})
export class ArtistaService {
  private readonly apiUrl = `${environment.apiUrl}/artistas`;

  constructor(private http: HttpClient) {}

  listar(
    nome?: string,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'nome',
    sortDir: string = 'asc'
  ): Observable<ArtistaPage> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);

    if (nome) {
      params = params.set('nome', nome);
    }

    return this.http.get<ArtistaPage>(this.apiUrl, { params });
  }

  buscarPorId(id: number): Observable<Artista> {
    return this.http.get<Artista>(`${this.apiUrl}/${id}`);
  }

  buscarAlbuns(id: number): Observable<Album[]> {
    return this.http.get<Album[]>(`${this.apiUrl}/${id}/albuns`);
  }

  criar(artista: ArtistaRequest): Observable<Artista> {
    return this.http.post<Artista>(this.apiUrl, artista);
  }

  atualizar(id: number, artista: ArtistaRequest): Observable<Artista> {
    return this.http.put<Artista>(`${this.apiUrl}/${id}`, artista);
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
