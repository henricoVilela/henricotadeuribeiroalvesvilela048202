package com.projeto.backend.domain.album;

public enum TipoCapa {
    FRENTE("Capa Frontal"),
    VERSO("Capa Traseira"),
    ENCARTE("Encarte"),
    DISCO("Disco"),
    PROMOCIONAL("Promocional"),
    OUTRO("Outro");

    private final String descricao;

    TipoCapa(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}

