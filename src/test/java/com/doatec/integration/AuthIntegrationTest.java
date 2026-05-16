package com.doatec.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Integracao - Autenticacao")
class AuthIntegrationTest extends BaseIntegrationTest {

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

    private String loginPayload(String email, String senha) {
        return """
                {
                    "email": "%s",
                    "senha": "%s"
                }
                """.formatted(email, senha);
    }

    // ==================== REGISTRO ====================

    @Nested
    @DisplayName("POST /api/register/aluno")
    class RegistroAluno {

        @Test
        @DisplayName("registro com sucesso retorna 201")
        void registroComSucesso_retorna201() throws Exception {
            String payload = alunoPayload(
                    "Aluno Teste", "aluno.int@test.com", "123456", "RA123456"
            );

            mockMvc.perform(post("/api/register/aluno")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.nome").value("Aluno Teste"))
                    .andExpect(jsonPath("$.email").value("aluno.int@test.com"));
        }

        @Test
        @DisplayName("email duplicado retorna 400")
        void emailDuplicado_retorna400() throws Exception {
            // Primeiro registro
            String payload = alunoPayload(
                    "Aluno Dup", "dup@test.com", "123456", "RADUP001"
            );
            mockMvc.perform(post("/api/register/aluno")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isCreated());

            // Segundo registro com mesmo email
            String payloadDup = alunoPayload(
                    "Aluno Dup2", "dup@test.com", "123456", "RADUP002"
            );
            mockMvc.perform(post("/api/register/aluno")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payloadDup))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("campos obrigatorios faltando retorna 400")
        void camposObrigatoriosFaltando_retorna400() throws Exception {
            // Payload sem nome, email, senha e ra
            String payload = """
                    {
                        "telefone": "11999999999"
                    }
                    """;

            mockMvc.perform(post("/api/register/aluno")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== LOGIN ====================

    @Nested
    @DisplayName("POST /api/login")
    class Login {

        @Test
        @DisplayName("login com sucesso retorna 200 e dados do usuario")
        void loginComSucesso_retorna200() throws Exception {
            // Registra aluno primeiro
            String registro = alunoPayload(
                    "Login User", "login@test.com", "123456", "RALOGIN1"
            );
            mockMvc.perform(post("/api/register/aluno")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registro))
                    .andExpect(status().isCreated());

            // Faz login
            String login = loginPayload("login@test.com", "123456");

            mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(login))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("login@test.com"))
                    .andExpect(jsonPath("$.nome").value("Login User"));
        }

        @Test
        @DisplayName("credenciais invalidas retorna 401")
        void credenciaisInvalidas_retorna401() throws Exception {
            // Registra aluno
            String registro = alunoPayload(
                    "Senha User", "senha@test.com", "123456", "RASENHA1"
            );
            mockMvc.perform(post("/api/register/aluno")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registro))
                    .andExpect(status().isCreated());

            // Login com senha errada
            String login = loginPayload("senha@test.com", "senhaErrada");

            mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(login))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== SESSAO ====================

    @Nested
    @DisplayName("GET /api/users/me")
    class Sessao {

        @Test
        @DisplayName("com sessao valida retorna 200 e dados do usuario")
        void sessaoValida_retorna200() throws Exception {
            // Registra e faz login
            String registro = alunoPayload(
                    "Me User", "me@test.com", "123456", "RAMEUSER"
            );
            mockMvc.perform(post("/api/register/aluno")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registro))
                    .andExpect(status().isCreated());

            // Usa SecurityMockMvcRequestPostProcessors para simular autenticação
            mockMvc.perform(get("/api/users/me")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("me@test.com").roles("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("me@test.com"))
                    .andExpect(jsonPath("$.nome").value("Me User"));
        }

        @Test
        @DisplayName("sem sessao retorna 401")
        void semSessao_retorna401() throws Exception {
            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
