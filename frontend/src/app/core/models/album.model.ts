export type TipoCapa = 'FRENTE' | 'VERSO' | 'ENCARTE' | 'DISCO' | 'PROMOCIONAL' | 'OUTRO';

export interface AlbumCapa {
  id: number;
  objectKey: string;
  nomeArquivo: string;
  contentType: string;
  tamanhoBytes: number;
  tipoCapa: TipoCapa;
  ordem: number;
  url: string | null;
  createdAt: string;
}

export interface Album {
  id: number;
  artistaId: number;
  artistaNome: string;
  nome: string;
  anoLancamento: number | null;
  gravadora: string | null;
  genero: string | null;
  totalFaixas: number | null;
  duracaoTotal: number | null;
  descricao: string | null;
  ativo: boolean;
  totalCapas: number;
  capas: AlbumCapa[];
  createdAt: string;
  updatedAt: string;
}

export interface AlbumRequest {
  artistaId: number;
  nome: string;
  anoLancamento?: number;
  gravadora?: string;
  genero?: string;
  totalFaixas?: number;
  duracaoTotal?: number;
  descricao?: string;
}

export interface AlbumPage {
  content: Album[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
