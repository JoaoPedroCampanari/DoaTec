CREATE TABLE suporte_formulario (
                                    id INT PRIMARY KEY AUTO_INCREMENT, -- Alterado para INT e AUTO_INCREMENT
                                    autor_id INT, -- Alterado para INT
                                    nome VARCHAR(255) NOT NULL,
                                    email VARCHAR(255) NOT NULL,
                                    assunto VARCHAR(255) NOT NULL,
                                    mensagem TEXT NOT NULL,
                                    status VARCHAR(50) NOT NULL,
                                    data_criacao TIMESTAMP,
                                    CONSTRAINT fk_suporteformulario_autor FOREIGN KEY (autor_id) REFERENCES pessoa(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;