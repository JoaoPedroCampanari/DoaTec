package com.doatec.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integracao - Seguranca")
class SecurityIntegrationTest extends BaseIntegrationTest {

    // ==================== HELPERS ====================

    private String alunoPayload(String nome, String email, String senha, String ra) {
        return """
                {
                    "nome": "%s",
                    "email": "%s",
                    "senha": "%s",
                    "ra": "%s",
                    "telefone": "11999999999",
                    "cep": "12345-678",
                    "logradouro": "Rua Teste",
                    "numero": "100",
                    "bairro": "Centro",
                    "cidade": "Sao Paulo",
                    "estado": "SP"
                }
                """.formatted(nome, email, senha, ra);
    }

    private String doadorPFPayload(String nome, String email, String senha, String cpf) {
        return """
                {
                    "nome": "%s",
                    "email": "%s",
                    "senha": "%s",
                    "cpf": "%s",
                    "telefone": "11999999999",
                    "cep": "12345-678",
                    "logradouro": "Rua Teste",
                    "numero": "100",
                    "bairro": "Centro",
                    "cidade": "Sao Paulo",
                    "estado": "SP"
                }
                """.formatted(nome, email, senha, cpf);
    }

    private String doadorPJPayload(String razaoSocial, String nomeRep, String email, String senha, String cnpj) {
        return """
                {
                    "razaoSocial": "%s",
                    "nomeRepresentante": "%s",
                    "email": "%s",
                    "senha": "%s",
                    "cnpj": "%s",
                    "telefone": "11999999999",
                    "cep": "12345-678",
                    "logradouro": "Rua Teste",
                    "numero": "100",
                    "bairro": "Centro",
                    "cidade": "Sao Paulo",
                    "estado": "SP"
                }
                """.formatted(razaoSocial, nomeRep, email, senha, cnpj);
    }

    private String suportePayload(String nome, String email, String assunto, String mensagem) {
        return """
                {
                    "nome": "%s",
                    "email": "%s",
                    "assunto": "%s",
                    "mensagem": "%s"
                }
                """.formatted(nome, email, assunto, mensagem);
    }

    // ==================== ENDPOINTS PUBLICOS ====================

    @Nested
    @DisplayName("Endpoints publicos (sem autenticacao)")
    class EndpointsPublicos {

        @Test
        @DisplayName("GET /api/dashboard/stats deve retornar 200")
        void dashboardStats_deveRetornar200() throws Exception {
            mockMvc.perform(get("/api/dashboard/stats"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /api/register/aluno deve retornar 201")
        void registrarAluno_deveRetornar201() throws Exception {
            String body = alunoPayload(
                    "Novo Aluno", "novo.aluno@test.com", "123456", "RA123456"
            );
            mockMvc.perform(post("/api/register/aluno")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("POST /api/register/doador-pf deve retornar 201")
        void registrarDoadorPF_deveRetornar201() throws Exception {
            String body = doadorPFPayload(
                    "Novo Doador PF", "novo.doadorpf@test.com", "123456", "529.982.247-25"
            );
            mockMvc.perform(post("/api/register/doador-pf")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("POST /api/register/doador-pj deve retornar 201")
        void registrarDoadorPJ_deveRetornar201() throws Exception {
            String body = doadorPJPayload(
                    "Empresa Teste LTDA", "Representante Teste",
                    "novo.doadorpj@test.com", "123456", "11.222.333/0001-81"
            );
            mockMvc.perform(post("/api/register/doador-pj")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("POST /api/suporte deve retornar 201")
        void enviarSuporte_deveRetornar201() throws Exception {
            // Registrar um usuario primeiro (suporte exige usuario cadastrado)
            String registro = alunoPayload(
                    "Suporte User", "suporte.user@test.com", "123456", "RASUPORT"
            );
            mockMvc.perform(post("/api/register/aluno")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registro))
                    .andExpect(status().isCreated());

            // Enviar ticket de suporte com o email/nome do usuario registrado
            String body = suportePayload(
                    "Suporte User", "suporte.user@test.com", "Duvida", "Preciso de ajuda com o sistema"
            );
            mockMvc.perform(post("/api/suporte")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated());
        }
    }

    // ==================== ENDPOINTS PROTEGIDOS ====================

    @Nested
    @DisplayName("Endpoints protegidos (sem autenticacao)")
    class EndpointsProtegidos {

        @Test
        @DisplayName("GET /api/users/me sem sessao deve retornar 401")
        void getMe_semSessao_deveRetornar401() throws Exception {
            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/donations sem sessao deve retornar 401")
        void criarDoacao_semSessao_deveRetornar401() throws Exception {
            mockMvc.perform(post("/api/donations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/notificacoes sem sessao deve retornar 401")
        void listarNotificacoes_semSessao_deveRetornar401() throws Exception {
            mockMvc.perform(get("/api/notificacoes"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/solicitacoes sem sessao deve retornar 401")
        void criarSolicitacao_semSessao_deveRetornar401() throws Exception {
            mockMvc.perform(post("/api/solicitacoes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== HIERARQUIA DE ROLES ====================

    @Nested
    @DisplayName("Hierarquia de roles")
    class HierarquiaRoles {

        @Test
        @DisplayName("USER nao pode acessar GET /api/admin/dashboard (403)")
        void user_naoPodeAcessarAdminDashboard() throws Exception {
            mockMvc.perform(get("/api/admin/dashboard")
                            .with(SecurityMockMvcRequestPostProcessors.user("user@test.com").roles("USER")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("USER nao pode acessar GET /api/super-admin/admins (403)")
        void user_naoPodeAcessarSuperAdminAdmins() throws Exception {
            mockMvc.perform(get("/api/super-admin/admins")
                            .with(SecurityMockMvcRequestPostProcessors.user("user@test.com").roles("USER")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ADMIN pode acessar GET /api/admin/dashboard (200)")
        void admin_podeAcessarAdminDashboard() throws Exception {
            mockMvc.perform(get("/api/admin/dashboard")
                            .with(SecurityMockMvcRequestPostProcessors.user("admin@test.com").roles("ADMIN")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ADMIN nao pode acessar GET /api/super-admin/admins (403)")
        void admin_naoPodeAcessarSuperAdminAdmins() throws Exception {
            mockMvc.perform(get("/api/super-admin/admins")
                            .with(SecurityMockMvcRequestPostProcessors.user("admin@test.com").roles("ADMIN")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("SUPER_ADMIN pode acessar GET /api/super-admin/admins (200)")
        void superAdmin_podeAcessarSuperAdminAdmins() throws Exception {
            mockMvc.perform(get("/api/super-admin/admins")
                            .with(SecurityMockMvcRequestPostProcessors.user("superadmin@test.com").roles("SUPER_ADMIN")))
                    .andExpect(status().isOk());
        }
    }
}
