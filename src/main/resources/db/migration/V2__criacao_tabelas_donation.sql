CREATE TABLE doacao (
   id UUID PRIMARY KEY,
   doador_id UUID NOT NULL,
   data_doacao DATE,
   status VARCHAR(50),
   CONSTRAINT fk_doacao_doador FOREIGN KEY (doador_id) REFERENCES pessoa(id)
);

CREATE TABLE item_doado (
    id UUID PRIMARY KEY,
    doacao_id UUID NOT NULL,
    tipo_item VARCHAR(100) NOT NULL,
    descricao TEXT NOT NULL,
    CONSTRAINT fk_item_doado_doacao FOREIGN KEY (doacao_id) REFERENCES doacao(id)
);