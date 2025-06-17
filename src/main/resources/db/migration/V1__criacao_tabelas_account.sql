CREATE TABLE pessoa (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        nome VARCHAR(255) NOT NULL,
                        email VARCHAR(255) NOT NULL UNIQUE,
                        senha VARCHAR(255),
                        endereco VARCHAR(255),
                        telefone VARCHAR(20),
                        tipo VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE pessoa_fisica (
                               pessoa_id INT PRIMARY KEY,
                               cpf VARCHAR(14) NOT NULL UNIQUE,
                               CONSTRAINT fk_pessoafisica_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE pessoa_juridica (
                                 pessoa_id INT PRIMARY KEY,
                                 cnpj VARCHAR(18) NOT NULL UNIQUE,
                                 CONSTRAINT fk_pessoajuridica_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE aluno (
                       pessoa_id INT PRIMARY KEY,
                       ra VARCHAR(20) UNIQUE,
                       CONSTRAINT fk_aluno_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;