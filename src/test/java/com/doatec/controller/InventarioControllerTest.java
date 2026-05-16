package com.doatec.controller;

import com.doatec.dto.response.EquipamentoResponse;
import com.doatec.dto.response.SugestaoMatchingResponse;
import com.doatec.dto.response.SugestaoMatchingResponse.MatchEquipamentoResponse;
import com.doatec.model.account.Pessoa;
import com.doatec.model.inventory.StatusEquipamento;
import com.doatec.repository.PessoaRepository;
import com.doatec.service.InventarioService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventarioController")
class InventarioControllerTest {

    @Mock
    private InventarioService inventarioService;

    @Mock
    private PessoaRepository pessoaRepository;

    @InjectMocks
    private InventarioController controller;

    private Pessoa admin;
    private User userDetails;
    private EquipamentoResponse equipamentoResponse;
    private SugestaoMatchingResponse sugestaoResponse;

    @BeforeEach
    void setUp() {
        admin = mock(Pessoa.class);
        lenient().when(admin.getId()).thenReturn(99);

        userDetails = new User("admin@teste.com", "", List.of());

        equipamentoResponse = EquipamentoResponse.builder()
                .id(1)
                .tipo("Notebook")
                .descricao("Notebook Dell Latitude")
                .status("DISPONIVEL")
                .estadoConservacao("Bom")
                .doadorOrigem("Joao Silva")
                .solicitacaoDestinoId(null)
                .alunoDestinatarioId(null)
                .dataEntradaInventario(LocalDateTime.of(2026, 5, 1, 10, 0))
                .dataAtribuicao(null)
                .build();

        MatchEquipamentoResponse match = MatchEquipamentoResponse.builder()
                .equipamentoId(1)
                .tipo("Notebook")
                .descricao("Notebook Dell Latitude")
                .estadoConservacao("Bom")
                .scoreCompatibilidade(95)
                .build();

        sugestaoResponse = SugestaoMatchingResponse.builder()
                .solicitacaoId(10)
                .alunoNome("Maria Santos")
                .alunoEmail("maria@teste.com")
                .preferenciaEquipamento("Notebook")
                .equipamentosCompativeis(List.of(match))
                .build();
    }

    // ----------------------------------------------------------------
    // GET /api/admin/inventario
    // ----------------------------------------------------------------

    @Nested
    @DisplayName("listarEquipamentos")
    class ListarEquipamentosTests {

        @Test
        @DisplayName("retorna 200 com lista de equipamentos sem filtro")
        void listarSemFiltro() {
            when(inventarioService.listarEquipamentos(null))
                    .thenReturn(List.of(equipamentoResponse));

            ResponseEntity<List<EquipamentoResponse>> response =
                    controller.listarEquipamentos(null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertEquals("Notebook", response.getBody().get(0).tipo());
            verify(inventarioService).listarEquipamentos(null);
        }

        @Test
        @DisplayName("retorna 200 com lista filtrada por status")
        void listarComFiltroStatus() {
            when(inventarioService.listarEquipamentos(StatusEquipamento.DISPONIVEL))
                    .thenReturn(List.of(equipamentoResponse));

            ResponseEntity<List<EquipamentoResponse>> response =
                    controller.listarEquipamentos(StatusEquipamento.DISPONIVEL);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertEquals("DISPONIVEL", response.getBody().get(0).status());
            verify(inventarioService).listarEquipamentos(StatusEquipamento.DISPONIVEL);
        }

        @Test
        @DisplayName("retorna 200 com lista vazia quando nenhum equipamento existe")
        void listarVazio() {
            when(inventarioService.listarEquipamentos(null))
                    .thenReturn(List.of());

            ResponseEntity<List<EquipamentoResponse>> response =
                    controller.listarEquipamentos(null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
        }
    }

    // ----------------------------------------------------------------
    // GET /api/admin/inventario/{id}
    // ----------------------------------------------------------------

    @Nested
    @DisplayName("buscarEquipamento")
    class BuscarEquipamentoTests {

        @Test
        @DisplayName("retorna 200 com o equipamento encontrado")
        void buscarPorIdExistente() {
            when(inventarioService.buscarPorId(1)).thenReturn(equipamentoResponse);

            ResponseEntity<EquipamentoResponse> response =
                    controller.buscarEquipamento(1);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().id());
            assertEquals("Notebook", response.getBody().tipo());
            verify(inventarioService).buscarPorId(1);
        }

        @Test
        @DisplayName("propaga excecao quando equipamento nao existe")
        void buscarPorIdInexistente() {
            when(inventarioService.buscarPorId(999))
                    .thenThrow(new RuntimeException("Equipamento não encontrado"));

            assertThrows(RuntimeException.class,
                    () -> controller.buscarEquipamento(999));
            verify(inventarioService).buscarPorId(999);
        }
    }

