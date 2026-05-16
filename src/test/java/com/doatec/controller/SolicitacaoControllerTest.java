package com.doatec.controller;

import com.doatec.dto.request.SolicitacaoRequest;
import com.doatec.dto.response.SolicitacaoResponse;
import com.doatec.exception.BusinessException;
import com.doatec.model.account.Aluno;
import com.doatec.model.account.Pessoa;
import com.doatec.model.solicitacao.SolicitacaoHardware;
import com.doatec.model.solicitacao.StatusSolicitacao;
import com.doatec.service.SolicitacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SolicitacaoController")
class SolicitacaoControllerTest {

    @Mock
    private SolicitacaoService solicitacaoService;

    @InjectMocks
    private SolicitacaoController solicitacaoController;

    private Pessoa aluno;
    private SolicitacaoHardware solicitacao;

    @BeforeEach
    void setUp() {
        aluno = new Aluno("2023001");
        aluno.setId(1);
        aluno.setNome("Aluno Teste");
        aluno.setEmail("aluno@teste.com");
        aluno.setAtivo(true);

        solicitacao = SolicitacaoHardware.builder()
                .id(5)
                .aluno(aluno)
                .dataSolicitacao(LocalDate.now())
                .status(StatusSolicitacao.EM_ANALISE)
                .justificativa("Preciso para aulas de programacao")
                .preferenciaEquipamento("Notebook")
                .build();
    }

    @Nested
    @DisplayName("criar endpoint")
    class CriarTests {

        @Test
        @DisplayName("POST retorna 201 com SolicitacaoResponse")
        void criaSolicitacaoComSucesso() {
            User userDetails = new User("aluno@teste.com", "", List.of());
            SolicitacaoRequest request = new SolicitacaoRequest(
                    "Aluno Teste", "aluno@teste.com", null, "2023001",
                    "Preciso para aulas de programacao", "Notebook");

            when(solicitacaoService.criarSolicitacao(eq("aluno@teste.com"), any(SolicitacaoRequest.class)))
                    .thenReturn(solicitacao);

            ResponseEntity<SolicitacaoResponse> responseEntity =
                    solicitacaoController.criar(userDetails, request);

            assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
            assertNotNull(responseEntity.getBody());
            assertEquals(5, responseEntity.getBody().id());
            assertEquals("Aluno Teste", responseEntity.getBody().alunoNome());
            assertEquals("EM_ANALISE", responseEntity.getBody().status());

            verify(solicitacaoService).criarSolicitacao(eq("aluno@teste.com"), any(SolicitacaoRequest.class));
        }

        @Test
        @DisplayName("POST com erro de negocio retorna 400")
        void criaSolicitacaoComErro() {
            User userDetails = new User("aluno@teste.com", "", List.of());
            SolicitacaoRequest request = new SolicitacaoRequest(
                    "Aluno Teste", "aluno@teste.com", null, "2023001",
                    "Justificativa", "Notebook");

            when(solicitacaoService.criarSolicitacao(eq("aluno@teste.com"), any(SolicitacaoRequest.class)))
                    .thenThrow(new BusinessException("Apenas alunos podem fazer solicitacoes de hardware."));

            ResponseEntity<SolicitacaoResponse> responseEntity =
                    solicitacaoController.criar(userDetails, request);

            assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        }
    }

    @Nested
    @DisplayName("excluir endpoint")
    class ExcluirTests {

        @Test
        @DisplayName("DELETE retorna 204 com ownership valido")
        void excluirComOwnership() {
            User userDetails = new User("aluno@teste.com", "", List.of());

            doNothing().when(solicitacaoService).excluirSolicitacao(5, "aluno@teste.com");

            ResponseEntity<Void> responseEntity = solicitacaoController.excluir(5, userDetails);

            assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
            verify(solicitacaoService).excluirSolicitacao(5, "aluno@teste.com");
        }

        @Test
        @DisplayName("DELETE sem ownership lanca BusinessException")
        void excluirSemOwnership() {
            User userDetails = new User("outro@teste.com", "", List.of());

            doThrow(new BusinessException("Voce so pode excluir suas proprias solicitacoes."))
                    .when(solicitacaoService).excluirSolicitacao(5, "outro@teste.com");

            assertThrows(BusinessException.class,
                    () -> solicitacaoController.excluir(5, userDetails));
        }
    }
}
