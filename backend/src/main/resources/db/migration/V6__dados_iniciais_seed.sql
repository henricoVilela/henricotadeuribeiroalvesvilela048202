-- Inserção de dados iniciais para teste e demonstração do sistema.
-- Usuário de teste:
--   - username: admin
--   - password: admin123 (hash bcrypt gerado com strength 10)

INSERT INTO usuario (username, email, password_hash, nome_completo, ativo) VALUES
('admin', 'admin@sistema.com', '$2a$10$8K1p/fOvKgHYh5.R3DqYxOO1AqD5O8FKD.Ky8NJFyJKwJyqZ5YGCK', 'Administrador do Sistema', true);

-- -----------------------------------------------------------------------------
-- Artistas de exemplo conforme especificação do projeto
-- -----------------------------------------------------------------------------

-- Serj Tankian (Cantor - vocalista do System of a Down)
INSERT INTO artista (nome, nome_busca, tipo, pais_origem, ano_formacao, biografia, ativo) VALUES
('Serj Tankian', 'serj tankian', 'CANTOR', 'Estados Unidos', 1994, 
 'Serj Tankian é um cantor, compositor e multi-instrumentista armênio-americano, mais conhecido como vocalista da banda System of a Down.', 
 true);

-- Mike Shinoda (Cantor - cofundador do Linkin Park)
INSERT INTO artista (nome, nome_busca, tipo, pais_origem, ano_formacao, biografia, ativo) VALUES
('Mike Shinoda', 'mike shinoda', 'CANTOR', 'Estados Unidos', 1996, 
 'Mike Shinoda é um músico, rapper, cantor, compositor e produtor americano, cofundador e vocalista da banda Linkin Park.', 
 true);

-- Michel Teló (Cantor sertanejo brasileiro)
INSERT INTO artista (nome, nome_busca, tipo, pais_origem, ano_formacao, biografia, ativo) VALUES
('Michel Teló', 'michel telo', 'CANTOR', 'Brasil', 2008, 
 'Michel Teló é um cantor, compositor e músico brasileiro de música sertaneja. Ficou mundialmente conhecido com o hit "Ai Se Eu Te Pego".', 
 true);

-- Guns N' Roses (Banda de rock americana)
INSERT INTO artista (nome, nome_busca, tipo, pais_origem, ano_formacao, biografia, ativo) VALUES
('Guns N'' Roses', 'guns n roses', 'BANDA', 'Estados Unidos', 1985, 
 'Guns N'' Roses é uma banda de hard rock americana formada em Los Angeles em 1985. É uma das bandas mais vendidas de todos os tempos.', 
 true);

-- -----------------------------------------------------------------------------
-- Álbuns dos artistas
-- -----------------------------------------------------------------------------

-- Álbuns do Serj Tankian (artista_id = 1)
INSERT INTO album (artista_id, nome, nome_busca, ano_lancamento, gravadora, genero, total_faixas, ativo) VALUES
(1, 'Harakiri', 'harakiri', 2012, 'Serjical Strike Records', 'Rock Alternativo', 10, true),
(1, 'Black Blooms', 'black blooms', 2019, 'Serjical Strike Records', 'Rock Experimental', 4, true),
(1, 'The Rough Dog', 'the rough dog', 2021, 'Serjical Strike Records', 'Rock', 8, true);

-- Álbuns do Mike Shinoda (artista_id = 2)
INSERT INTO album (artista_id, nome, nome_busca, ano_lancamento, gravadora, genero, total_faixas, ativo) VALUES
(2, 'The Rising Tied', 'the rising tied', 2005, 'Warner Bros. Records', 'Hip Hop', 16, true),
(2, 'Post Traumatic', 'post traumatic', 2018, 'Warner Bros. Records', 'Rock Alternativo', 16, true),
(2, 'Post Traumatic EP', 'post traumatic ep', 2018, 'Warner Bros. Records', 'Rock Alternativo', 3, true),
(2, 'Where''d You Go', 'whered you go', 2006, 'Warner Bros. Records', 'Hip Hop', 1, true);

-- Álbuns do Michel Teló (artista_id = 3)
INSERT INTO album (artista_id, nome, nome_busca, ano_lancamento, gravadora, genero, total_faixas, ativo) VALUES
(3, 'Bem Sertanejo', 'bem sertanejo', 2014, 'Som Livre', 'Sertanejo', 20, true),
(3, 'Bem Sertanejo - O Show (Ao Vivo)', 'bem sertanejo o show ao vivo', 2015, 'Som Livre', 'Sertanejo', 25, true),
(3, 'Bem Sertanejo - (1ª Temporada) - EP', 'bem sertanejo 1a temporada ep', 2014, 'Som Livre', 'Sertanejo', 5, true);

-- Álbuns do Guns N' Roses (artista_id = 4)
INSERT INTO album (artista_id, nome, nome_busca, ano_lancamento, gravadora, genero, total_faixas, ativo) VALUES
(4, 'Use Your Illusion I', 'use your illusion i', 1991, 'Geffen Records', 'Hard Rock', 16, true),
(4, 'Use Your Illusion II', 'use your illusion ii', 1991, 'Geffen Records', 'Hard Rock', 14, true),
(4, 'Greatest Hits', 'greatest hits', 2004, 'Geffen Records', 'Hard Rock', 14, true);