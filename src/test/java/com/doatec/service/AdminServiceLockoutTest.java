package com.doatec.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.doatec.exception.BusinessException;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.Role;
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
@DisplayName("AdminService - Lockout e Validação de Transições")
class AdminServiceLockoutTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private DoacaoRepository doacaoRepository;

    @Mock
    private EquipamentoRepository equipamentoRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private LogAcaoRepository logAcaoRepository;

    @Mock
    private InventarioService inventarioService;

    @Mock
    private SuporteFormularioRepository suporteRepository;

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AdminService adminService;

    private Pessoa admin;
    private Pessoa superAdmin;
    private Pessoa pessoaNormal;

    @BeforeEach
    void setUp() {
        admin = new DoadorPF("123.456.789-00");
        admin.setId(1);
        admin.setNome("Admin Teste");
        admin.setEmail("admin@doatec.com");
        admin.setAtivo(true);
        admin.setRole(Role.ADMIN);

        superAdmin = new DoadorPF("987.654.321-00");
        superAdmin.setId(2);
        superAdmin.setNome("Super Admin Teste");
        superAdmin.setEmail("super@doatec.com");
        superAdmin.setAtivo(true);
        superAdmin.setRole(Role.SUPER_ADMIN);

        pessoaNormal = new DoadorPF("111.222.333-44");
        pessoaNormal.setId(3);
        pessoaNormal.setNome("Doador Teste");
        pessoaNormal.setEmail("doador@teste.com");
        pessoaNormal.setAtivo(true);
        pessoaNormal.setRole(Role.USER);
    }

    @Nested
    @DisplayName("alterarStatusUsuario - proteção contra auto-desativação")
    class AlterarStatusUsuarioTests {

        @Test
        @DisplayName("Admin não pode alterar seu próprio status")
        void naoPodeAlterarProprioStatus() {
            when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

            BusinessException ex = assertThrows(BusinessException.class, () -> {
                adminService.alterarStatusUsuario(admin.getId(), false, admin.getId());
            });

            assertTrue(ex.getMessage().contains("próprio status"),
                    "Deve impedir admin de alterar próprio status");

            verify(pessoaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Admin pode alterar status de outra pessoa")
        void podeAlterarStatusDeOutraPessoa() {
            when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(pessoaRepository.findById(pessoaNormal.getId())).thenReturn(Optional.of(pessoaNormal));
            when(pessoaRepository.save(any())).thenReturn(pessoaNormal);
            when(logAcaoRepository.save(any())).thenReturn(null);

            assertDoesNotThrow(() -> {
                adminService.alterarStatusUsuario(pessoaNormal.getId(), false, admin.getId());
            });

            verify(pessoaRepository).save(pessoaNormal);
        }

        @Test
        @DisplayName("Admin não existe retorna erro")
        void adminNaoExisteRetornaErro() {
            when(pessoaRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(BusinessException.class, () -> {
                adminService.alterarStatusUsuario(1, false, 999);
            });
        }

        @Test
        @DisplayName("Altera para ativo=true funciona normalmente")
        void podeAtivarOutraPessoa() {
            pessoaNormal.setAtivo(false);
            when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(pessoaRepository.findById(pessoaNormal.getId())).thenReturn(Optional.of(pessoaNormal));
            when(pessoaRepository.save(any())).thenReturn(pessoaNormal);
            when(logAcaoRepository.save(any())).thenReturn(null);

            assertDoesNotThrow(() -> {
                adminService.alterarStatusUsuario(pessoaNormal.getId(), true, admin.getId());
            });

            assertTrue(pessoaNormal.getAtivo());
            verify(pessoaRepository).save(pessoaNormal);
        }
    }
}
