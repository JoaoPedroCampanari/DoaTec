CREATE TABLE suporte_formulario (
     id UUID PRIMARY KEY,
     autor_id UUID,
     nome VARCHAR(255) NOT NULL,
     email VARCHAR(255) NOT NULL,
     assunto VARCHAR(255) NOT NULL,
     mensagem TEXT NOT NULL,
     status VARCHAR(50) NOT NULL,
     data_criacao TIMESTAMP,
     CONSTRAINT fk_suporteformulario_autor FOREIGN KEY (autor_id) REFERENCES pessoa(id)
);