# DoaTec - Servicos e Endpoints

## Servicos Docker

| Servico | URL | Credenciais |
|---|---|---|
| Aplicacao DoaTec | http://localhost:8080 | Login via aplicacao |
| Super Admin (auto-criado) | http://localhost:8080/admin.html | admin@doatec.com / Admin@123 (**Troque a senha em produção!**) |
| pgAdmin | http://localhost:5050 | admin@doatec.com / admin123 |
| PostgreSQL | localhost:5432 | doatec_user / doatec_password |
| Mailhog UI | http://localhost:8025 | (acesso direto ao painel) |

### Conectar ao banco via pgAdmin

1. Acesse http://localhost:5050
2. Faca login com admin@doatec.com / admin123
3. Add New Server:
   - Name: DoaTec
   - Host: db
   - Port: 5432
   - Username: doatec_user
   - Password: doatec_password

---

## Hierarquia de Roles

```
SUPER_ADMIN > ADMIN > USER
```

- **SUPER_ADMIN**: Acesso total + gestão de administradores (botão "Criar Admin" na aba Usuários)
- **ADMIN**: Gestão de doações, solicitações, suporte e usuários (5 abas)
- **USER**: Acesso básico ao sistema

---

## API Endpoints

### Autenticacao (publico)

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | /api/login | Login do usuario |
| POST | /api/logout | Logout do usuario |

### Registro (publico)

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | /api/register | Registro generico (legado) |
| POST | /api/register/aluno | Registrar aluno |
| POST | /api/register/doador-pf | Registrar doador PF |
| POST | /api/register/doador-pj | Registrar doador PJ |

### Usuarios (autenticado)

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | /api/users/me | Dados do usuario logado |
| GET | /api/users/{id} | Buscar usuario por ID |
| PUT | /api/users/{id} | Atualizar usuario |
| GET | /api/users/{id}/donations | Doacoes do usuario |
| GET | /api/users/{id}/solicitacoes | Solicitacoes do usuario |

### Doacoes (autenticado)

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | /api/donations | Criar doacao |

### Upload (publico)

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | /api/upload/foto | Upload de foto |
| GET | /uploads/** | Servir arquivos enviados |

### Solicitacoes (autenticado)

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | /api/solicitacoes | Criar solicitacao |

### Suporte (autenticado)

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | /api/suporte | Enviar mensagem de suporte |

### Dashboard (publico)

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | /api/dashboard/stats | Estatisticas gerais |

### Notificacoes (autenticado)

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | /api/notificacoes/usuario/{id} | Notificacoes do usuario |
| GET | /api/notificacoes/usuario/{id}/nao-lidas | Notificacoes nao lidas |
| GET | /api/notificacoes/usuario/{id}/count | Contagem de nao lidas |
| GET | /api/notificacoes/usuario/{id}/resumo | Resumo das notificacoes |
| PUT | /api/notificacoes/{id}/ler | Marcar como lida |
| PUT | /api/notificacoes/usuario/{id}/ler-todas | Marcar todas como lidas |

### Admin (requer role ADMIN ou SUPER_ADMIN)

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | /api/admin/dashboard | Dashboard admin |
| GET | /api/admin/doacoes | Listar doacoes |
| PUT | /api/admin/doacoes/{id}/aprovar | Aprovar doacao |
| PUT | /api/admin/doacoes/{id}/rejeitar | Rejeitar doacao |
| PUT | /api/admin/doacoes/{id}/status?novoStatus=... | Alterar status da doacao (transicoes validadas) |
| GET | /api/admin/solicitacoes | Listar solicitacoes |
| PUT | /api/admin/solicitacoes/{id}/aprovar | Aprovar solicitacao |
| PUT | /api/admin/solicitacoes/{id}/rejeitar | Rejeitar solicitacao |
| PUT | /api/admin/solicitacoes/{id}/concluir | Concluir solicitacao aprovada |
| GET | /api/admin/suporte | Listar mensagens de suporte |
| PUT | /api/admin/suporte/{id}/responder | Responder suporte |
| PUT | /api/admin/suporte/{id}/status | Alterar status do suporte |
| GET | /api/admin/usuarios | Listar usuarios |
| GET | /api/admin/usuarios/tipo/{tipoPessoa} | Usuarios por tipo |
| GET | /api/admin/usuarios/role/{role} | Usuarios por role |
| PUT | /api/admin/usuarios/{id}/status | Alterar status do usuario |
| PUT | /api/admin/usuarios/{id}/role | Alterar role do usuario |

### Super Admin (requer role SUPER_ADMIN)

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | /api/super-admin/admins | Listar administradores |
| POST | /api/super-admin/admins | Criar novo administrador |
| PUT | /api/super-admin/admins/{id}/rebaixar | Rebaixar admin para USER |
| PUT | /api/super-admin/admins/{id}/role?novaRole=... | Alterar role do admin |
| PUT | /api/super-admin/admins/{id}/status | Alterar status do admin |
| DELETE | /api/super-admin/admins/{id} | Desativar administrador (soft delete) |

### Inventario (requer role ADMIN)

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | /api/admin/inventario | Listar inventario |
| GET | /api/admin/inventario/{id} | Buscar item por ID |
| GET | /api/admin/inventario/disponiveis | Itens disponiveis |
| GET | /api/admin/inventario/sugestoes/{solicitacaoId} | Sugestoes para solicitacao |
| POST | /api/admin/inventario/{equipamentoId}/atribuir/{solicitacaoId} | Atribuir equipamento |
| PUT | /api/admin/inventario/{id}/entregar | Marcar como entregue |

---

## Paginas (Frontend)

| Pagina | URL |
|---|---|
| Home | http://localhost:8080/ |
| Login | http://localhost:8080/login.html |
| Registro | http://localhost:8080/registro.html |
| Perfil | http://localhost:8080/perfil.html |
| Aluno | http://localhost:8080/aluno.html |
| Doar | http://localhost:8080/donate.html |
| Minhas Doacoes | http://localhost:8080/minhas-doacoes.html |
| Meus Pedidos | http://localhost:8080/meus-pedidos.html |
| Suporte | http://localhost:8080/suporte.html |
| Sobre | http://localhost:8080/sobre.html |
| Painel Admin | http://localhost:8080/admin.html |

---

## Comandos Docker

| Acao | Comando |
|---|---|
| Iniciar | docker compose up -d |
| Iniciar com rebuild | docker compose up --build -d |
| Parar | docker compose down |
| Ver logs | docker compose logs -f app |
| Ver status | docker compose ps |
