-- V14__refatorar_tabela_pessoa.sql
-- Este script refatora a tabela pessoa e remove as tabelas de herança.

-- 1. Adiciona a nova coluna 'documento' à tabela 'pessoa'
ALTER TABLE pessoa
    ADD COLUMN documento VARCHAR(36);

-- 2. Copia dados da tabela 'pessoa_fisica' para a nova coluna 'documento' na tabela 'pessoa'
UPDATE pessoa p
SET p.documento = (SELECT pf.cpf FROM pessoa_fisica pf WHERE pf.pessoa_id = p.id)
WHERE p.tipo = 'DOADOR_PF' AND p.id IN (SELECT pessoa_id FROM pessoa_fisica);

-- 3. Copia dados da tabela 'pessoa_juridica' para a nova coluna 'documento' na tabela 'pessoa'
UPDATE pessoa p
SET p.documento = (SELECT pj.cnpj FROM pessoa_juridica pj WHERE pj.pessoa_id = p.id)
WHERE p.tipo = 'DOADOR_PJ' AND p.id IN (SELECT pessoa_id FROM pessoa_juridica);

-- 4. Copia dados da tabela 'aluno' para a nova coluna 'documento' na tabela 'pessoa'
UPDATE pessoa p
SET p.documento = (SELECT a.ra FROM aluno a WHERE a.pessoa_id = p.id)
WHERE p.tipo = 'ALUNO' AND p.id IN (SELECT pessoa_id FROM aluno);

-- 5. Adiciona a restrição UNIQUE à coluna 'documento'.
ALTER TABLE pessoa
    ADD CONSTRAINT uk_pessoa_documento UNIQUE (documento);

-- 6. Torna a coluna 'documento' NOT NULL para os tipos que a aplicação exige.
-- Esta parte deve ser cuidadosamente testada com seus dados existentes.
UPDATE pessoa p
SET p.documento = 'TEMP_DOC_' || p.id -- Preencher nulos se for o caso
WHERE p.documento IS NULL AND p.tipo IN ('DOADOR_PF', 'DOADOR_PJ', 'ALUNO');

ALTER TABLE pessoa MODIFY COLUMN documento VARCHAR(36) NOT NULL;

-- 7. Remove as tabelas antigas (PessoaFisica, PessoaJuridica, Aluno)
DROP TABLE pessoa_fisica;
DROP TABLE pessoa_juridica;
DROP TABLE aluno;