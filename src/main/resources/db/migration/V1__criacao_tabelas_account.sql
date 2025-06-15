CREATE TABLE pessoa (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255),
    endereco VARCHAR(255),
    telefone VARCHAR(20),
    tipo VARCHAR(50)
);

CREATE TABLE pessoa_fisica (
    pessoa_id UUID PRIMARY KEY,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    CONSTRAINT fk_pessoafisica_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa(id)
);

CREATE TABLE pessoa_juridica (
    pessoa_id UUID PRIMARY KEY,
    cnpj VARCHAR(18) NOT NULL UNIQUE,
    CONSTRAINT fk_pessoajuridica_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa(id)
);

CREATE TABLE aluno (
    pessoa_id UUID PRIMARY KEY,
    ra VARCHAR(20) UNIQUE,
    CONSTRAINT fk_aluno_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa(id)
);