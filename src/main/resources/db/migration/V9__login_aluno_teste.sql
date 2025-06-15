INSERT INTO pessoa (id, nome, email, senha, endereco, telefone, tipo)
VALUES (
           'a4e6ed52-1b69-4a25-996b-95246757132a',
           'Aluno de Teste da Silva',
           'aluno@teste.com',
           'senha123',
           'Rua da Fatec, 100',
           '(14) 99999-8888',
           'ALUNO'
       );

INSERT INTO aluno (pessoa_id, ra, justificativa, preferencia_equipamento)
VALUES (
           'a4e6ed52-1b69-4a25-996b-95246757132a',
           '1234567890123',
           NULL,
           NULL
       );