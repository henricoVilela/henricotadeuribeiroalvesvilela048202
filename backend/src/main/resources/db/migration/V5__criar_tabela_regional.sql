
-- Os dados são importados periodicamente e a sincronização segue regras específicas de inativação/atualização.
--   1. Novo no endpoint → inserir na tabela local
--   2. Não disponível no endpoint → inativar na tabela local
--   3. Atributo alterado → inativar registro anterior e criar novo
CREATE TABLE regional (
    id              INTEGER         PRIMARY KEY,
    nome            VARCHAR(200)    NOT NULL,
    ativo           BOOLEAN         NOT NULL DEFAULT TRUE,
    external_id     INTEGER         NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_regional_nome ON regional(nome);
CREATE INDEX idx_regional_ativo ON regional(ativo);
CREATE INDEX idx_regional_external_id ON regional(external_id);

-- Comentários nas colunas
COMMENT ON TABLE regional IS 'Tabela de regionais sincronizada com API externa da Polícia Civil';
COMMENT ON COLUMN regional.external_id IS 'ID original da API externa para rastreamento';
