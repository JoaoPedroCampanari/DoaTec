CREATE TABLE solicitacao_hardware (
    id UUID PRIMARY KEY,
    aluno_id UUID NOT NULL,
    status VARCHAR(255) NOT NULL,
    data_solicitacao DATE,
    CONSTRAINT fk_solicitacao_aluno FOREIGN KEY (aluno_id) REFERENCES pessoa(id)
);