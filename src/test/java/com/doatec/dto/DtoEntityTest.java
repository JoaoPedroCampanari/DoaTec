package com.doatec.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import com.doatec.dto.request.AvaliacaoRequest;
import com.doatec.dto.request.RegistroRequest;
import com.doatec.dto.request.StatusUsuarioRequest;
import com.doatec.dto.response.DashboardStatsResponse;
import com.doatec.dto.response.ItemDoadoResponse;
import com.doatec.exception.ErrorResponse;
import com.doatec.model.account.DoadorPJ;
import com.doatec.model.account.Role;
import com.doatec.model.chat.ContextoChat;
import com.doatec.model.chat.MensagemChat;
import com.doatec.model.notification.Notificacao;
import com.doatec.model.notification.TipoNotificacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("DTOs e Entities - Construtores e Builders")
class DtoEntityTest {

    // ==================== ErrorResponse ====================

    @Nested
    @DisplayName("ErrorResponse")
    class ErrorResponseTests {

        @Test
        @DisplayName("Constrói ErrorResponse com todos os campos")
        void construtorCompleto() {
            LocalDateTime now = LocalDateTime.now();
            ErrorResponse error = new ErrorResponse(now, 400, "Bad Request", "Erro de validação", List.of("Campo obrigatório"));

            assertEquals(now, error.timestamp());
            assertEquals(400, error.status());
            assertEquals("Bad Request", error.error());
            assertEquals("Erro de validação", error.message());
            assertEquals(1, error.details().size());
        }

        @Test
        @DisplayName("Factory method of sem details")
        void ofSemDetails() {
            ErrorResponse error = ErrorResponse.of(404, "Not Found", "Recurso não encontrado");

            assertNotNull(error.timestamp());
            assertEquals(404, error.status());
            assertEquals("Not Found", error.error());
            assertEquals("Recurso não encontrado", error.message());
            assertNull(error.details());
        }

        @Test
        @DisplayName("Factory method of com details")
        void ofComDetails() {
            ErrorResponse error = ErrorResponse.of(400, "Bad Request", "Erro", List.of("detail1", "detail2"));

            assertNotNull(error.timestamp());
            assertEquals(2, error.details().size());
        }
    }

    // ==================== RegistroRequest ====================

    @Nested
    @DisplayName("RegistroRequest")
    class RegistroRequestTests {

        @Test
        @DisplayName("Constrói RegistroRequest com todos os campos")
        void construtorCompleto() {
            RegistroRequest request = new RegistroRequest(
                    "DOADOR_PF", "João Silva", "123.456.789-00",
                    "joao@test.com", "Rua A", "11999999999", "123456"
            );

            assertEquals("DOADOR_PF", request.tipoPessoa());
            assertEquals("João Silva", request.nome());
            assertEquals("123.456.789-00", request.documento());
            assertEquals("joao@test.com", request.email());
            assertEquals("Rua A", request.endereco());
            assertEquals("11999999999", request.telefone());
            assertEquals("123456", request.senha());
        }
    }

    // ==================== ItemDoadoResponse ====================

    @Nested
    @DisplayName("ItemDoadoResponse")
    class ItemDoadoResponseTests {

        @Test
        @DisplayName("Constrói ItemDoadoResponse com todos os campos")
        void construtorCompleto() {
            ItemDoadoResponse item = new ItemDoadoResponse("Notebook", "Notebook Dell");

            assertEquals("Notebook", item.tipoItem());
            assertEquals("Notebook Dell", item.descricao());
        }
    }

    // ==================== DashboardStatsResponse ====================

    @Nested
    @DisplayName("DashboardStatsResponse")
    class DashboardStatsResponseTests {

        @Test
        @DisplayName("Constrói DashboardStatsResponse com builder")
        void builderCompleto() {
            java.time.LocalDate hoje = java.time.LocalDate.now();
            DashboardStatsResponse stats = DashboardStatsResponse.builder()
                    .totalDonations(10L)
                    .lastDonationDate(hoje)
                    .build();

            assertEquals(10L, stats.totalDonations());
            assertEquals(hoje, stats.lastDonationDate());
        }
    }

    // ==================== AvaliacaoRequest ====================

    @Nested
    @DisplayName("AvaliacaoRequest")
    class AvaliacaoRequestTests {

        @Test
        @DisplayName("Constrói AvaliacaoRequest com observação")
        void construtorCompleto() {
            AvaliacaoRequest request = new AvaliacaoRequest("Aprovado com ressalvas");

            assertEquals("Aprovado com ressalvas", request.observacao());
        }
    }

    // ==================== StatusUsuarioRequest ====================

    @Nested
    @DisplayName("StatusUsuarioRequest")
    class StatusUsuarioRequestTests {

        @Test
        @DisplayName("Constrói StatusUsuarioRequest com ativo=true")
        void construtorAtivo() {
            StatusUsuarioRequest request = new StatusUsuarioRequest(true);

            assertTrue(request.ativo());
        }

