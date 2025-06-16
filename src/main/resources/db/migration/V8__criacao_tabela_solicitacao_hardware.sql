CREATE TABLE solicitacao_hardware (
                                      id INT PRIMARY KEY AUTO_INCREMENT, -- Alterado para INT e AUTO_INCREMENT
                                      aluno_id INT NOT NULL, -- Alterado para INT
                                      status VARCHAR(255) NOT NULL,
                                      data_solicitacao DATE,
                                      CONSTRAINT fk_solicitacao_aluno FOREIGN KEY (aluno_id) REFERENCES pessoa(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;