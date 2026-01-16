
-- Criação de funções utilitárias e triggers para automação do banco.

-- Função: Atualizar campo updated_at automaticamente
CREATE OR REPLACE FUNCTION fn_atualizar_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers para atualização automática de updated_at
CREATE TRIGGER trg_usuario_updated_at
    BEFORE UPDATE ON usuario
    FOR EACH ROW
    EXECUTE FUNCTION fn_atualizar_updated_at();

CREATE TRIGGER trg_artista_updated_at
    BEFORE UPDATE ON artista
    FOR EACH ROW
    EXECUTE FUNCTION fn_atualizar_updated_at();

CREATE TRIGGER trg_album_updated_at
    BEFORE UPDATE ON album
    FOR EACH ROW
    EXECUTE FUNCTION fn_atualizar_updated_at();

CREATE TRIGGER trg_regional_updated_at
    BEFORE UPDATE ON regional
    FOR EACH ROW
    EXECUTE FUNCTION fn_atualizar_updated_at();

-- -----------------------------------------------------------------------------
-- Função: Remover acentos de texto (útil para buscas)
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_remover_acentos(texto TEXT)
RETURNS TEXT AS $$
BEGIN
    RETURN TRANSLATE(
        LOWER(texto),
        'áàâãäéèêëíìîïóòôõöúùûüçñÁÀÂÃÄÉÈÊËÍÌÎÏÓÒÔÕÖÚÙÛÜÇÑ',
        'aaaaaeeeeiiiiooooouuuucnAAAAAEEEEIIIIOOOOOUUUUCN'
    );
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- -----------------------------------------------------------------------------
-- Função: Atualizar nome_busca automaticamente (artista)
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_artista_nome_busca()
RETURNS TRIGGER AS $$
BEGIN
    NEW.nome_busca = fn_remover_acentos(NEW.nome);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_artista_nome_busca
    BEFORE INSERT OR UPDATE OF nome ON artista
    FOR EACH ROW
    EXECUTE FUNCTION fn_artista_nome_busca();

-- -----------------------------------------------------------------------------
-- Função: Atualizar nome_busca automaticamente (album)
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_album_nome_busca()
RETURNS TRIGGER AS $$
BEGIN
    NEW.nome_busca = fn_remover_acentos(NEW.nome);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_album_nome_busca
    BEFORE INSERT OR UPDATE OF nome ON album
    FOR EACH ROW
    EXECUTE FUNCTION fn_album_nome_busca();

-- Comentários
COMMENT ON FUNCTION fn_atualizar_updated_at() IS 'Atualiza automaticamente o campo updated_at';
COMMENT ON FUNCTION fn_remover_acentos(TEXT) IS 'Remove acentos de um texto para facilitar buscas';
COMMENT ON FUNCTION fn_artista_nome_busca() IS 'Atualiza o campo nome_busca do artista automaticamente';
COMMENT ON FUNCTION fn_album_nome_busca() IS 'Atualiza o campo nome_busca do album automaticamente';
