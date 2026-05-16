package com.doatec.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.doatec.dto.request.DoacaoRequest;
import com.doatec.dto.response.DashboardStatsResponse;
import com.doatec.dto.response.DoacaoResponse;
import com.doatec.exception.BusinessException;
import com.doatec.model.account.Aluno;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.DoadorPJ;
import com.doatec.model.donation.Doacao;
import com.doatec.model.donation.PreferenciaEntrega;
import com.doatec.model.donation.StatusDoacao;
import com.doatec.repository.DoacaoRepository;
import com.doatec.repository.PessoaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("DoacaoService")
class DoacaoServiceTest {

    @Mock
    private DoacaoRepository doacaoRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @InjectMocks
    private DoacaoService service;

    private DoadorPF doadorPF;
    private DoadorPJ doadorPJ;
    private DoacaoRequest requestValidoPF;

    @BeforeEach
    void setUp() {
        doadorPF = new DoadorPF("123.456.789-00");
        doadorPF.setId(1);
        doadorPF.setNome("João Silva");
        doadorPF.setEmail("joao@teste.com");
        doadorPF.setSenha("senha123");

        doadorPJ = new DoadorPJ();
        doadorPJ.setId(2);
        doadorPJ.setNome("Empresa LTDA");
        doadorPJ.setEmail("empresa@teste.com");
        doadorPJ.setCnpj("12.345.678/0001-90");
        doadorPJ.setRazaoSocial("Empresa LTDA");
        doadorPJ.setSenha("senha123");

        requestValidoPF = DoacaoRequest.builder()
                .nome("João Silva")
                .tipoDocumento("cpf")
                .numeroDocumento("123.456.789-00")
                .email("joao@teste.com")
                .descricaoGeral("Notebook usado em bom estado")
                .preferenciaEntrega(PreferenciaEntrega.PONTO_DE_COLETA)
                .build();
    }

    // =====================================================================
    // registrarDoacao
    // =====================================================================
    @Nested
    @DisplayName("registrarDoacao")
    class RegistrarDoacaoTests {

        @Test
        @DisplayName("Deve lançar exceção quando email não está cadastrado")
        void emailNaoCadastrado_deveLancarExcecao() {
            when(pessoaRepository.findByEmail("inexistente@email.com"))
                    .thenReturn(Optional.empty());

            DoacaoRequest request = DoacaoRequest.builder()
                    .nome("Fantasma")
                    .tipoDocumento("cpf")
                    .numeroDocumento("000.000.000-00")
                    .email("inexistente@email.com")
                    .descricaoGeral("Qualquer coisa")
                    .build();

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    service.registrarDoacao(request));

            assertTrue(ex.getMessage().contains("não está cadastrado"));
        }

        @Test
        @DisplayName("Deve lançar exceção quando documento é CPF mas pessoa é DoadorPJ")
        void cpfParaDoadorPJ_deveLancarExcecao() {
            when(pessoaRepository.findByEmail("empresa@teste.com"))
                    .thenReturn(Optional.of(doadorPJ));

            DoacaoRequest request = DoacaoRequest.builder()
                    .nome("Empresa LTDA")
                    .tipoDocumento("cpf")
                    .numeroDocumento("123.456.789-00")
                    .email("empresa@teste.com")
                    .descricaoGeral("Computadores")
                    .build();

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    service.registrarDoacao(request));

