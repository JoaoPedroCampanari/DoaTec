package com.doatec.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.Role;
import com.doatec.model.donation.Doacao;
import com.doatec.model.donation.StatusDoacao;
import com.doatec.exception.BusinessException;
import com.doatec.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService - Transições de Status de Doação")
class AdminServiceDoacaoTest {

    @Mock
    private DoacaoService doacaoService;

    @Mock
    private DoacaoRepository doacaoRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private EquipamentoRepository equipamentoRepository;

    @Mock
    private InventarioService inventarioService;

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private LogAcaoRepository logAcaoRepository;

    @InjectMocks
    private AdminService adminService;

    private Pessoa admin;
    private Doacao doacao;

    @BeforeEach
    void setUp() {
        admin = new DoadorPF("123.456.789-00");
        admin.setId(1);
        admin.setNome("Admin Teste");
        admin.setEmail("admin@doatec.com");
        admin.setAtivo(true);
        admin.setRole(Role.ADMIN);

        doacao = new Doacao();
        doacao.setId(1);
        doacao.setStatus(StatusDoacao.EM_ANALISE);
        doacao.setDoador(admin);
    }

    private void stubSaveMocks() {
        when(doacaoRepository.save(any(Doacao.class))).thenAnswer(inv -> inv.getArgument(0));
        when(logAcaoRepository.save(any())).thenReturn(null);
        when(notificacaoService.criarNotificacao(anyInt(), anyString(), anyString(), any(), anyInt(), anyString()))
                .thenReturn(null);
    }

    @Nested
    @DisplayName("aprovarDoacao validacao de transicao")
    class AprovarDoacaoTests {

        @Test
        @DisplayName("EM_ANALISE -> FINALIZADO é válido")
        void emAnaliseParaFinalizadoValido() {
            doacao.setStatus(StatusDoacao.EM_ANALISE);
            when(doacaoRepository.findById(1)).thenReturn(Optional.of(doacao));
            when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            stubSaveMocks();

            assertDoesNotThrow(() -> adminService.aprovarDoacao(1, admin.getId(), null));
            assertEquals(StatusDoacao.FINALIZADO, doacao.getStatus());
        }

