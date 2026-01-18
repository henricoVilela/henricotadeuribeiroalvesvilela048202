package com.projeto.backend.domain.artista;

public enum TipoArtista {
    CANTOR("Cantor"),
    BANDA("Banda"),
    DUPLA("Dupla"),
    GRUPO("Grupo");

    private final String descricao;

    TipoArtista(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