    // ----------------------------------------------------------------
    // GET /api/admin/inventario/disponiveis
    // ----------------------------------------------------------------

    @Nested
    @DisplayName("buscarDisponiveis")
    class BuscarDisponiveisTests {

        @Test
        @DisplayName("retorna 200 com equipamentos disponiveis sem preferencia")
        void buscarSemPreferencia() {
            when(inventarioService.buscarDisponiveisPorPreferencia(null))
                    .thenReturn(List.of(equipamentoResponse));

            ResponseEntity<List<EquipamentoResponse>> response =
                    controller.buscarDisponiveis(null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            verify(inventarioService).buscarDisponiveisPorPreferencia(null);
        }

        @Test
        @DisplayName("retorna 200 com equipamentos filtrados por preferencia")
        void buscarComPreferencia() {
            when(inventarioService.buscarDisponiveisPorPreferencia("Notebook"))
                    .thenReturn(List.of(equipamentoResponse));

            ResponseEntity<List<EquipamentoResponse>> response =
                    controller.buscarDisponiveis("Notebook");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertEquals("Notebook", response.getBody().get(0).tipo());
            verify(inventarioService).buscarDisponiveisPorPreferencia("Notebook");
        }

        @Test
        @DisplayName("retorna 200 com lista vazia quando nao ha disponiveis")
        void buscarVazio() {
            when(inventarioService.buscarDisponiveisPorPreferencia("Impressora"))
                    .thenReturn(List.of());

            ResponseEntity<List<EquipamentoResponse>> response =
                    controller.buscarDisponiveis("Impressora");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
        }
    }

    // ----------------------------------------------------------------
    // GET /api/admin/inventario/sugestoes/{solicitacaoId}
    // ----------------------------------------------------------------

    @Nested
    @DisplayName("sugerirMatchings")
    class SugerirMatchingsTests {

        @Test
        @DisplayName("retorna 200 com sugestoes de matching")
        void sugerirComSucesso() {
            when(inventarioService.sugerirMatchings(10)).thenReturn(sugestaoResponse);

            ResponseEntity<SugestaoMatchingResponse> response =
                    controller.sugerirMatchings(10);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(10, response.getBody().solicitacaoId());
            assertEquals("Maria Santos", response.getBody().alunoNome());
            assertEquals(1, response.getBody().equipamentosCompativeis().size());
            assertEquals(95, response.getBody().equipamentosCompativeis().get(0).scoreCompatibilidade());
            verify(inventarioService).sugerirMatchings(10);
        }

        @Test
        @DisplayName("propaga excecao quando solicitacao nao existe")
        void sugerirSolicitacaoInexistente() {
            when(inventarioService.sugerirMatchings(999))
                    .thenThrow(new RuntimeException("Solicitação não encontrada"));

            assertThrows(RuntimeException.class,
                    () -> controller.sugerirMatchings(999));
            verify(inventarioService).sugerirMatchings(999);
        }
    }

    // ----------------------------------------------------------------
    // POST /api/admin/inventario/{equipamentoId}/atribuir/{solicitacaoId}
    // ----------------------------------------------------------------

    @Nested
    @DisplayName("atribuirEquipamento")
    class AtribuirEquipamentoTests {

