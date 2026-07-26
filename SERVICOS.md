# DoaTec - Servicos e Endpoints

Atualizado: 2026-05-28 — sincronizado com 13 controllers ativos e 10 services.

## Servicos Docker

| Servico | URL | Credenciais |
|---|---|---|
| Aplicacao DoaTec | http://localhost:8080 | Login via aplicacao |
| Super Admin (auto-criado pelo `DataSeeder`) | http://localhost:8080/admin.html | admin@doatec.com / Admin@123 ⚠️ **Hardcoded — trocar antes de qualquer deploy real** |
| pgAdmin | http://localhost:5050 | admin@doatec.com / admin123 ⚠️ |
| PostgreSQL | localhost:5432 | doatec_user / doatec_password ⚠️ |
| Mailhog UI | http://localhost:8025 | (acesso direto ao painel) |
| Mailhog SMTP | localhost:1025 | (sem auth) |

> ⚠️ **Pendência crítica de segurança** (ver `analiseComplexa.md` §4 e `contexto.md` §99): senha do Super Admin, credenciais do banco e do pgAdmin estão hardcoded no repositório. Externalizar via env vars (`${SPRING_DATASOURCE_PASSWORD}`, `@Value("${app.bootstrap.super-admin.password}")`) e rotacionar antes de produção.

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
- **USER**: Acesso básico ao sistema (doar, solicitar, abrir ticket, chat)

Definida em `SecurityConfig` via `RoleHierarchy`; nenhum `@PreAuthorize` em métodos — autorização hoje é 100% baseada em URL matching.

---

## Camada de Servicos (Backend)

| Service | Linhas | Responsabilidade |
|---|---:|---|
| `AdminService` | ~556 | Aprovar/rejeitar/alterar doações, solicitações, suporte e usuários. ⚠️ God Service — quebrar em 4 (ver §11.2 do `analiseComplexa.md`) |
| `PessoaService` | ~400 | Cadastro (aluno/PF/PJ), atualização de perfil, listagem por tipo/role, alterar status/role |
| `InventarioService` | ~470 | CRUD manual de Equipamento, sugestões de matching, atribuir/entregar |
| `SuperAdminService` | ~210 | Criar admin, rebaixar, alterar role, alterar status, desativar (soft delete) |
| `DoacaoService` | ~150 | Registrar doação + itens + foto/comprovante |
| `NotificacaoService` | ~150 | Criar/listar/contar/marcar como lida |
| `SuporteFormularioService` | ~70 | Abrir ticket, listar tickets do usuário, deletar (autor) |
| `ChatService` | ~110 | Enviar mensagem, histórico por contexto + referencia |
| `EmailService` | ~120 | Envio via Mailhog (`@Slf4j`, fail-soft, único service com `@RequiredArgsConstructor`) |
| `SolicitacaoService` | ~80 | Criar/cancelar solicitação de hardware |

Detalhes em `analiseComplexa.md` §2 (Arquitetura) e §10 (Qualidade).

---

## API Endpoints

### Autenticacao (publico)

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | /api/login | Login do usuario (⚠️ sem rate limit / lockout — brute force aberto) |
| POST | /api/logout | Logout do usuario |

### Registro (publico)

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | /api/register | Registro generico (legado — `@Deprecated(forRemoval=true)`) |
| POST | /api/register/aluno | Registrar aluno |
| POST | /api/register/doador-pf | Registrar doador PF |
| POST | /api/register/doador-pj | Registrar doador PJ |

### Usuarios (autenticado)

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | /api/users/me | Dados do usuario logado (com contadores de doações/solicitações/tickets) |
| GET | /api/users/me/donations | Doacoes do usuario logado |
| GET | /api/users/me/solicitacoes | Solicitacoes do usuario logado |
| PUT | /api/users/me | Atualizar proprio perfil |
| GET | /api/users/{id} | Buscar usuario por ID (requer ADMIN ou propria conta) |
| PUT | /api/users/{id} | Atualizar usuario (requer ADMIN ou propria conta) |
| GET | /api/users/{id}/donations | Doacoes do usuario (requer ADMIN ou propria conta) |
| GET | /api/users/{id}/solicitacoes | Solicitacoes do usuario (requer ADMIN ou propria conta) |

### Doacoes (autenticado)

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | /api/donations | Listar todas as doacoes |
| POST | /api/donations | Criar doacao (com itens + fotoUrl + comprovanteUrl) |
| DELETE | /api/donations/{id} | Excluir doacao (ownership: doador ou ADMIN) |

### Solicitacoes (autenticado)

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | /api/solicitacoes | Criar solicitacao (aluno) |
| DELETE | /api/solicitacoes/{id} | Cancelar solicitacao (ownership: aluno ou ADMIN) |