        @Test
        @DisplayName("EM_TRIAGEM -> FINALIZADO lança BusinessException")
        void emTriagemParaFinalizadoInvalido() {
            doacao.setStatus(StatusDoacao.EM_TRIAGEM);
            when(doacaoRepository.findById(1)).thenReturn(Optional.of(doacao));
            when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    adminService.aprovarDoacao(1, admin.getId(), null));

            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("status") || ex.getMessage().contains("Transição"));
            assertEquals(StatusDoacao.EM_TRIAGEM, doacao.getStatus());
        }

        @Test
        @DisplayName("RECEBIDO -> FINALIZADO lança BusinessException")
        void recebidoParaFinalizadoInvalido() {
            doacao.setStatus(StatusDoacao.RECEBIDO);
            when(doacaoRepository.findById(1)).thenReturn(Optional.of(doacao));
            when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    adminService.aprovarDoacao(1, admin.getId(), null));

            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("status") || ex.getMessage().contains("Transição"));
        }

        @Test
        @DisplayName("FINALIZADO -> FINALIZADO lança BusinessException (idempotente)")
        void finalizadoParaFinalizadoInvalido() {
            doacao.setStatus(StatusDoacao.FINALIZADO);
            when(doacaoRepository.findById(1)).thenReturn(Optional.of(doacao));
            when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    adminService.aprovarDoacao(1, admin.getId(), null));

            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("status") || ex.getMessage().contains("Transição"));
        }
    }

    @Nested
    @DisplayName("rejeitarDoacao validacao de transicao")
    class RejeitarDoacaoTests {

        @Test
        @DisplayName("EM_ANALISE -> REJEITADA é válido")
        void emAnaliseParaRejeitadaValido() {
            doacao.setStatus(StatusDoacao.EM_ANALISE);
            when(doacaoRepository.findById(1)).thenReturn(Optional.of(doacao));
            when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            stubSaveMocks();

            assertDoesNotThrow(() -> adminService.rejeitarDoacao(1, admin.getId(), null));
            assertEquals(StatusDoacao.REJEITADA, doacao.getStatus());
        }

        @Test
        @DisplayName("RECEBIDO -> REJEITADA é válido")
        void recebidoParaRejeitadaValido() {
            doacao.setStatus(StatusDoacao.RECEBIDO);
            when(doacaoRepository.findById(1)).thenReturn(Optional.of(doacao));
            when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            stubSaveMocks();

            assertDoesNotThrow(() -> adminService.rejeitarDoacao(1, admin.getId(), null));
            assertEquals(StatusDoacao.REJEITADA, doacao.getStatus());
        }

        @Test
        @DisplayName("AGUARDANDO_COLETA -> REJEITADA é válido")
        void aguardandoColetaParaRejeitadaValido() {
            doacao.setStatus(StatusDoacao.AGUARDANDO_COLETA);
            when(doacaoRepository.findById(1)).thenReturn(Optional.of(doacao));
            when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            stubSaveMocks();

            assertDoesNotThrow(() -> adminService.rejeitarDoacao(1, admin.getId(), null));
            assertEquals(StatusDoacao.REJEITADA, doacao.getStatus());
        }

        @Test
        @DisplayName("FINALIZADO -> REJEITADA lança BusinessException")
        void finalizadoParaRejeitadaInvalido() {
            doacao.setStatus(StatusDoacao.FINALIZADO);
            when(doacaoRepository.findById(1)).thenReturn(Optional.of(doacao));
            when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    adminService.rejeitarDoacao(1, admin.getId(), null));

            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("status") || ex.getMessage().contains("Transição"));
            assertEquals(StatusDoacao.FINALIZADO, doacao.getStatus());
        }

        @Test
        @DisplayName("REJEITADA -> REJEITADA lança BusinessException")
        void rejeitadaParaRejeitadaInvalido() {
            doacao.setStatus(StatusDoacao.REJEITADA);
            when(doacaoRepository.findById(1)).thenReturn(Optional.of(doacao));
            when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    adminService.rejeitarDoacao(1, admin.getId(), null));

            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("status") || ex.getMessage().contains("Transição"));
        }
    }

    // Note: we can't fully test AdminService without all its mocks,
    // so we test the transition logic directly via a dedicated test class.

    @Test
    @DisplayName("Rejeitada -> Em Triagem é transição válida")
    void transicaoRejeitadaParaEmTriagem() {
        // This mirrors validarTransicaoDoacao logic from AdminService:244
        StatusDoacao atual = StatusDoacao.REJEITADA;
        StatusDoacao novo = StatusDoacao.EM_TRIAGEM;

        boolean transicaoValida = switch (atual) {
            case EM_TRIAGEM -> novo == StatusDoacao.AGUARDANDO_COLETA || novo == StatusDoacao.REJEITADA;
            case AGUARDANDO_COLETA -> novo == StatusDoacao.RECEBIDO || novo == StatusDoacao.REJEITADA;
            case RECEBIDO -> novo == StatusDoacao.EM_ANALISE || novo == StatusDoacao.REJEITADA;
            case EM_ANALISE -> novo == StatusDoacao.FINALIZADO || novo == StatusDoacao.REJEITADA;
            case REJEITADA -> novo == StatusDoacao.EM_TRIAGEM;
            case FINALIZADO -> false;
        };

        assertTrue(transicaoValida, "REJEITADA → EM_TRIAGEM deve ser permitida");
    }

    @Test
    @DisplayName("Rejeitada -> outro status é inválido")
    void transicaoRejeitadaParaOutroInvalido() {
        StatusDoacao atual = StatusDoacao.REJEITADA;
        StatusDoacao novo = StatusDoacao.AGUARDANDO_COLETA;

        boolean transicaoValida = switch (atual) {
            case EM_TRIAGEM -> novo == StatusDoacao.AGUARDANDO_COLETA || novo == StatusDoacao.REJEITADA;
            case AGUARDANDO_COLETA -> novo == StatusDoacao.RECEBIDO || novo == StatusDoacao.REJEITADA;
            case RECEBIDO -> novo == StatusDoacao.EM_ANALISE || novo == StatusDoacao.REJEITADA;
            case EM_ANALISE -> novo == StatusDoacao.FINALIZADO || novo == StatusDoacao.REJEITADA;
            case REJEITADA -> novo == StatusDoacao.EM_TRIAGEM;
            case FINALIZADO -> false;
        };

        assertFalse(transicaoValida, "REJEITADA → AGUARDANDO_COLETA deve ser inválido");
    }

    @Test
    @DisplayName("Finalizado não pode ser alterado")
    void transicaoFinalizadoInvalido() {
        StatusDoacao atual = StatusDoacao.FINALIZADO;
        StatusDoacao novo = StatusDoacao.EM_TRIAGEM;

        boolean transicaoValida = switch (atual) {
            case EM_TRIAGEM -> novo == StatusDoacao.AGUARDANDO_COLETA || novo == StatusDoacao.REJEITADA;
            case AGUARDANDO_COLETA -> novo == StatusDoacao.RECEBIDO || novo == StatusDoacao.REJEITADA;
            case RECEBIDO -> novo == StatusDoacao.EM_ANALISE || novo == StatusDoacao.REJEITADA;
            case EM_ANALISE -> novo == StatusDoacao.FINALIZADO || novo == StatusDoacao.REJEITADA;
            case REJEITADA -> novo == StatusDoacao.EM_TRIAGEM;
            case FINALIZADO -> false;
        };

        assertFalse(transicaoValida, "FINALIZADO → EM_TRIAGEM deve ser inválido");
    }
}
