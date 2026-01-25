export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  nome: string;
}

export interface AuthResponse {
  access_token: string;
  refresh_token: string;
  token_type: string;
  expires_in: number;
  username: string;
}

export interface RefreshRequest {
  refresh_token: string;
}

export interface User {
  id: number;
  username: string;
  email: string;
  nome: string;
}