            assertTrue(ex.getMessage().contains("não é Pessoa Física"));
        }

        @Test
        @DisplayName("Deve lançar exceção quando documento não corresponde ao cadastrado")
        void documentoDiferente_deveLancarExcecao() {
            when(pessoaRepository.findByEmail("joao@teste.com"))
                    .thenReturn(Optional.of(doadorPF));

            DoacaoRequest request = DoacaoRequest.builder()
                    .nome("João Silva")
                    .tipoDocumento("cpf")
                    .numeroDocumento("999.999.999-99")
                    .email("joao@teste.com")
                    .descricaoGeral("Monitor usado")
                    .build();

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    service.registrarDoacao(request));

            assertTrue(ex.getMessage().contains("não corresponde"));
        }

        @Test
        @DisplayName("Deve lançar exceção quando nome não corresponde ao cadastrado")
        void nomeDiferente_deveLancarExcecao() {
            when(pessoaRepository.findByEmail("joao@teste.com"))
                    .thenReturn(Optional.of(doadorPF));

            DoacaoRequest request = DoacaoRequest.builder()
                    .nome("Maria Santos")
                    .tipoDocumento("cpf")
                    .numeroDocumento("123.456.789-00")
                    .email("joao@teste.com")
                    .descricaoGeral("Teclado")
                    .build();

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    service.registrarDoacao(request));

            assertTrue(ex.getMessage().contains("Nome incorreto"));
        }

        @Test
        @DisplayName("Deve registrar doação com sucesso e retornar entidade salva")
        void sucesso_deveRetornarDoacaoSalva() {
            when(pessoaRepository.findByEmail("joao@teste.com"))
                    .thenReturn(Optional.of(doadorPF));
            when(doacaoRepository.save(any(Doacao.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            Doacao resultado = service.registrarDoacao(requestValidoPF);

            assertNotNull(resultado);
            assertEquals(doadorPF, resultado.getDoador());
            assertEquals(StatusDoacao.EM_TRIAGEM, resultado.getStatus());
            assertEquals("Notebook usado em bom estado", resultado.getDescricaoGeral());
            assertEquals(PreferenciaEntrega.PONTO_DE_COLETA, resultado.getPreferenciaEntrega());

            verify(doacaoRepository).save(any(Doacao.class));
        }
    }

    // =====================================================================
    // excluirDoacao
    // =====================================================================
    @Nested
    @DisplayName("excluirDoacao")
    class ExcluirDoacaoTests {

        private Doacao doacao;

        @BeforeEach
        void setUpDoacao() {
            doacao = new Doacao();
            doacao.setId(10);
            doacao.setDoador(doadorPF);
            doacao.setStatus(StatusDoacao.EM_TRIAGEM);
        }

        @Test
        @DisplayName("Deve lançar exceção quando doação não pertence ao solicitante")
        void ownershipDiferente_deveLancarExcecao() {
            when(doacaoRepository.findById(10))
                    .thenReturn(Optional.of(doacao));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    service.excluirDoacao(10, "outro@email.com"));

            assertEquals("Você só pode excluir suas próprias doações.", ex.getMessage());

            verify(doacaoRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando status não é EM_TRIAGEM")
        void statusNaoEmTriagem_deveLancarExcecao() {
            doacao.setStatus(StatusDoacao.AGUARDANDO_COLETA);

            when(doacaoRepository.findById(10))
                    .thenReturn(Optional.of(doacao));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    service.excluirDoacao(10, "joao@teste.com"));

            assertEquals("Só é possível excluir doações em triagem.", ex.getMessage());

            verify(doacaoRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Deve excluir doação com sucesso")
        void sucesso_deveExcluirDoacao() {
            when(doacaoRepository.findById(10))
                    .thenReturn(Optional.of(doacao));

            assertDoesNotThrow(() ->
                    service.excluirDoacao(10, "joao@teste.com"));

            verify(doacaoRepository).delete(doacao);
        }
    }

    // =====================================================================
    // listDoacoes
    // =====================================================================
    @Nested
    @DisplayName("listDoacoes")
    class ListDoacoesTests {

        private Doacao doacaoComDoador;

        @BeforeEach
        void setUpDoacao() {
            doacaoComDoador = new Doacao();
            doacaoComDoador.setId(1);
            doacaoComDoador.setDoador(doadorPF);
            doacaoComDoador.setStatus(StatusDoacao.EM_TRIAGEM);
            doacaoComDoador.setDescricaoGeral("Teste");
        }

        @Test
        @DisplayName("Deve filtrar por status e doadorId quando ambos informados")
        void comStatusEDoadorId_deveFiltrarPorAmbos() {
            when(doacaoRepository.findByDoadorIdAndStatus(1, StatusDoacao.EM_TRIAGEM))
                    .thenReturn(List.of(doacaoComDoador));

            List<DoacaoResponse> resultado = service.listDoacoes(StatusDoacao.EM_TRIAGEM, 1);

            assertEquals(1, resultado.size());
            verify(doacaoRepository).findByDoadorIdAndStatus(1, StatusDoacao.EM_TRIAGEM);
        }

        @Test
        @DisplayName("Deve filtrar apenas por status quando doadorId é nulo")
        void apenasStatus_deveFiltrarPorStatus() {
            when(doacaoRepository.findByStatus(StatusDoacao.EM_TRIAGEM))
                    .thenReturn(List.of(doacaoComDoador));

            List<DoacaoResponse> resultado = service.listDoacoes(StatusDoacao.EM_TRIAGEM, null);

            assertEquals(1, resultado.size());
            verify(doacaoRepository).findByStatus(StatusDoacao.EM_TRIAGEM);
        }

        @Test
        @DisplayName("Deve filtrar apenas por doadorId quando status é nulo")
        void apenasDoadorId_deveFiltrarPorDoador() {
            when(doacaoRepository.findByDoadorId(1))
                    .thenReturn(List.of(doacaoComDoador));

            List<DoacaoResponse> resultado = service.listDoacoes(null, 1);

            assertEquals(1, resultado.size());
            verify(doacaoRepository).findByDoadorId(1);
        }

        @Test
        @DisplayName("Deve retornar todas quando nenhum filtro é informado")
        void semFiltros_deveRetornarTodas() {
            when(doacaoRepository.findAll())
                    .thenReturn(List.of(doacaoComDoador));

            List<DoacaoResponse> resultado = service.listDoacoes(null, null);

            assertEquals(1, resultado.size());
            verify(doacaoRepository).findAll();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há doações")
        void listaVazia_deveRetornarListaVazia() {
            when(doacaoRepository.findAll())
                    .thenReturn(Collections.emptyList());

            List<DoacaoResponse> resultado = service.listDoacoes(null, null);

            assertTrue(resultado.isEmpty());
            verify(doacaoRepository).findAll();
        }

        @Test
        @DisplayName("Filtra por status e doadorId simultaneamente")
        void filtraPorStatusEDoadorId() {
            Doacao doacao = new Doacao();
            doacao.setId(1);
            doacao.setStatus(StatusDoacao.EM_TRIAGEM);

            when(doacaoRepository.findByDoadorIdAndStatus(5, StatusDoacao.EM_TRIAGEM))
                    .thenReturn(List.of(doacao));

            List<DoacaoResponse> resultado = service.listDoacoes(StatusDoacao.EM_TRIAGEM, 5);

            assertEquals(1, resultado.size());
            verify(doacaoRepository).findByDoadorIdAndStatus(5, StatusDoacao.EM_TRIAGEM);
        }
    }

    // ==================== getDashboardStats ====================

    @Nested
    @DisplayName("getDashboardStats")
    class GetDashboardStatsTests {

        @Test
        @DisplayName("Retorna estatísticas com última doação")
        void retornaEstatisticasComUltimaDoacao() {
            Doacao doacao = new Doacao();
            doacao.setId(1);
            doacao.setDataDoacao(java.time.LocalDate.of(2026, 5, 15));

            when(doacaoRepository.count()).thenReturn(10L);
            when(doacaoRepository.findTopByOrderByDataDoacaoDesc()).thenReturn(Optional.of(doacao));

            DashboardStatsResponse stats = service.getDashboardStats();

            assertNotNull(stats);
            assertEquals(10L, stats.totalDonations());
            assertEquals(java.time.LocalDate.of(2026, 5, 15), stats.lastDonationDate());
        }

        @Test
        @DisplayName("Retorna estatísticas sem doações")
        void retornaEstatisticasSemDoacoes() {
            when(doacaoRepository.count()).thenReturn(0L);
            when(doacaoRepository.findTopByOrderByDataDoacaoDesc()).thenReturn(Optional.empty());

            DashboardStatsResponse stats = service.getDashboardStats();

            assertNotNull(stats);
            assertEquals(0L, stats.totalDonations());
            assertNull(stats.lastDonationDate());
        }
    }

    // ==================== findDoacoesByDoadorId ====================

    @Nested
    @DisplayName("findDoacoesByDoadorId")
    class FindDoacoesByDoadorIdTests {

        @Test
        @DisplayName("Retorna doações do doador")
        void retornaDoacoesDoDoador() {
            DoadorPF doador = new DoadorPF("111.222.333-44");
            doador.setId(5);
            doador.setNome("Doador");
            doador.setEmail("doador@test.com");

            Doacao doacao = new Doacao();
            doacao.setId(1);
            doacao.setStatus(StatusDoacao.EM_TRIAGEM);
            doacao.setDoador(doador);

            when(doacaoRepository.findByDoadorId(5)).thenReturn(List.of(doacao));

            List<DoacaoResponse> resultado = service.findDoacoesByDoadorId(5);

            assertEquals(1, resultado.size());
            verify(doacaoRepository).findByDoadorId(5);
        }
    }

    // ==================== registrarDoacao - tipos adicionais ====================

    @Nested
    @DisplayName("registrarDoacao - tipos de documento adicionais")
    class RegistrarDoacaoTiposTests {

        @Test
        @DisplayName("Lança exceção para tipo de documento inválido")
        void tipoDocumentoInvalido() {
            DoadorPF doador = new DoadorPF("123.456.789-00");
            doador.setId(1);
            doador.setNome("Doador");
            doador.setEmail("doador@test.com");

            DoacaoRequest request = DoacaoRequest.builder()
                    .nome("Doador")
                    .tipoDocumento("invalido")
                    .numeroDocumento("123.456.789-00")
                    .email("doador@test.com")
                    .descricaoGeral("Teste")
                    .build();

            when(pessoaRepository.findByEmail("doador@test.com")).thenReturn(Optional.of(doador));

            assertThrows(BusinessException.class, () -> service.registrarDoacao(request));
        }

        @Test
        @DisplayName("Registra doação com telefone atualiza cadastro")
        void registraComTelefone() {
            DoadorPF doador = new DoadorPF("123.456.789-00");
            doador.setId(1);
            doador.setNome("Doador");
            doador.setEmail("doador@test.com");

            DoacaoRequest request = DoacaoRequest.builder()
                    .nome("Doador")
                    .tipoDocumento("cpf")
                    .numeroDocumento("123.456.789-00")
                    .email("doador@test.com")
                    .descricaoGeral("Teste")
                    .telefone("11988888888")
                    .build();

            when(pessoaRepository.findByEmail("doador@test.com")).thenReturn(Optional.of(doador));
            when(pessoaRepository.save(any())).thenReturn(doador);
            when(doacaoRepository.save(any(Doacao.class))).thenReturn(new Doacao());

            Doacao result = service.registrarDoacao(request);

            assertNotNull(result);
            assertEquals("11988888888", doador.getTelefone());
            verify(pessoaRepository).save(doador);
        }
    }
}
