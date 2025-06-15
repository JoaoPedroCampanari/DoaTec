CREATE TABLE aluno (
   id UUID PRIMARY KEY,
   nome VARCHAR(255) NOT NULL,
   email VARCHAR(255) NOT NULL UNIQUE,
   senha VARCHAR(255),
   endereco VARCHAR(255),
   telefone VARCHAR(20),
   tipo VARCHAR(50),
   ra VARCHAR(20) UNIQUE
);