        @Test
        @DisplayName("Constrói StatusUsuarioRequest com ativo=false")
        void construtorInativo() {
            StatusUsuarioRequest request = new StatusUsuarioRequest(false);

            assertFalse(request.ativo());
        }
    }

    // ==================== Notificacao ====================

    @Nested
    @DisplayName("Notificacao")
    class NotificacaoTests {

        @Test
        @DisplayName("Constrói Notificacao com builder")
        void builderCompleto() {
            Notificacao notificacao = Notificacao.builder()
                    .id(1)
                    .titulo("Doação Aprovada")
                    .mensagem("Sua doação foi aprovada")
                    .tipo(TipoNotificacao.DOACAO_APROVADA)
                    .entidadeRelacionadaId(10)
                    .entidadeRelacionadaTipo("DOACAO")
                    .build();

            assertEquals(1, notificacao.getId());
            assertEquals("Doação Aprovada", notificacao.getTitulo());
            assertEquals("Sua doação foi aprovada", notificacao.getMensagem());
            assertEquals(TipoNotificacao.DOACAO_APROVADA, notificacao.getTipo());
            assertEquals(10, notificacao.getEntidadeRelacionadaId());
            assertEquals("DOACAO", notificacao.getEntidadeRelacionadaTipo());
            assertFalse(notificacao.getLida());
            assertNotNull(notificacao.getDataCriacao());
        }

        @Test
        @DisplayName("OnCreate preenche dataCriacao se nula")
        void onCreatePreencheData() throws Exception {
            Notificacao notificacao = Notificacao.builder()
                    .titulo("Teste")
                    .mensagem("Mensagem")
                    .tipo(TipoNotificacao.DOACAO_APROVADA)
                    .build();

            java.lang.reflect.Method onCreate = Notificacao.class.getDeclaredMethod("onCreate");
            onCreate.setAccessible(true);
            onCreate.invoke(notificacao);

            assertNotNull(notificacao.getDataCriacao());
        }
    }

    // ==================== MensagemChat ====================

    @Nested
    @DisplayName("MensagemChat")
    class MensagemChatTests {

        @Test
        @DisplayName("Constrói MensagemChat com builder")
        void builderCompleto() {
            MensagemChat mensagem = MensagemChat.builder()
                    .id(1)
                    .conteudo("Olá, tudo bem?")
                    .referenciaId(10)
                    .contexto(ContextoChat.SUPORTE)
                    .build();

            assertEquals(1, mensagem.getId());
            assertEquals("Olá, tudo bem?", mensagem.getConteudo());
            assertEquals(10, mensagem.getReferenciaId());
            assertEquals(ContextoChat.SUPORTE, mensagem.getContexto());
        }

        @Test
        @DisplayName("OnCreate preenche dataEnvio")
        void onCreatePreencheData() throws Exception {
            MensagemChat mensagem = MensagemChat.builder()
                    .conteudo("Teste")
                    .referenciaId(1)
                    .contexto(ContextoChat.DOACAO)
                    .build();

            java.lang.reflect.Method onCreate = MensagemChat.class.getDeclaredMethod("onCreate");
            onCreate.setAccessible(true);
            onCreate.invoke(mensagem);

            assertNotNull(mensagem.getDataEnvio());
        }
    }

    // ==================== DoadorPJ ====================

    @Nested
    @DisplayName("DoadorPJ")
    class DoadorPJTests {

        @Test
        @DisplayName("Constrói DoadorPJ com builder")
        void builderCompleto() {
            DoadorPJ doador = DoadorPJ.builder()
                    .id(1)
                    .nome("Empresa LTDA")
                    .email("empresa@test.com")
                    .cnpj("12.345.678/0001-90")
                    .razaoSocial("Empresa LTDA")
                    .role(Role.USER)
                    .ativo(true)
                    .build();

            assertEquals(1, doador.getId());
            assertEquals("Empresa LTDA", doador.getNome());
            assertEquals("empresa@test.com", doador.getEmail());
            assertEquals("12.345.678/0001-90", doador.getCnpj());
            assertEquals("Empresa LTDA", doador.getRazaoSocial());
            assertEquals(Role.USER, doador.getRole());
            assertTrue(doador.getAtivo());
        }

        @Test
        @DisplayName("getDocumento retorna CNPJ")
        void getDocumentoRetornaCnpj() {
            DoadorPJ doador = DoadorPJ.builder()
                    .cnpj("12.345.678/0001-90")
                    .build();

            assertEquals("12.345.678/0001-90", doador.getDocumento());
        }

        @Test
        @DisplayName("getTipoPessoa retorna DOADOR_PJ")
        void getTipoPessoaRetornaDoadorPJ() {
            DoadorPJ doador = DoadorPJ.builder().build();

            assertEquals("DOADOR_PJ", doador.getTipoPessoa());
        }
    }
}