### Suporte (autenticado)

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | /api/suporte | Enviar mensagem de suporte / abrir ticket |
| GET | /api/suporte/meus-tickets | Listar tickets do usuario logado |
| DELETE | /api/suporte/{id} | Deletar ticket (autor ou ADMIN) |

### Chat (autenticado)

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | /api/chat/enviar | Enviar mensagem (body: contexto, referenciaId, conteudo) |
| GET | /api/chat/historico/{contexto}/{referenciaId} | Histórico do chat (contexto: SUPORTE/DOACAO/SOLICITACAO) |

### Upload (autenticado — cai em anyRequest)

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | /api/upload/foto | Upload de imagem (JPEG/PNG/GIF/WebP, magic bytes validados, max 5MB) |
| POST | /api/upload/comprovante | Upload de imagem ou PDF (max 5MB) |
| GET | /uploads/** | Servir arquivos enviados (público — ⚠️ contém comprovantes com dados LGPD) |

### Dashboard (publico)

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | /api/dashboard/stats | Estatisticas gerais para landing page |

### Notificacoes (autenticado)

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | /api/notificacoes | Todas as notificacoes do usuario logado |
| GET | /api/notificacoes/nao-lidas | Notificacoes nao lidas |
| GET | /api/notificacoes/count | Contagem de nao lidas (polled a cada 60s pelo frontend) |
| PUT | /api/notificacoes/ler-todas | Marcar todas como lidas |
| DELETE | /api/notificacoes/{notificacaoId} | Deletar notificacao (apenas destinatario) |

### Admin (requer role ADMIN ou SUPER_ADMIN)

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | /api/admin/dashboard | Dashboard admin (contadores e estatísticas) |
| GET | /api/admin/doacoes | Listar doacoes paginadas |
| PUT | /api/admin/doacoes/{id}/aprovar | Aprovar doacao |
| PUT | /api/admin/doacoes/{id}/rejeitar | Rejeitar doacao (body: motivoRejeicao) |
| PUT | /api/admin/doacoes/{id}/status?novoStatus=... | Alterar status da doacao (transicoes validadas) |
| GET | /api/admin/solicitacoes | Listar solicitacoes paginadas |
| PUT | /api/admin/solicitacoes/{id}/aprovar | Aprovar solicitacao |
| PUT | /api/admin/solicitacoes/{id}/rejeitar | Rejeitar solicitacao (body: motivoRejeicao) |
| PUT | /api/admin/solicitacoes/{id}/status?novoStatus=... | Alterar status da solicitacao (transicoes validadas) |
| PUT | /api/admin/solicitacoes/{id}/concluir | Concluir solicitacao aprovada |
| GET | /api/admin/suporte | Listar mensagens de suporte |
| PUT | /api/admin/suporte/{id}/responder | Responder suporte |
| PUT | /api/admin/suporte/{id}/status | Alterar status do suporte |
| GET | /api/admin/usuarios | Listar usuarios paginados |
| GET | /api/admin/usuarios/tipo/{tipoPessoa} | Usuarios por tipo (ALUNO/DOADOR_PF/DOADOR_PJ) |
| GET | /api/admin/usuarios/role/{role} | Usuarios por role (USER/ADMIN/SUPER_ADMIN) |
| PUT | /api/admin/usuarios/{id}/status | Alterar status (ativo/inativo) do usuario |
| PUT | /api/admin/usuarios/{id}/role | Alterar role do usuario |

### Inventario (requer role ADMIN ou SUPER_ADMIN)

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | /api/admin/inventario | Listar inventario com filtros (status, tipoEquipamento, etc.) |
| POST | /api/admin/inventario | Criar equipamento manualmente |
| GET | /api/admin/inventario/{id} | Buscar item por ID |
| PUT | /api/admin/inventario/{id} | Editar equipamento (apenas DISPONIVEL) |
| DELETE | /api/admin/inventario/{id} | Excluir equipamento (apenas DISPONIVEL) |
| GET | /api/admin/inventario/disponiveis | Itens disponiveis |
| GET | /api/admin/inventario/sugestoes/{solicitacaoId} | Sugestoes de matching para solicitacao |
| POST | /api/admin/inventario/{equipamentoId}/atribuir/{solicitacaoId} | Atribuir equipamento à solicitacao |
| PUT | /api/admin/inventario/{id}/entregar | Marcar como entregue |

### Super Admin (requer role SUPER_ADMIN)

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | /api/super-admin/admins | Listar administradores |
| POST | /api/super-admin/admins | Criar novo administrador |
| PUT | /api/super-admin/admins/{id}/rebaixar | Rebaixar admin para USER |
| PUT | /api/super-admin/admins/{id}/role?novaRole=... | Alterar role do admin |
| PUT | /api/super-admin/admins/{id}/status | Alterar status do admin (body: `{"ativo": true/false}`) |
| DELETE | /api/super-admin/admins/{id} | Desativar administrador (soft delete) |

---

## Paginas (Frontend)

13 páginas HTML estáticas em `src/main/resources/static/`, todas consumindo a API JSON.

| Pagina | URL | Auth |
|---|---|---|
| Home | http://localhost:8080/ | público |
| Login | http://localhost:8080/login.html | público |
| Registro | http://localhost:8080/registro.html | público |
| Sobre | http://localhost:8080/sobre.html | público |
| Perfil | http://localhost:8080/perfil.html | autenticado |
| Aluno | http://localhost:8080/aluno.html | role USER (aluno) |
| Doar | http://localhost:8080/donate.html | autenticado |
| Minhas Doacoes | http://localhost:8080/minhas-doacoes.html | autenticado |
| Meus Pedidos | http://localhost:8080/meus-pedidos.html | autenticado |
| Suporte | http://localhost:8080/suporte.html | autenticado |
| Meus Tickets | http://localhost:8080/meus-tickets.html | autenticado |
| Painel Admin | http://localhost:8080/admin.html | role ADMIN/SUPER_ADMIN |

Scripts comuns: `main.js`, `auth.js` (wrapper `apiFetch` com CSRF header + timeout 10s), `nav-visibility.js`, `notifications.js` (polling 60s), `theme-switcher.js`, `viacep.js`, `vendor/imask.min.js`. CSS único: `style.css` (4011 linhas).

---

## Endpoints publicos (permitAll no SecurityConfig)

- `/` `/index.html` `/login.html` `/registro.html` `/sobre.html` (páginas estáticas)
- `/api/login` `/api/logout`
- `/api/register/**`
- `/api/dashboard/stats`
- `/api/suporte` (POST público — ⚠️ DoS / flood de tickets sem captcha)
- `/uploads/**` (arquivos servidos como estáticos — ⚠️ comprovantes LGPD acessíveis pela UUID)
- Recursos estáticos: `/js/**` `/css/**` `/images/**` `*.png` `*.svg`

Demais endpoints caem em `anyRequest().authenticated()`.

---

## Variaveis de Ambiente

| Variavel | Default | Onde |
|---|---|---|
| `PORT` | 8080 | Railway injeta dinamicamente |
| `SPRING_PROFILES_ACTIVE` | (vazio) | `docker` no `docker-compose.yml` |
| `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:8080` | suporta wildcards (`https://*.up.railway.app`) |
| `JAVA_OPTS` | `""` | passado para `java $JAVA_OPTS -jar app.jar` |

⚠️ Faltando externalizar (ver `analiseComplexa.md` §4.3 S3 e §8.3 B2): `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `PGADMIN_DEFAULT_PASSWORD`, `app.bootstrap.super-admin.password`.

---

## Comandos Docker

| Acao | Comando |
|---|---|
| Iniciar | `docker compose up -d` |
| Iniciar com rebuild | `docker compose up --build -d` |
| Rebuild apenas o app (cache busting) | `docker compose build --no-cache app && docker compose up -d app --force-recreate` |
| Parar | `docker compose down` |
| Parar + limpar volumes (re-seed do DB) | `docker compose down -v` |
| Ver logs | `docker compose logs -f app` |
| Ver status | `docker compose ps` |
| Acessar shell do app | `docker compose exec app sh` |
| Acessar psql | `docker compose exec db psql -U doatec_user -d doatec` |

---

## Convenções e Observações

- **Idioma**: rotas misturam PT-BR e EN — `/api/donations` vs `/api/solicitacoes` vs `/api/notificacoes`. A análise complexa sugere padronizar em PT-BR (`/api/doacoes`).
- **Verbos HTTP**: alguns endpoints usam PUT para ações não-idempotentes (ex.: `/responder` deveria ser POST).
- **Paginação**: apenas `AdminController` e `SuperAdminController` usam `Page<>+Pageable`; demais listagens retornam `List` cru.
- **Ownership**: validações de "dono vs admin" estão inline no controller (replicadas ~6×) — não há `@PreAuthorize` em nenhum método.
- **DELETE**: a maioria devolve 204, exceto `/api/notificacoes/{id}` (devolve 200) — inconsistência registrada na análise (API13).
- **CSRF**: desabilitado globalmente (`SecurityConfig.java:40`); `apiFetch` no front já envia `X-XSRF-TOKEN` mas o servidor ignora. Reabilitar é o item nº 1 da §11.1 do `analiseComplexa.md`.

---

## Referencias

- Mapa técnico completo: `contexto.md` (1331 linhas, com seção 99 = snapshot da análise de 2026-05-28)
- Análise multidimensional: `analiseComplexa.md` (14 seções, 46 itens no plano de ação)
- Mermaid (fluxos): `mermaid.md`
- Plano de desenvolvimento: `plan.md`