        @Test
        @DisplayName("retorna 200 com equipamento reservado")
        void atribuirComSucesso() {
            EquipamentoResponse reservado = EquipamentoResponse.builder()
                    .id(1)
                    .tipo("Notebook")
                    .descricao("Notebook Dell Latitude")
                    .status("RESERVADO")
                    .estadoConservacao("Bom")
                    .solicitacaoDestinoId(10)
                    .alunoDestinatarioId(5)
                    .dataEntradaInventario(LocalDateTime.of(2026, 5, 1, 10, 0))
                    .dataAtribuicao(LocalDateTime.of(2026, 5, 16, 14, 30))
                    .build();

            when(pessoaRepository.findByEmail("admin@teste.com"))
                    .thenReturn(Optional.of(admin));
            when(inventarioService.atribuirEquipamento(1, 10, 99))
                    .thenReturn(reservado);

            ResponseEntity<EquipamentoResponse> response =
                    controller.atribuirEquipamento(1, 10, userDetails);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("RESERVADO", response.getBody().status());
            assertEquals(10, response.getBody().solicitacaoDestinoId());
            assertEquals(5, response.getBody().alunoDestinatarioId());
            verify(pessoaRepository).findByEmail("admin@teste.com");
            verify(inventarioService).atribuirEquipamento(1, 10, 99);
        }

        @Test
        @DisplayName("propaga excecao quando admin nao encontrado no repositorio")
        void atribuirAdminNaoEncontrado() {
            when(pessoaRepository.findByEmail("admin@teste.com"))
                    .thenReturn(Optional.empty());

            assertThrows(RuntimeException.class,
                    () -> controller.atribuirEquipamento(1, 10, userDetails));
            verify(pessoaRepository).findByEmail("admin@teste.com");
            verifyNoInteractions(inventarioService);
        }

        @Test
        @DisplayName("propaga excecao do service ao atribuir equipamento invalido")
        void atribuirEquipamentoInvalido() {
            when(pessoaRepository.findByEmail("admin@teste.com"))
                    .thenReturn(Optional.of(admin));
            when(inventarioService.atribuirEquipamento(999, 10, 99))
                    .thenThrow(new RuntimeException("Equipamento não encontrado"));

            assertThrows(RuntimeException.class,
                    () -> controller.atribuirEquipamento(999, 10, userDetails));
            verify(inventarioService).atribuirEquipamento(999, 10, 99);
        }
    }

    // ----------------------------------------------------------------
    // PUT /api/admin/inventario/{id}/entregar
    // ----------------------------------------------------------------

    @Nested
    @DisplayName("marcarComoEntregue")
    class MarcarComoEntregueTests {

        @Test
        @DisplayName("retorna 200 com equipamento entregue")
        void entregarComSucesso() {
            EquipamentoResponse entregue = EquipamentoResponse.builder()
                    .id(1)
                    .tipo("Notebook")
                    .descricao("Notebook Dell Latitude")
                    .status("ENTREGUE")
                    .estadoConservacao("Bom")
                    .solicitacaoDestinoId(10)
                    .alunoDestinatarioId(5)
                    .dataEntradaInventario(LocalDateTime.of(2026, 5, 1, 10, 0))
                    .dataAtribuicao(LocalDateTime.of(2026, 5, 16, 14, 30))
                    .build();

            when(pessoaRepository.findByEmail("admin@teste.com"))
                    .thenReturn(Optional.of(admin));
            when(inventarioService.marcarComoEntregue(1, 99))
                    .thenReturn(entregue);

            ResponseEntity<EquipamentoResponse> response =
                    controller.marcarComoEntregue(1, userDetails);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("ENTREGUE", response.getBody().status());
            verify(pessoaRepository).findByEmail("admin@teste.com");
            verify(inventarioService).marcarComoEntregue(1, 99);
        }

        @Test
        @DisplayName("propaga excecao quando admin nao encontrado no repositorio")
        void entregarAdminNaoEncontrado() {
            when(pessoaRepository.findByEmail("admin@teste.com"))
                    .thenReturn(Optional.empty());

            assertThrows(RuntimeException.class,
                    () -> controller.marcarComoEntregue(1, userDetails));
            verify(pessoaRepository).findByEmail("admin@teste.com");
            verifyNoInteractions(inventarioService);
        }

        @Test
        @DisplayName("propaga excecao do service ao entregar equipamento invalido")
        void entregarEquipamentoInvalido() {
            when(pessoaRepository.findByEmail("admin@teste.com"))
                    .thenReturn(Optional.of(admin));
            when(inventarioService.marcarComoEntregue(999, 99))
                    .thenThrow(new RuntimeException("Equipamento não encontrado"));

            assertThrows(RuntimeException.class,
                    () -> controller.marcarComoEntregue(999, userDetails));
            verify(inventarioService).marcarComoEntregue(999, 99);
        }
    }
}
