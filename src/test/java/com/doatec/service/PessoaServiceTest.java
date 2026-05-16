package com.doatec.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.doatec.dto.request.AlunoRegistroRequest;
import com.doatec.dto.request.DoadorPFRegistroRequest;
import com.doatec.dto.request.DoadorPJRegistroRequest;
import com.doatec.dto.request.LoginRequest;
import com.doatec.dto.request.PessoaUpdateRequest;
import com.doatec.dto.response.UsuarioAdminResponse;
import com.doatec.exception.BusinessException;
import com.doatec.model.account.Aluno;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.DoadorPJ;
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
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("PessoaService - Métodos Adicionais")
class PessoaServiceTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private DoadorPFRepository doadorPFRepository;

    @Mock
    private DoadorPJRepository doadorPJRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PessoaService pessoaService;

    private DoadorPF admin;

    @BeforeEach
    void setUp() {
        admin = new DoadorPF("123.456.789-00");
        admin.setId(1);
        admin.setNome("Admin Teste");
        admin.setEmail("admin@doatec.com");
        admin.setAtivo(true);
        admin.setRole(Role.ADMIN);
    }

    // ==================== registrarAluno ====================

    @Nested
    @DisplayName("registrarAluno")
    class RegistrarAlunoTests {

        @Test
        @DisplayName("Lança exceção quando email já cadastrado")
        void emailDuplicado() {
            AlunoRegistroRequest request = AlunoRegistroRequest.builder()
                    .nome("Novo Aluno")
                    .email("existente@test.com")
                    .senha("123456")
                    .ra("12345678")
                    .cep("12345-678")
                    .logradouro("Rua Teste")
                    .numero("100")
                    .bairro("Centro")
                    .cidade("São Paulo")
                    .estado("SP")
                    .build();

            when(pessoaRepository.findByEmail("existente@test.com")).thenReturn(Optional.of(admin));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> pessoaService.registrarAluno(request));

            assertTrue(ex.getMessage().contains("email"));
            verify(alunoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lança exceção quando RA já cadastrado")
        void raDuplicado() {
            AlunoRegistroRequest request = AlunoRegistroRequest.builder()
                    .nome("Novo Aluno")
                    .email("novo@test.com")
                    .senha("123456")
                    .ra("12345678")
                    .cep("12345-678")
                    .logradouro("Rua Teste")
                    .numero("100")
                    .bairro("Centro")
                    .cidade("São Paulo")
                    .estado("SP")
                    .build();

            when(pessoaRepository.findByEmail("novo@test.com")).thenReturn(Optional.empty());
            when(alunoRepository.existsByRa("12345678")).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> pessoaService.registrarAluno(request));

            assertTrue(ex.getMessage().contains("RA"));
            verify(alunoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Registra aluno com sucesso")
        void sucesso() {
            AlunoRegistroRequest request = AlunoRegistroRequest.builder()
                    .nome("Novo Aluno")
                    .email("novo@test.com")
                    .senha("123456")
                    .ra("12345678")
                    .cep("12345-678")
                    .logradouro("Rua Teste")
                    .numero("100")
                    .bairro("Centro")
                    .cidade("São Paulo")
                    .estado("SP")
                    .build();

            when(pessoaRepository.findByEmail("novo@test.com")).thenReturn(Optional.empty());
            when(alunoRepository.existsByRa("12345678")).thenReturn(false);
            when(passwordEncoder.encode("123456")).thenReturn("encoded123");
            when(alunoRepository.save(any(Aluno.class))).thenAnswer(inv -> {
                Aluno a = inv.getArgument(0);
                a.setId(10);
                return a;
            });

            Aluno result = pessoaService.registrarAluno(request);

            assertNotNull(result);
            assertEquals("Novo Aluno", result.getNome());
            assertEquals("novo@test.com", result.getEmail());
            assertEquals("encoded123", result.getSenha());
            assertEquals("12345678", result.getRa());
            verify(alunoRepository).save(any(Aluno.class));
        }
    }

    // ==================== registrarDoadorPF ====================

    @Nested
    @DisplayName("registrarDoadorPF")
    class RegistrarDoadorPFTests {

        @Test
        @DisplayName("Lança exceção quando email já cadastrado")
        void emailDuplicado() {
            DoadorPFRegistroRequest request = DoadorPFRegistroRequest.builder()
                    .nome("Novo Doador")
                    .email("existente@test.com")
                    .senha("123456")
                    .cpf("111.222.333-44")
                    .cep("12345-678")
                    .logradouro("Rua Teste")
                    .numero("100")
                    .bairro("Centro")
                    .cidade("São Paulo")
                    .estado("SP")
                    .build();

            when(pessoaRepository.findByEmail("existente@test.com")).thenReturn(Optional.of(admin));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> pessoaService.registrarDoadorPF(request));

            assertTrue(ex.getMessage().contains("email"));
            verify(doadorPFRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lança exceção quando CPF já cadastrado")
        void cpfDuplicado() {
            DoadorPFRegistroRequest request = DoadorPFRegistroRequest.builder()
                    .nome("Novo Doador")
                    .email("novo@test.com")
                    .senha("123456")
                    .cpf("111.222.333-44")
                    .cep("12345-678")
                    .logradouro("Rua Teste")
                    .numero("100")
                    .bairro("Centro")
                    .cidade("São Paulo")
                    .estado("SP")
                    .build();

            when(pessoaRepository.findByEmail("novo@test.com")).thenReturn(Optional.empty());
            when(doadorPFRepository.existsByCpf("111.222.333-44")).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> pessoaService.registrarDoadorPF(request));

            assertTrue(ex.getMessage().contains("CPF"));
            verify(doadorPFRepository, never()).save(any());
        }

        @Test
        @DisplayName("Registra doador PF com sucesso")
        void sucesso() {
            DoadorPFRegistroRequest request = DoadorPFRegistroRequest.builder()
                    .nome("Novo Doador")
                    .email("novo@test.com")
                    .senha("123456")
                    .cpf("111.222.333-44")
                    .cep("12345-678")
                    .logradouro("Rua Teste")
                    .numero("100")
                    .bairro("Centro")
                    .cidade("São Paulo")
                    .estado("SP")
                    .build();

            when(pessoaRepository.findByEmail("novo@test.com")).thenReturn(Optional.empty());
            when(doadorPFRepository.existsByCpf("111.222.333-44")).thenReturn(false);
            when(passwordEncoder.encode("123456")).thenReturn("encoded123");
            when(doadorPFRepository.save(any(DoadorPF.class))).thenAnswer(inv -> {
                DoadorPF d = inv.getArgument(0);
                d.setId(10);
                return d;
            });

            DoadorPF result = pessoaService.registrarDoadorPF(request);

            assertNotNull(result);
            assertEquals("Novo Doador", result.getNome());
            assertEquals("111.222.333-44", result.getCpf());
            verify(doadorPFRepository).save(any(DoadorPF.class));
        }
    }

    // ==================== registrarDoadorPJ ====================

    @Nested
    @DisplayName("registrarDoadorPJ")
    class RegistrarDoadorPJTests {

        @Test
        @DisplayName("Lança exceção quando email já cadastrado")
        void emailDuplicado() {
            DoadorPJRegistroRequest request = DoadorPJRegistroRequest.builder()
                    .razaoSocial("Empresa LTDA")
                    .nomeRepresentante("Representante")
                    .email("existente@test.com")
                    .senha("123456")
                    .cnpj("12.345.678/0001-90")
                    .cep("12345-678")
                    .logradouro("Rua Teste")
                    .numero("100")
                    .bairro("Centro")
                    .cidade("São Paulo")
                    .estado("SP")
                    .build();

            when(pessoaRepository.findByEmail("existente@test.com")).thenReturn(Optional.of(admin));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> pessoaService.registrarDoadorPJ(request));

            assertTrue(ex.getMessage().contains("email"));
            verify(doadorPJRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lança exceção quando CNPJ já cadastrado")
        void cnpjDuplicado() {
            DoadorPJRegistroRequest request = DoadorPJRegistroRequest.builder()
                    .razaoSocial("Empresa LTDA")
                    .nomeRepresentante("Representante")
                    .email("novo@test.com")
                    .senha("123456")
                    .cnpj("12.345.678/0001-90")
                    .cep("12345-678")
                    .logradouro("Rua Teste")
                    .numero("100")
                    .bairro("Centro")
                    .cidade("São Paulo")
                    .estado("SP")
                    .build();

            when(pessoaRepository.findByEmail("novo@test.com")).thenReturn(Optional.empty());
            when(doadorPJRepository.existsByCnpj("12.345.678/0001-90")).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> pessoaService.registrarDoadorPJ(request));

            assertTrue(ex.getMessage().contains("CNPJ"));
            verify(doadorPJRepository, never()).save(any());
        }

        @Test
        @DisplayName("Registra doador PJ com sucesso")
        void sucesso() {
            DoadorPJRegistroRequest request = DoadorPJRegistroRequest.builder()
                    .razaoSocial("Empresa LTDA")
                    .nomeRepresentante("Representante")
                    .email("novo@test.com")
                    .senha("123456")
                    .cnpj("12.345.678/0001-90")
                    .cep("12345-678")
                    .logradouro("Rua Teste")
                    .numero("100")
                    .bairro("Centro")
                    .cidade("São Paulo")
                    .estado("SP")
                    .build();

            when(pessoaRepository.findByEmail("novo@test.com")).thenReturn(Optional.empty());
            when(doadorPJRepository.existsByCnpj("12.345.678/0001-90")).thenReturn(false);
            when(passwordEncoder.encode("123456")).thenReturn("encoded123");
            when(doadorPJRepository.save(any(DoadorPJ.class))).thenAnswer(inv -> {
                DoadorPJ d = inv.getArgument(0);
                d.setId(10);
                return d;
            });

            DoadorPJ result = pessoaService.registrarDoadorPJ(request);

            assertNotNull(result);
            assertEquals("Representante", result.getNome());
            assertEquals("12.345.678/0001-90", result.getCnpj());
            assertEquals("Empresa LTDA", result.getRazaoSocial());
            verify(doadorPJRepository).save(any(DoadorPJ.class));
        }
    }

    // ==================== autenticar ====================

    @Nested
    @DisplayName("autenticar")
    class AutenticarTests {

        @Test
        @DisplayName("Retorna null quando usuário inativo")
        void usuarioInativo() {
            DoadorPF inativo = new DoadorPF("123.456.789-00");
            inativo.setId(5);
            inativo.setAtivo(false);

            LoginRequest request = new LoginRequest("inativo@test.com", "123456");

            when(pessoaRepository.findByEmail("inativo@test.com")).thenReturn(Optional.of(inativo));

            Pessoa result = pessoaService.autenticar(request);

            assertNull(result);
        }

        @Test
        @DisplayName("Retorna null quando senha incorreta")
        void senhaIncorreta() {
            DoadorPF pessoa = new DoadorPF("123.456.789-00");
            pessoa.setId(5);
            pessoa.setAtivo(true);
            pessoa.setSenha("encodedSenha");

            LoginRequest request = new LoginRequest("user@test.com", "senhaErrada");

            when(pessoaRepository.findByEmail("user@test.com")).thenReturn(Optional.of(pessoa));
            when(passwordEncoder.matches("senhaErrada", "encodedSenha")).thenReturn(false);

            Pessoa result = pessoaService.autenticar(request);

            assertNull(result);
        }

        @Test
        @DisplayName("Retorna pessoa quando autenticado com sucesso")
        void sucesso() {
            DoadorPF pessoa = new DoadorPF("123.456.789-00");
            pessoa.setId(5);
            pessoa.setAtivo(true);
            pessoa.setSenha("encodedSenha");

            LoginRequest request = new LoginRequest("user@test.com", "123456");

            when(pessoaRepository.findByEmail("user@test.com")).thenReturn(Optional.of(pessoa));
            when(passwordEncoder.matches("123456", "encodedSenha")).thenReturn(true);

            Pessoa result = pessoaService.autenticar(request);

            assertNotNull(result);
            assertEquals(5, result.getId());
        }
    }

    // ==================== updatePessoaProfile ====================

    @Nested
    @DisplayName("updatePessoaProfile")
    class UpdatePessoaProfileTests {

        @Test
        @DisplayName("Lança exceção quando email já cadastrado para outro usuário")
        void emailDuplicado() {
            DoadorPF pessoa = new DoadorPF("123.456.789-00");
            pessoa.setId(5);
            pessoa.setEmail("atual@test.com");

            PessoaUpdateRequest dto = PessoaUpdateRequest.builder()
                    .email("outro@test.com")
                    .build();

            when(pessoaRepository.findById(5)).thenReturn(Optional.of(pessoa));
            when(pessoaRepository.existsByEmailAndIdNot("outro@test.com", 5)).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> pessoaService.updatePessoaProfile(5, dto));

            assertTrue(ex.getMessage().contains("email"));
        }

        @Test
        @DisplayName("Atualiza parcialmente apenas campos não-nulos")
        void atualizacaoParcial() {
            DoadorPF pessoa = new DoadorPF("123.456.789-00");
            pessoa.setId(5);
            pessoa.setEmail("atual@test.com");
            pessoa.setNome("Nome Original");
            pessoa.setTelefone("11999999999");

            PessoaUpdateRequest dto = PessoaUpdateRequest.builder()
                    .telefone("11988888888")
                    .build();

            when(pessoaRepository.findById(5)).thenReturn(Optional.of(pessoa));
            when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoa);

            Pessoa result = pessoaService.updatePessoaProfile(5, dto);

            assertNotNull(result);
            assertEquals("11988888888", result.getTelefone());
            assertEquals("Nome Original", result.getNome());
            assertEquals("atual@test.com", result.getEmail());
            verify(pessoaRepository).save(pessoa);
        }

        @Test
        @DisplayName("Atualiza email com sucesso quando não duplicado")
        void atualizaEmail() {
            DoadorPF pessoa = new DoadorPF("123.456.789-00");
            pessoa.setId(5);
            pessoa.setEmail("atual@test.com");

            PessoaUpdateRequest dto = PessoaUpdateRequest.builder()
                    .email("novo@test.com")
                    .build();

            when(pessoaRepository.findById(5)).thenReturn(Optional.of(pessoa));
            when(pessoaRepository.existsByEmailAndIdNot("novo@test.com", 5)).thenReturn(false);
            when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoa);

            Pessoa result = pessoaService.updatePessoaProfile(5, dto);

            assertNotNull(result);
            assertEquals("novo@test.com", result.getEmail());
            verify(pessoaRepository).save(pessoa);
        }
    }

    // ==================== alterarRoleUsuario ====================

    @Nested
    @DisplayName("alterarRoleUsuario")
    class AlterarRoleUsuarioTests {

        @Test
        @DisplayName("Admin normal não pode promover a ADMIN")
        void adminNaoPodePromoverAAdmin() {
            DoadorPF pessoa = new DoadorPF("111.222.333-44");
            pessoa.setId(3);
            pessoa.setRole(Role.USER);

            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));
            when(pessoaRepository.findById(3)).thenReturn(Optional.of(pessoa));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> pessoaService.alterarRoleUsuario(3, Role.ADMIN, 1));

            assertTrue(ex.getMessage().contains("Super Admin"));
        }

        @Test
        @DisplayName("Altera role com sucesso")
        void sucesso() {
            DoadorPF pessoa = new DoadorPF("111.222.333-44");
            pessoa.setId(3);
            pessoa.setRole(Role.USER);

            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));
            when(pessoaRepository.findById(3)).thenReturn(Optional.of(pessoa));
            when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoa);

            UsuarioAdminResponse result = pessoaService.alterarRoleUsuario(3, Role.USER, 1);

            assertNotNull(result);
            verify(pessoaRepository).save(pessoa);
        }
    }

    // ==================== findById ====================

    @Nested
    @DisplayName("findById")
    class FindByIdTests {

        @Test
        @DisplayName("Retorna pessoa quando encontrada")
        void retornaPessoa() {
            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));

            Pessoa result = pessoaService.findById(1);

            assertNotNull(result);
            assertEquals(1, result.getId());
        }

        @Test
        @DisplayName("Retorna null quando não encontrada")
        void retornaNull() {
            when(pessoaRepository.findById(999)).thenReturn(Optional.empty());

            Pessoa result = pessoaService.findById(999);

            assertNull(result);
        }
    }

    // ==================== findAll ====================

    @Nested
    @DisplayName("findAll")
    class FindAllTests {

        @Test
        @DisplayName("Retorna lista de pessoas")
        void retornaLista() {
            when(pessoaRepository.findAll()).thenReturn(java.util.List.of(admin));

            java.util.List<Pessoa> result = pessoaService.findAll();

            assertEquals(1, result.size());
        }
    }

    // ==================== deleteById ====================

    @Nested
    @DisplayName("deleteById")
    class DeleteByIdTests {

        @Test
        @DisplayName("Deleta pessoa por ID")
        void deletaPessoa() {
            doNothing().when(pessoaRepository).deleteById(1);

            assertDoesNotThrow(() -> pessoaService.deleteById(1));

            verify(pessoaRepository).deleteById(1);
        }
    }

    // ==================== listarTodosUsuarios ====================

    @Nested
    @DisplayName("listarTodosUsuarios")
    class ListarTodosUsuariosTests {

        @Test
        @DisplayName("Retorna lista de UsuarioAdminResponse")
        void retornaLista() {
            when(pessoaRepository.findAll()).thenReturn(java.util.List.of(admin));

            java.util.List<UsuarioAdminResponse> result = pessoaService.listarTodosUsuarios();

            assertEquals(1, result.size());
        }
    }

    // ==================== listarUsuariosPorRole ====================

    @Nested
    @DisplayName("listarUsuariosPorRole")
    class ListarUsuariosPorRoleTests {

        @Test
        @DisplayName("Retorna lista filtrada por role")
        void retornaListaPorRole() {
            when(pessoaRepository.findByRole(Role.ADMIN)).thenReturn(java.util.List.of(admin));

            java.util.List<UsuarioAdminResponse> result = pessoaService.listarUsuariosPorRole(Role.ADMIN);

            assertEquals(1, result.size());
        }
    }

    // ==================== listarUsuariosPorTipoPessoa ====================

    @Nested
    @DisplayName("listarUsuariosPorTipoPessoa")
    class ListarUsuariosPorTipoPessoaTests {

        @Test
        @DisplayName("Retorna lista de alunos")
        void retornaListaDeAlunos() {
            Aluno aluno = new Aluno();
            aluno.setId(10);
            aluno.setNome("Aluno Teste");
            when(alunoRepository.findAll()).thenReturn(java.util.List.of(aluno));

            java.util.List<UsuarioAdminResponse> result = pessoaService.listarUsuariosPorTipoPessoa(com.doatec.model.account.TipoPessoa.ALUNO);

            assertEquals(1, result.size());
            verify(alunoRepository).findAll();
        }

        @Test
        @DisplayName("Retorna lista de doadores PF")
        void retornaListaDeDoadorPF() {
            when(doadorPFRepository.findAll()).thenReturn(java.util.List.of(admin));

            java.util.List<UsuarioAdminResponse> result = pessoaService.listarUsuariosPorTipoPessoa(com.doatec.model.account.TipoPessoa.DOADOR_PF);

            assertEquals(1, result.size());
            verify(doadorPFRepository).findAll();
        }

        @Test
        @DisplayName("Retorna lista de doadores PJ")
        void retornaListaDeDoadorPJ() {
            DoadorPJ doadorPJ = DoadorPJ.builder().id(5).nome("Empresa").email("emp@test.com").cnpj("12.345.678/0001-90").role(Role.USER).ativo(true).build();
            when(doadorPJRepository.findAll()).thenReturn(java.util.List.of(doadorPJ));

            java.util.List<UsuarioAdminResponse> result = pessoaService.listarUsuariosPorTipoPessoa(com.doatec.model.account.TipoPessoa.DOADOR_PJ);

            assertEquals(1, result.size());
            verify(doadorPJRepository).findAll();
        }
    }

    // ==================== registrarPessoa (deprecated) ====================

    @Nested
    @DisplayName("registrarPessoa (deprecated)")
    class RegistrarPessoaTests {

        @Test
        @DisplayName("Lança exceção quando email já cadastrado")
        void emailDuplicado() {
            com.doatec.dto.request.RegistroRequest request = new com.doatec.dto.request.RegistroRequest(
                    "ALUNO", "João", "12345678", "existente@test.com", null, null, "123456");

            when(pessoaRepository.findByEmail("existente@test.com")).thenReturn(Optional.of(admin));

            assertThrows(BusinessException.class, () -> pessoaService.registrarPessoa(request));
        }

        @Test
        @DisplayName("Lança exceção quando documento já cadastrado")
        void documentoDuplicado() {
            com.doatec.dto.request.RegistroRequest request = new com.doatec.dto.request.RegistroRequest(
                    "ALUNO", "João", "12345678", "novo@test.com", null, null, "123456");

            when(pessoaRepository.findByEmail("novo@test.com")).thenReturn(Optional.empty());
            when(pessoaRepository.findByDocumento("12345678")).thenReturn(Optional.of(admin));

            assertThrows(BusinessException.class, () -> pessoaService.registrarPessoa(request));
        }

        @Test
        @DisplayName("Lança exceção para tipo de pessoa inválido")
        void tipoPessoaInvalido() {
            com.doatec.dto.request.RegistroRequest request = new com.doatec.dto.request.RegistroRequest(
                    "INVALIDO", "João", "12345678", "novo@test.com", null, null, "123456");

            when(pessoaRepository.findByEmail("novo@test.com")).thenReturn(Optional.empty());
            when(pessoaRepository.findByDocumento("12345678")).thenReturn(Optional.empty());

            assertThrows(BusinessException.class, () -> pessoaService.registrarPessoa(request));
        }

        @Test
        @DisplayName("Lança exceção quando documento é nulo")
        void documentoNulo() {
            com.doatec.dto.request.RegistroRequest request = new com.doatec.dto.request.RegistroRequest(
                    "ALUNO", "João", null, "novo@test.com", null, null, "123456");

            when(pessoaRepository.findByEmail("novo@test.com")).thenReturn(Optional.empty());

            assertThrows(BusinessException.class, () -> pessoaService.registrarPessoa(request));
        }
    }

    // ==================== alterarStatusUsuario - branches extras ====================

    @Nested
    @DisplayName("alterarStatusUsuario - branches adicionais")
    class AlterarStatusUsuarioBranchTests {

        @Test
        @DisplayName("Lança exceção quando admin não é realmente admin")
        void adminNaoEhAdmin() {
            DoadorPF naoAdmin = new DoadorPF("111.222.333-44");
            naoAdmin.setId(50);
            naoAdmin.setRole(Role.USER);
            naoAdmin.setAtivo(true);

            when(pessoaRepository.findById(50)).thenReturn(Optional.of(naoAdmin));

            assertThrows(BusinessException.class,
                    () -> pessoaService.alterarStatusUsuario(1, false, 50));
        }

        @Test
        @DisplayName("Lança exceção quando pessoa não existe")
        void pessoaNaoExiste() {
            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));
            when(pessoaRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(BusinessException.class,
                    () -> pessoaService.alterarStatusUsuario(999, false, 1));
        }

        @Test
        @DisplayName("Lança exceção quando admin normal tenta alterar outro admin")
        void adminNormalTentaAlterarOutroAdmin() {
            DoadorPF outroAdmin = new DoadorPF("999.888.777-66");
            outroAdmin.setId(30);
            outroAdmin.setRole(Role.ADMIN);
            outroAdmin.setAtivo(true);

            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));
            when(pessoaRepository.findById(30)).thenReturn(Optional.of(outroAdmin));

            assertThrows(BusinessException.class,
                    () -> pessoaService.alterarStatusUsuario(30, false, 1));
        }
    }

    // ==================== alterarRoleUsuario - branches extras ====================

    @Nested
    @DisplayName("alterarRoleUsuario - branches adicionais")
    class AlterarRoleUsuarioBranchTests {

        @Test
        @DisplayName("Lança exceção quando admin não existe")
        void adminNaoExiste() {
            when(pessoaRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(BusinessException.class,
                    () -> pessoaService.alterarRoleUsuario(1, Role.USER, 999));
        }

        @Test
        @DisplayName("Lança exceção quando pessoa não existe")
        void pessoaNaoExiste() {
            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));
            when(pessoaRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(BusinessException.class,
                    () -> pessoaService.alterarRoleUsuario(999, Role.USER, 1));
        }

        @Test
        @DisplayName("Lança exceção quando admin normal tenta alterar role de outro admin")
        void adminNormalTentaAlterarRoleDeOutroAdmin() {
            DoadorPF outroAdmin = new DoadorPF("999.888.777-66");
            outroAdmin.setId(30);
            outroAdmin.setRole(Role.ADMIN);

            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));
            when(pessoaRepository.findById(30)).thenReturn(Optional.of(outroAdmin));

            assertThrows(BusinessException.class,
                    () -> pessoaService.alterarRoleUsuario(30, Role.USER, 1));
        }
    }
}
