CREATE TABLE pessoa_fisica (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    endereco VARCHAR(255),
    cpf VARCHAR(14) NOT NULL UNIQUE
);


CREATE TABLE pessoa_juridica (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    endereco VARCHAR(255),
    cnpj VARCHAR(18) NOT NULL UNIQUE
);