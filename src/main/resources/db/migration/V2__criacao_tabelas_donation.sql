CREATE TABLE doacao (
                        id INT PRIMARY KEY AUTO_INCREMENT, -- Alterado para INT e AUTO_INCREMENT
                        doador_id INT NOT NULL, -- Alterado para INT
                        data_doacao DATE,
                        status VARCHAR(50),
                        CONSTRAINT fk_doacao_doador FOREIGN KEY (doador_id) REFERENCES pessoa(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE item_doado (
                            id INT PRIMARY KEY AUTO_INCREMENT, -- Alterado para INT e AUTO_INCREMENT
                            doacao_id INT NOT NULL, -- Alterado para INT
                            tipo_item VARCHAR(100) NOT NULL,
                            descricao TEXT NOT NULL,
                            CONSTRAINT fk_item_doado_doacao FOREIGN KEY (doacao_id) REFERENCES doacao(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;