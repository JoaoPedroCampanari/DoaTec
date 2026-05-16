package com.doatec.controller;

import com.doatec.dto.request.DoacaoRequest;
import com.doatec.dto.response.DoacaoResponse;
import com.doatec.exception.BusinessException;
import com.doatec.model.donation.Doacao;
import com.doatec.model.donation.StatusDoacao;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.Pessoa;
import com.doatec.service.DoacaoService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DoacaoController")
class DoacaoControllerTest {

    @Mock
    private DoacaoService doacaoService;

    @InjectMocks
    private DoacaoController doacaoController;

    private Pessoa doador;
    private Doacao doacao;

    @BeforeEach
    void setUp() {
        doador = new DoadorPF("123.456.789-00");
        doador.setId(1);
        doador.setNome("Doador Teste");
        doador.setEmail("doador@teste.com");
        doador.setAtivo(true);

        doacao = Doacao.builder()
                .id(10)
                .doador(doador)
                .dataDoacao(LocalDate.now())
                .status(StatusDoacao.EM_TRIAGEM)
                .descricaoGeral("Notebook usado")
                .build();
    }

    @Nested
    @DisplayName("createDonation endpoint")
    class CreateDonationTests {

        @Test
        @DisplayName("POST retorna 201 com DoacaoResponse")
        void criaDoacaoComSucesso() {
            DoacaoRequest request = DoacaoRequest.builder()
                    .nome("Doador Teste")
                    .email("doador@teste.com")
                    .tipoDocumento("cpf")
                    .numeroDocumento("123.456.789-00")
                    .descricaoGeral("Notebook usado")
                    .build();

            when(doacaoService.registrarDoacao(any(DoacaoRequest.class))).thenReturn(doacao);

            ResponseEntity<DoacaoResponse> responseEntity = doacaoController.createDonation(request);

            assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
            assertNotNull(responseEntity.getBody());
            assertEquals(10, responseEntity.getBody().id());
            assertEquals("Notebook usado", responseEntity.getBody().descricaoGeral());

            verify(doacaoService).registrarDoacao(any(DoacaoRequest.class));
        }
    }

    @Nested
    @DisplayName("excluir endpoint")
    class ExcluirTests {

        @Test
        @DisplayName("DELETE com ownership correto retorna 204")
        void excluirComOwnership() {
            User userDetails = new User("doador@teste.com", "", List.of());

            doNothing().when(doacaoService).excluirDoacao(10, "doador@teste.com");

            ResponseEntity<Void> responseEntity = doacaoController.excluir(10, userDetails);

            assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
            verify(doacaoService).excluirDoacao(10, "doador@teste.com");
        }

        @Test
        @DisplayName("DELETE sem ownership lanca BusinessException")
        void excluirSemOwnership() {
            User userDetails = new User("outro@teste.com", "", List.of());

            doThrow(new BusinessException("Voce so pode excluir suas proprias doacoes."))
                    .when(doacaoService).excluirDoacao(10, "outro@teste.com");

            assertThrows(BusinessException.class, () -> doacaoController.excluir(10, userDetails));
            verify(doacaoService).excluirDoacao(10, "outro@teste.com");
        }
    }
}
