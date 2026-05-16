package com.doatec.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.doatec.dto.request.CriarAdminRequest;
import com.doatec.dto.response.UsuarioAdminResponse;
import com.doatec.exception.BusinessException;
import com.doatec.model.account.*;
import com.doatec.repository.PessoaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("SuperAdminService")
class SuperAdminServiceTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SuperAdminService service;

    private Pessoa superAdmin;
    private Pessoa adminAlvo;

    private static final Integer SUPER_ADMIN_ID = 1;
    private static final Integer ADMIN_ALVO_ID = 2;

    @BeforeEach
    void setUp() {
        superAdmin = DoadorPF.builder()
                .id(SUPER_ADMIN_ID)
                .nome("Super Admin")
                .email("super@doatec.com")
                .senha("encoded")
                .cpf("12345678901")
                .role(Role.SUPER_ADMIN)
                .ativo(true)
                .build();

        adminAlvo = DoadorPF.builder()
                .id(ADMIN_ALVO_ID)
                .nome("Admin Alvo")
                .email("admin@doatec.com")
                .senha("encoded")
                .cpf("98765432100")
                .role(Role.ADMIN)
                .ativo(true)
                .build();
    }

    private void stubSuperAdminValido() {
        when(pessoaRepository.findById(SUPER_ADMIN_ID)).thenReturn(Optional.of(superAdmin));
    }

    // =====================================================================
    // criarAdmin
    // =====================================================================
    @Nested
    @DisplayName("criarAdmin")
    class CriarAdminTests {

        @Test
        @DisplayName("Deve lancar excecao quando quem executa nao e Super Admin")
        void naoESuperAdmin_deveLancarExcecao() {
            Pessoa adminComum = DoadorPF.builder()
                    .id(10)
                    .nome("Admin Comum")
                    .email("admincomum@doatec.com")
                    .senha("encoded")
                    .cpf("11122233344")
                    .role(Role.ADMIN)
                    .ativo(true)
                    .build();

            when(pessoaRepository.findById(10)).thenReturn(Optional.of(adminComum));

            CriarAdminRequest request = new CriarAdminRequest(
                    "Novo Admin", "novo@doatec.com", "Senha@123",
                    "DOADOR_PF", "55566677788", "ADMIN"
            );

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    service.criarAdmin(request, 10));

            assertEquals("Apenas Super Admins podem realizar esta operação.", ex.getMessage());
        }

        @Test
        @DisplayName("Deve lancar excecao ao tentar criar SUPER_ADMIN")
        void criarSuperAdmin_deveLancarExcecao() {
            stubSuperAdminValido();
            when(pessoaRepository.existsByEmail("novo@doatec.com")).thenReturn(false);
            when(pessoaRepository.findByDocumento("55566677788")).thenReturn(Optional.empty());

            CriarAdminRequest request = new CriarAdminRequest(
                    "Novo Admin", "novo@doatec.com", "Senha@123",
                    "DOADOR_PF", "55566677788", "SUPER_ADMIN"
            );

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    service.criarAdmin(request, SUPER_ADMIN_ID));

            assertEquals("Não é possível criar outro Super Admin.", ex.getMessage());
        }

        @Test
        @DisplayName("Deve usar senha padrao quando senha for nula")
        void senhaNula_deveUsarSenhaPadrao() {
            stubSuperAdminValido();
            when(pessoaRepository.existsByEmail("novo@doatec.com")).thenReturn(false);
            when(pessoaRepository.findByDocumento("55566677788")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("Admin@123")).thenReturn("encodedDefault");
            when(pessoaRepository.save(any(Pessoa.class))).thenAnswer(inv -> inv.getArgument(0));

            CriarAdminRequest request = new CriarAdminRequest(
                    "Novo Admin", "novo@doatec.com", null,
                    "DOADOR_PF", "55566677788", "ADMIN"
            );

            UsuarioAdminResponse response = service.criarAdmin(request, SUPER_ADMIN_ID);

            assertNotNull(response);
            assertEquals("Novo Admin", response.nome());
            verify(passwordEncoder).encode("Admin@123");
            verify(pessoaRepository).save(any(Pessoa.class));
        }

        @Test
        @DisplayName("Deve criar admin com sucesso")
        void sucesso_deveCriarAdmin() {
            stubSuperAdminValido();
            when(pessoaRepository.existsByEmail("novo@doatec.com")).thenReturn(false);
            when(pessoaRepository.findByDocumento("55566677788")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("Senha@123")).thenReturn("encodedSenha");
            when(pessoaRepository.save(any(Pessoa.class))).thenAnswer(inv -> {
                Pessoa p = inv.getArgument(0);
                p.setId(99);
                return p;
            });

            CriarAdminRequest request = new CriarAdminRequest(
                    "Novo Admin", "novo@doatec.com", "Senha@123",
                    "DOADOR_PF", "55566677788", "ADMIN"
            );

            UsuarioAdminResponse response = service.criarAdmin(request, SUPER_ADMIN_ID);

            assertNotNull(response);
            assertEquals("Novo Admin", response.nome());
            assertEquals("ADMIN", response.role());
            assertEquals("novo@doatec.com", response.email());
            verify(pessoaRepository).save(any(Pessoa.class));
        }
    }

    // =====================================================================
    // rebaixarAdmin
    // =====================================================================
    @Nested
    @DisplayName("rebaixarAdmin")
    class RebaixarAdminTests {

        @Test
        @DisplayName("Deve lancar excecao ao tentar rebaixar a si mesmo")
        void autoRebaixamento_deveLancarExcecao() {
            stubSuperAdminValido();

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    service.rebaixarAdmin(SUPER_ADMIN_ID, SUPER_ADMIN_ID));

            assertEquals("Você não pode rebaixar a si mesmo.", ex.getMessage());
        }

        @Test
        @DisplayName("Deve lancar excecao ao tentar rebaixar um Super Admin")
        void rebaixarSuperAdmin_deveLancarExcecao() {
            stubSuperAdminValido();
            Pessoa outroSuperAdmin = DoadorPF.builder()
                    .id(3)
                    .nome("Outro Super")
                    .email("outro@doatec.com")
                    .senha("encoded")
                    .cpf("11111111111")
                    .role(Role.SUPER_ADMIN)
                    .ativo(true)
                    .build();
            when(pessoaRepository.findById(3)).thenReturn(Optional.of(outroSuperAdmin));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    service.rebaixarAdmin(3, SUPER_ADMIN_ID));

            assertEquals("Não é possível rebaixar um Super Admin.", ex.getMessage());
        }

        @Test
        @DisplayName("Deve rebaixar admin com sucesso - role vira USER")
        void sucesso_deveRebaixarParaUser() {
            stubSuperAdminValido();
            when(pessoaRepository.findById(ADMIN_ALVO_ID)).thenReturn(Optional.of(adminAlvo));
            when(pessoaRepository.save(any(Pessoa.class))).thenAnswer(inv -> inv.getArgument(0));

            UsuarioAdminResponse response = service.rebaixarAdmin(ADMIN_ALVO_ID, SUPER_ADMIN_ID);

            assertNotNull(response);
            assertEquals("USER", response.role());
            assertEquals(Role.USER, adminAlvo.getRole());
            verify(pessoaRepository).save(adminAlvo);
        }
    }

    // =====================================================================
    // excluirAdmin
    // =====================================================================
    @Nested
    @DisplayName("excluirAdmin")
    class ExcluirAdminTests {

        @Test
        @DisplayName("Deve lancar excecao ao tentar excluir o ultimo Super Admin")
        void ultimoSuperAdmin_deveLancarExcecao() {
            stubSuperAdminValido();
            Pessoa outroSuperAdmin = DoadorPF.builder()
                    .id(3)
                    .nome("Outro Super")
                    .email("outro@doatec.com")
                    .senha("encoded")
                    .cpf("11111111111")
                    .role(Role.SUPER_ADMIN)
                    .ativo(true)
                    .build();
            when(pessoaRepository.findById(3)).thenReturn(Optional.of(outroSuperAdmin));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    service.excluirAdmin(3, SUPER_ADMIN_ID));

            assertEquals("Não é possível excluir um Super Admin.", ex.getMessage());
        }

        @Test
        @DisplayName("Deve excluir admin com sucesso - desativa e rebaixa")
        void sucesso_deveDesativarERebaixar() {
            stubSuperAdminValido();
            when(pessoaRepository.findById(ADMIN_ALVO_ID)).thenReturn(Optional.of(adminAlvo));
            when(pessoaRepository.save(any(Pessoa.class))).thenAnswer(inv -> inv.getArgument(0));

            assertDoesNotThrow(() ->
                    service.excluirAdmin(ADMIN_ALVO_ID, SUPER_ADMIN_ID));

            assertFalse(adminAlvo.getAtivo());
            assertEquals(Role.USER, adminAlvo.getRole());
            verify(pessoaRepository).save(adminAlvo);
        }
    }

    // =====================================================================
    // alterarRoleAdmin
    // =====================================================================
    @Nested
    @DisplayName("alterarRoleAdmin")
    class AlterarRoleAdminTests {

        @Test
        @DisplayName("Deve lancar excecao ao tentar alterar propria role")
        void autoModificacao_deveLancarExcecao() {
            stubSuperAdminValido();

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    service.alterarRoleAdmin(SUPER_ADMIN_ID, Role.ADMIN, SUPER_ADMIN_ID));

            assertEquals("Você não pode alterar sua própria role.", ex.getMessage());
        }

        @Test
        @DisplayName("Deve alterar role com sucesso")
        void sucesso_deveAlterarRole() {
            stubSuperAdminValido();
            when(pessoaRepository.findById(ADMIN_ALVO_ID)).thenReturn(Optional.of(adminAlvo));
            when(pessoaRepository.save(any(Pessoa.class))).thenAnswer(inv -> inv.getArgument(0));

            UsuarioAdminResponse response = service.alterarRoleAdmin(ADMIN_ALVO_ID, Role.USER, SUPER_ADMIN_ID);

            assertNotNull(response);
            assertEquals("USER", response.role());
            assertEquals(Role.USER, adminAlvo.getRole());
            verify(pessoaRepository).save(adminAlvo);
        }
    }

    // =====================================================================
    // alterarStatusAdmin
    // =====================================================================
    @Nested
    @DisplayName("alterarStatusAdmin")
    class AlterarStatusAdminTests {

        @Test
        @DisplayName("Deve lancar excecao ao tentar alterar proprio status")
        void autoModificacao_deveLancarExcecao() {
            stubSuperAdminValido();

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    service.alterarStatusAdmin(SUPER_ADMIN_ID, false, SUPER_ADMIN_ID));

            assertEquals("Você não pode alterar seu próprio status.", ex.getMessage());
        }

        @Test
        @DisplayName("Deve alterar status com sucesso")
        void sucesso_deveAlterarStatus() {
            stubSuperAdminValido();
            when(pessoaRepository.findById(ADMIN_ALVO_ID)).thenReturn(Optional.of(adminAlvo));
            when(pessoaRepository.save(any(Pessoa.class))).thenAnswer(inv -> inv.getArgument(0));

            UsuarioAdminResponse response = service.alterarStatusAdmin(ADMIN_ALVO_ID, false, SUPER_ADMIN_ID);

            assertNotNull(response);
            assertFalse(response.ativo());
            assertFalse(adminAlvo.getAtivo());
            verify(pessoaRepository).save(adminAlvo);
        }
    }

    // ==================== listarAdmins ====================

    @Nested
    @DisplayName("listarAdmins")
    class ListarAdminsTests {

        @Test
        @DisplayName("Deve listar admins com paginação")
        void listarAdminsComPaginacao() {
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
            org.springframework.data.domain.Page<Pessoa> page = new org.springframework.data.domain.PageImpl<>(java.util.List.of(adminAlvo), pageable, 1);

            when(pessoaRepository.findByRoleIn(any(), any())).thenReturn(page);

            org.springframework.data.domain.Page<UsuarioAdminResponse> result = service.listarAdmins(pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(pessoaRepository).findByRoleIn(any(), any());
        }
    }

    // ==================== criarAdmin - DOADOR_PJ ====================

    @Nested
    @DisplayName("criarAdmin - tipos adicionais")
    class CriarAdminTiposTests {

        @Test
        @DisplayName("Deve criar admin doador PJ")
        void criarAdminDoadorPJ() {
            stubSuperAdminValido();

            CriarAdminRequest request = new CriarAdminRequest(
                    "Admin PJ", "adminpj@test.com", "123456",
                    "DOADOR_PJ", "12.345.678/0001-90", "ADMIN"
            );

            when(pessoaRepository.existsByEmail("adminpj@test.com")).thenReturn(false);
            when(pessoaRepository.findByDocumento("12.345.678/0001-90")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("123456")).thenReturn("encoded");
            when(pessoaRepository.save(any(Pessoa.class))).thenAnswer(inv -> inv.getArgument(0));

            UsuarioAdminResponse response = service.criarAdmin(request, SUPER_ADMIN_ID);

            assertNotNull(response);
            assertEquals("Admin PJ", response.nome());
            verify(pessoaRepository).save(any());
        }

        @Test
        @DisplayName("Lança exceção para role não-ADMIN")
        void lancaExcecaoParaRoleNaoAdmin() {
            stubSuperAdminValido();

            CriarAdminRequest request = new CriarAdminRequest(
                    "User", "user@test.com", "123456",
                    "DOADOR_PF", "123.456.789-00", "USER"
            );

            when(pessoaRepository.existsByEmail("user@test.com")).thenReturn(false);
            when(pessoaRepository.findByDocumento("123.456.789-00")).thenReturn(Optional.empty());

            assertThrows(BusinessException.class, () -> service.criarAdmin(request, SUPER_ADMIN_ID));
        }

        @Test
        @DisplayName("Lança exceção para documento duplicado")
        void lancaExcecaoParaDocumentoDuplicado() {
            stubSuperAdminValido();

            CriarAdminRequest request = new CriarAdminRequest(
                    "Admin", "novo@test.com", "123456",
                    "DOADOR_PF", "123.456.789-00", "ADMIN"
            );

            when(pessoaRepository.existsByEmail("novo@test.com")).thenReturn(false);
            when(pessoaRepository.findByDocumento("123.456.789-00")).thenReturn(Optional.of(adminAlvo));

            assertThrows(BusinessException.class, () -> service.criarAdmin(request, SUPER_ADMIN_ID));
        }

        @Test
        @DisplayName("Lança exceção para email duplicado")
        void lancaExcecaoParaEmailDuplicado() {
            stubSuperAdminValido();

            CriarAdminRequest request = new CriarAdminRequest(
                    "Admin", "existente@test.com", "123456",
                    "DOADOR_PF", "999.888.777-66", "ADMIN"
            );

            when(pessoaRepository.existsByEmail("existente@test.com")).thenReturn(true);

            assertThrows(BusinessException.class, () -> service.criarAdmin(request, SUPER_ADMIN_ID));
        }
    }
}
