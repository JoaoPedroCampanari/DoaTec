INSERT INTO pessoa (id, nome, email, senha, endereco, telefone, tipo)
VALUES (
           'b5e7c8f0-1a2b-3c4d-5e6f-7a8b9c0d1e2f',
           'Maria Doadora da Silva',
           'doador@teste.com',
           '123456',
           'Rua das Doações, 123',
           '(14) 98765-4321',
           'DOADOR_PF'
       );

INSERT INTO pessoa_fisica (pessoa_id, cpf)
VALUES (
           'b5e7c8f0-1a2b-3c4d-5e6f-7a8b9c0d1e2f',
           '123456789'
       );

INSERT INTO pessoa (id, nome, email, senha, endereco, telefone, tipo)
VALUES (
           'c6f8d9e1-2b3c-4d5e-6f7a-8b9c0d1e2f3a',
           'Empresa Solidária Ltda',
           'doadorpj@teste.com',
           'senha789',
           'Avenida do Bem, 789',
           '(14) 3471-1234',
           'DOADOR_PJ'
       );

INSERT INTO pessoa_juridica (pessoa_id, cnpj)
VALUES (
           'c6f8d9e1-2b3c-4d5e-6f7a-8b9c0d1e2f3a',
           '11222333000144'
       );