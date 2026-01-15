CREATE TABLE artista (
    id              BIGSERIAL       PRIMARY KEY,
    nome            VARCHAR(200)    NOT NULL,
    nome_busca      VARCHAR(200)    NOT NULL,
    tipo            VARCHAR(20)     NOT NULL DEFAULT 'CANTOR',
    pais_origem     VARCHAR(100)    NULL,
    ano_formacao    INTEGER         NULL,
    biografia       TEXT            NULL,
    ativo           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT ck_artista_tipo CHECK (tipo IN ('CANTOR', 'BANDA', 'DUPLA', 'GRUPO'))
);

CREATE INDEX idx_artista_nome ON artista(nome);
CREATE INDEX idx_artista_nome_busca ON artista(nome_busca);
CREATE INDEX idx_artista_tipo ON artista(tipo);

-- Índice para busca case-insensitive e ordenação
CREATE INDEX idx_artista_nome_lower ON artista(LOWER(nome));

COMMENT ON TABLE artista IS 'Tabela de artistas (cantores, bandas, duplas e grupos)';
COMMENT ON COLUMN artista.nome_busca IS 'Nome normalizado para buscas (sem acentos, lowercase)';
COMMENT ON COLUMN artista.tipo IS 'Tipo do artista: CANTOR, BANDA, DUPLA ou GRUPO';
