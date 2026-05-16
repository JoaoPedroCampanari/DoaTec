package com.doatec.integration;

import com.doatec.model.account.DoadorPF;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Integração - Doações")
class DoacaoIntegrationTest extends BaseIntegrationTest {

    private DoadorPF doador;

    @BeforeEach
    void setUp() {
        // Usar email único por teste para evitar conflitos de CPF
        String uniqueEmail = "doador-" + System.nanoTime() + "@test.com";
        doador = criarDoadorPF(uniqueEmail, "Doador Integração");
    }

    // ==================== HELPERS ====================

    private String doacaoJson() {
        return """
                {
                    "nome": "Doador Integração",
                    "tipoDocumento": "cpf",
                    "numeroDocumento": "%s",
                    "email": "%s",
                    "descricaoGeral": "Computador desktop para doação",
                    "preferenciaEntrega": "PONTO_DE_COLETA"
                }
                """.formatted(doador.getCpf(), doador.getEmail());
    }

    private String doacaoJsonComEmail(String email) {
        return """
                {
                    "nome": "Doador Integração",
                    "tipoDocumento": "cpf",
                    "numeroDocumento": "%s",
                    "email": "%s",
                    "descricaoGeral": "Computador desktop para doação"
                }
                """.formatted(doador.getCpf(), email);
    }

    private int criarDoacaoViaApi() throws Exception {
        String response = mockMvc.perform(post("/api/donations")
                        .with(SecurityMockMvcRequestPostProcessors.user(doador.getEmail()).roles("USER"))
                        .contentType("application/json")
                        .content(doacaoJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("EM_TRIAGEM"))
                .andReturn().getResponse().getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        return node.get("id").asInt();
    }

    // ==================== FLUXO COMPLETO DE DOAÇÃO ====================

    @Nested
    @DisplayName("Fluxo completo de doação")
    class FluxoCompletoTests {

        @Test
        @DisplayName("POST /api/donations - criar doação retorna 201")
        void criarDoacao_retorna201() throws Exception {
            mockMvc.perform(post("/api/donations")
                            .with(SecurityMockMvcRequestPostProcessors.user(doador.getEmail()).roles("USER"))
                            .contentType("application/json")
                            .content(doacaoJson()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.doadorNome").value("Doador Integração"))
                    .andExpect(jsonPath("$.doadorEmail").value(doador.getEmail()))
                    .andExpect(jsonPath("$.status").value("EM_TRIAGEM"))
                    .andExpect(jsonPath("$.descricaoGeral").value("Computador desktop para doação"))
                    .andExpect(jsonPath("$.preferenciaEntrega").value("PONTO_DE_COLETA"));
        }

        @Test
        @DisplayName("GET /api/users/me/donations - lista doações do usuário logado")
        void listarMinhasDoacoes_retorna200() throws Exception {
            criarDoacaoViaApi();

            mockMvc.perform(get("/api/users/me/donations")
                            .with(SecurityMockMvcRequestPostProcessors.user(doador.getEmail()).roles("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].doadorEmail").value(doador.getEmail()))
                    .andExpect(jsonPath("$[0].status").value("EM_TRIAGEM"));
        }

        @Test
        @DisplayName("DELETE /api/donations/{id} - excluir doação em triagem retorna 204")
        void excluirDoacao_retorna204() throws Exception {
            int doacaoId = criarDoacaoViaApi();

            mockMvc.perform(delete("/api/donations/" + doacaoId)
                            .with(SecurityMockMvcRequestPostProcessors.user(doador.getEmail()).roles("USER")))
                    .andExpect(status().isNoContent());

            // Verificar que a doação foi removida da listagem
            mockMvc.perform(get("/api/users/me/donations")
                            .with(SecurityMockMvcRequestPostProcessors.user(doador.getEmail()).roles("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ==================== FLUXO DE APROVAÇÃO ====================

    @Nested
    @DisplayName("Fluxo de aprovação de doação")
    class FluxoAprovacaoTests {

        @Test
        @DisplayName("PUT /api/admin/doacoes/{id}/aprovar - admin aprova doação retorna 200 com status FINALIZADO")
        void aprovarDoacao_retorna200() throws Exception {
            int doacaoId = criarDoacaoViaApi();

            DoadorPF admin = criarAdmin("admin-integ@test.com", "Admin Integração");

            // Mover doação para EM_ANALISE primeiro (EM_TRIAGEM -> AGUARDANDO_COLETA -> RECEBIDO -> EM_ANALISE)
            mockMvc.perform(put("/api/admin/doacoes/" + doacaoId + "/status")
                            .with(SecurityMockMvcRequestPostProcessors.user("admin-integ@test.com").roles("ADMIN"))
                            .param("novoStatus", "AGUARDANDO_COLETA"))
                    .andExpect(status().isOk());

            mockMvc.perform(put("/api/admin/doacoes/" + doacaoId + "/status")
                            .with(SecurityMockMvcRequestPostProcessors.user("admin-integ@test.com").roles("ADMIN"))
                            .param("novoStatus", "RECEBIDO"))
                    .andExpect(status().isOk());

            mockMvc.perform(put("/api/admin/doacoes/" + doacaoId + "/status")
                            .with(SecurityMockMvcRequestPostProcessors.user("admin-integ@test.com").roles("ADMIN"))
                            .param("novoStatus", "EM_ANALISE"))
                    .andExpect(status().isOk());

            // Agora pode aprovar (EM_ANALISE -> FINALIZADO)
            mockMvc.perform(put("/api/admin/doacoes/" + doacaoId + "/aprovar")
                            .with(SecurityMockMvcRequestPostProcessors.user("admin-integ@test.com").roles("ADMIN"))
                            .contentType("application/json")
                            .content("{\"observacao\": \"Doação aprovada com sucesso\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(doacaoId))
                    .andExpect(jsonPath("$.status").value("FINALIZADO"))
                    .andExpect(jsonPath("$.observacaoAdmin").value("Doação aprovada com sucesso"));
        }
    }

    // ==================== VALIDAÇÕES ====================

    @Nested
    @DisplayName("Validações de doação")
    class ValidacoesTests {

        @Test
        @DisplayName("POST /api/donations - email não cadastrado retorna 400")
        void criarDoacao_emailNaoCadastrado_retorna400() throws Exception {
            mockMvc.perform(post("/api/donations")
                            .with(SecurityMockMvcRequestPostProcessors.user(doador.getEmail()).roles("USER"))
                            .contentType("application/json")
                            .content(doacaoJsonComEmail("inexistente@test.com")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("DELETE /api/donations/{id} - doação de outro usuário é rejeitado")
        void excluirDoacao_deOutroUsuario_lancaExcecao() throws Exception {
            int doacaoId = criarDoacaoViaApi();

            // Criar outro doador com email único
            String outroEmail = "outro-" + System.nanoTime() + "@test.com";
            criarDoadorPF(outroEmail, "Outro Doador");

            mockMvc.perform(delete("/api/donations/" + doacaoId)
                            .with(SecurityMockMvcRequestPostProcessors.user(outroEmail).roles("USER")))
                    .andExpect(status().isBadRequest());
        }
    }
}
