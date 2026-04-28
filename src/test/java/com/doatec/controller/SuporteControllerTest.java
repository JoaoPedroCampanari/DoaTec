package com.doatec.controller;

import com.doatec.dto.response.SuporteResponse;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.Pessoa;
import com.doatec.repository.PessoaRepository;
import com.doatec.service.SuporteFormularioService;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SuporteController - Meus Tickets")
class SuporteControllerTest {

    @Mock
    private SuporteFormularioService suporteService;

    @Mock
    private PessoaRepository pessoaRepository;

    @InjectMocks
    private SuporteController suporteController;

    private Pessoa autor;

    @BeforeEach
    void setUp() {
        autor = new DoadorPF("123.456.789-00");
        autor.setId(1);
        autor.setNome("Usuario Teste");
        autor.setEmail("usuario@teste.com");
        autor.setAtivo(true);
    }

    @Nested
    @DisplayName("getMeusTickets endpoint")
    class GetMeusTicketsTests {

        @Test
        @DisplayName("Retorna lista de tickets do autor autenticado")
        void retornaTicketsDoAutor() {
            User userDetails = new User("usuario@teste.com", "", List.of());

            SuporteResponse responseDto = SuporteResponse.builder()
                    .id(1)
                    .autorNome("Usuario Teste")
                    .autorEmail("usuario@teste.com")
                    .assunto("Hardware solicitado")
                    .mensagem("Preciso de um notebook.")
                    .status("ABERTO")
                    .resposta("Seu pedido foi aprovado.")
                    .build();

            when(pessoaRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(autor));
            when(suporteService.getTicketsByAutorId(1)).thenReturn(List.of(responseDto));

            ResponseEntity<List<SuporteResponse>> responseEntity = suporteController.getMeusTickets(userDetails);

            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertNotNull(responseEntity.getBody());
            assertEquals(1, responseEntity.getBody().size());
            assertEquals("Hardware solicitado", responseEntity.getBody().get(0).assunto());
            assertEquals("Seu pedido foi aprovado.", responseEntity.getBody().get(0).resposta());
            verify(pessoaRepository).findByEmail("usuario@teste.com");
            verify(suporteService).getTicketsByAutorId(1);
        }

        @Test
        @DisplayName("Retorna UNAUTHORIZED quando usuario nao existe no banco")
        void retornaUnauthorizedQuandoUsuarioNaoExiste() {
            User userDetails = new User("usuario@teste.com", "", List.of());
            when(pessoaRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.empty());

            ResponseEntity<List<SuporteResponse>> responseEntity = suporteController.getMeusTickets(userDetails);

            assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        }

        @Test
        @DisplayName("Retorna lista vazia quando autor nao tem tickets")
        void retornaListaVaziaSemTickets() {
            User userDetails = new User("usuario@teste.com", "", List.of());
            when(pessoaRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(autor));
            when(suporteService.getTicketsByAutorId(1)).thenReturn(List.of());

            ResponseEntity<List<SuporteResponse>> responseEntity = suporteController.getMeusTickets(userDetails);

            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertNotNull(responseEntity.getBody());
            assertTrue(responseEntity.getBody().isEmpty());
        }
    }
}
