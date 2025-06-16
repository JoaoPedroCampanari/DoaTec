INSERT INTO pessoa (nome, email, senha, endereco, telefone, tipo, documento)
VALUES (
           'Aluno de Teste da Silva',
           'aluno@teste.com',
           'senha123',
           'Rua da Fatec, 100',
           '(14) 99999-8888',
           'ALUNO', -- Enum name no formato String
           '1234567890123' -- RA do aluno
       );

INSERT INTO pessoa (nome, email, senha, endereco, telefone, tipo, documento)
VALUES (
           'Maria Doadora da Silva',
           'doador@teste.com',
           '123456',
           'Rua das Doações, 123',
           '(14) 98765-4321',
           'DOADOR_PF', -- Enum name no formato String
           '123456789' -- CPF do doador
       );

INSERT INTO pessoa (nome, email, senha, endereco, telefone, tipo, documento)
VALUES (
           'Empresa Solidária Ltda',
           'doadorpj@teste.com',
           'senha789',
           'Avenida do Bem, 789',
           '(14) 3471-1234',
           'DOADOR_PJ', -- Enum name no formato String
           '11222333000144' -- CNPJ da empresa
       );