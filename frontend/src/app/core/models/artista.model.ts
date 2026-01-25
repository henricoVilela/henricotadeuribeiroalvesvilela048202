export type TipoArtista = 'CANTOR' | 'BANDA' | 'DUPLA' | 'GRUPO';

export interface Artista {
  id: number;
  nome: string;
  tipo: TipoArtista;
  tipoDescricao: string;
  paisOrigem: string | null;
  anoFormacao: number | null;
  biografia: string | null;
  ativo: boolean;
  totalAlbuns: number;
  createdAt: string;
  updatedAt: string;
}

export interface ArtistaRequest {
  nome: string;
  tipo: TipoArtista;
  paisOrigem?: string;
  anoFormacao?: number;
  biografia?: string;
}

export interface ArtistaPage {
  content: Artista[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
