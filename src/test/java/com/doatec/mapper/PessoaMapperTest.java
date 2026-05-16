package com.doatec.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.doatec.dto.request.AlunoRegistroRequest;
import com.doatec.dto.request.DoadorPFRegistroRequest;
import com.doatec.dto.request.DoadorPJRegistroRequest;
import com.doatec.dto.request.PessoaUpdateRequest;
import com.doatec.dto.response.UserLoginResponse;
import com.doatec.dto.response.UsuarioAdminResponse;
import com.doatec.model.account.Aluno;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.DoadorPJ;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PessoaMapper")
class PessoaMapperTest {

    // ==================== toAluno ====================

    @Nested
    @DisplayName("toAluno")
    class ToAlunoTests {

        @Test
        @DisplayName("Converte AlunoRegistroRequest para Aluno")
        void converteParaAluno() {
            AlunoRegistroRequest request = AlunoRegistroRequest.builder()
                    .nome("João Silva")
                    .email("joao@test.com")
                    .senha("123456")
                    .ra("12345678")
                    .telefone("11999999999")
                    .cep("12345-678")
                    .logradouro("Rua Teste")
                    .numero("100")
                    .bairro("Centro")
                    .cidade("São Paulo")
                    .estado("SP")
                    .build();

            Aluno aluno = PessoaMapper.toAluno(request);

            assertNotNull(aluno);
            assertEquals("João Silva", aluno.getNome());
            assertEquals("joao@test.com", aluno.getEmail());
            assertEquals("123456", aluno.getSenha());
            assertEquals("12345678", aluno.getRa());
            assertEquals("11999999999", aluno.getTelefone());
            assertEquals("12345-678", aluno.getEndereco());
            assertEquals("Rua Teste", aluno.getLogradouro());
            assertEquals("100", aluno.getNumero());
            assertEquals("Centro", aluno.getBairro());
            assertEquals("São Paulo", aluno.getCidade());
            assertEquals("SP", aluno.getEstado());
            assertEquals(Role.USER, aluno.getRole());
            assertTrue(aluno.getAtivo());
        }

        @Test
        @DisplayName("Converte com telefone nulo")
        void converteComTelefoneNulo() {
            AlunoRegistroRequest request = AlunoRegistroRequest.builder()
                    .nome("João")
                    .email("joao@test.com")
                    .senha("123456")
                    .ra("12345678")
                    .cep("12345-678")
                    .logradouro("Rua")
                    .numero("1")
                    .bairro("Bairro")
                    .cidade("Cidade")
                    .estado("SP")
                    .build();

            Aluno aluno = PessoaMapper.toAluno(request);

            assertEquals("", aluno.getTelefone());
        }
    }

    // ==================== toDoadorPF ====================

    @Nested
    @DisplayName("toDoadorPF")
    class ToDoadorPFTests {

        @Test
        @DisplayName("Converte DoadorPFRegistroRequest para DoadorPF")
        void converteParaDoadorPF() {
            DoadorPFRegistroRequest request = DoadorPFRegistroRequest.builder()
                    .nome("Maria Santos")
                    .email("maria@test.com")
                    .senha("123456")
                    .cpf("123.456.789-00")
                    .telefone("11988888888")
                    .cep("12345-678")
                    .logradouro("Rua A")
                    .numero("200")
                    .bairro("Jardim")
                    .cidade("Campinas")
                    .estado("SP")
                    .build();

            DoadorPF doador = PessoaMapper.toDoadorPF(request);

            assertNotNull(doador);
            assertEquals("Maria Santos", doador.getNome());
            assertEquals("maria@test.com", doador.getEmail());
            assertEquals("123.456.789-00", doador.getCpf());
            assertEquals("11988888888", doador.getTelefone());
            assertEquals(Role.USER, doador.getRole());
            assertTrue(doador.getAtivo());
        }
    }

    // ==================== toDoadorPJ ====================

    @Nested
    @DisplayName("toDoadorPJ")
    class ToDoadorPJTests {

        @Test
        @DisplayName("Converte DoadorPJRegistroRequest para DoadorPJ")
        void converteParaDoadorPJ() {
            DoadorPJRegistroRequest request = DoadorPJRegistroRequest.builder()
                    .razaoSocial("Empresa LTDA")
                    .nomeRepresentante("José Representante")
                    .email("empresa@test.com")
                    .senha("123456")
                    .cnpj("12.345.678/0001-90")
                    .telefone("11977777777")
                    .cep("12345-678")
                    .logradouro("Av. Principal")
                    .numero("300")
                    .bairro("Industrial")
                    .cidade("São Paulo")
                    .estado("SP")
                    .build();

            DoadorPJ doador = PessoaMapper.toDoadorPJ(request);

            assertNotNull(doador);
            assertEquals("José Representante", doador.getNome());
            assertEquals("empresa@test.com", doador.getEmail());
            assertEquals("12.345.678/0001-90", doador.getCnpj());
            assertEquals("Empresa LTDA", doador.getRazaoSocial());
            assertEquals("11977777777", doador.getTelefone());
            assertEquals(Role.USER, doador.getRole());
            assertTrue(doador.getAtivo());
        }
    }

    // ==================== toResponse ====================

    @Nested
    @DisplayName("toResponse")
    class ToResponseTests {

        @Test
        @DisplayName("Converte Pessoa para UserLoginResponse")
        void converteParaResponse() {
            DoadorPF pessoa = DoadorPF.builder()
                    .id(1)
                    .nome("João")
                    .email("joao@test.com")
                    .telefone("11999999999")
                    .cpf("123.456.789-00")
                    .role(Role.USER)
                    .ativo(true)
                    .endereco("12345-678")
                    .logradouro("Rua Teste")
                    .numero("100")
                    .bairro("Centro")
                    .cidade("São Paulo")
                    .estado("SP")
                    .build();

            UserLoginResponse response = PessoaMapper.toResponse(pessoa);

            assertNotNull(response);
            assertEquals(1, response.id());
            assertEquals("João", response.nome());
            assertEquals("joao@test.com", response.email());
            assertEquals("11999999999", response.telefone());
            assertEquals("DOADOR_PF", response.tipoPessoa());
            assertEquals("USER", response.role());
            assertTrue(response.ativo());
        }

        @Test
        @DisplayName("Converte Pessoa para UserLoginResponse com contadores")
        void converteParaResponseComContadores() {
            DoadorPF pessoa = DoadorPF.builder()
                    .id(1)
                    .nome("João")
                    .email("joao@test.com")
                    .cpf("123.456.789-00")
                    .role(Role.USER)
                    .ativo(true)
                    .build();

            UserLoginResponse response = PessoaMapper.toResponse(pessoa, 5L, 3L, 2L);

            assertNotNull(response);
            assertEquals(5L, response.totalDoacoes());
            assertEquals(3L, response.totalSolicitacoes());
            assertEquals(2L, response.totalTicketsSuporte());
        }
    }

    // ==================== toAdminResponse ====================

    @Nested
    @DisplayName("toAdminResponse")
    class ToAdminResponseTests {

        @Test
        @DisplayName("Converte Pessoa para UsuarioAdminResponse")
        void converteParaAdminResponse() {
            DoadorPF pessoa = DoadorPF.builder()
                    .id(1)
                    .nome("Admin")
                    .email("admin@test.com")
                    .cpf("123.456.789-00")
                    .telefone("11999999999")
                    .role(Role.ADMIN)
                    .ativo(true)
                    .endereco("12345-678")
                    .build();

            UsuarioAdminResponse response = PessoaMapper.toAdminResponse(pessoa);

            assertNotNull(response);
            assertEquals(1, response.id());
            assertEquals("Admin", response.nome());
            assertEquals("admin@test.com", response.email());
            assertEquals("123.456.789-00", response.documento());
            assertEquals("ADMIN", response.role());
            assertTrue(response.ativo());
        }
    }

    // ==================== updatePessoaFromRequest ====================

    @Nested
    @DisplayName("updatePessoaFromRequest")
    class UpdatePessoaFromRequestTests {

        @Test
        @DisplayName("Atualiza campos não-nulos")
        void atualizaCamposNaoNulos() {
            DoadorPF pessoa = DoadorPF.builder()
                    .id(1)
                    .nome("João")
                    .email("joao@test.com")
                    .telefone("11999999999")
                    .build();

            PessoaUpdateRequest request = PessoaUpdateRequest.builder()
                    .email("novo@test.com")
                    .telefone("11988888888")
                    .build();

            PessoaMapper.updatePessoaFromRequest(request, pessoa);

            assertEquals("novo@test.com", pessoa.getEmail());
            assertEquals("11988888888", pessoa.getTelefone());
            assertEquals("João", pessoa.getNome());
        }

        @Test
        @DisplayName("Não atualiza campos nulos")
        void naoAtualizaCamposNulos() {
            DoadorPF pessoa = DoadorPF.builder()
                    .id(1)
                    .nome("João")
                    .email("joao@test.com")
                    .build();

            PessoaUpdateRequest request = PessoaUpdateRequest.builder().build();

            PessoaMapper.updatePessoaFromRequest(request, pessoa);

            assertEquals("joao@test.com", pessoa.getEmail());
            assertEquals("João", pessoa.getNome());
        }
    }

    // ==================== toPessoa (deprecated) ====================

    @Nested
    @DisplayName("toPessoa (deprecated)")
    class ToPessoaTests {

        @Test
        @DisplayName("Converte RegistroRequest para Aluno")
        void converteParaAluno() {
            com.doatec.dto.request.RegistroRequest request = new com.doatec.dto.request.RegistroRequest(
                    "ALUNO", "João Silva", "12345678",
                    "joao@test.com", "Rua A", "11999999999", "123456"
            );

            Pessoa pessoa = PessoaMapper.toPessoa(request);

            assertNotNull(pessoa);
            assertTrue(pessoa instanceof Aluno);
            assertEquals("João Silva", pessoa.getNome());
            assertEquals("joao@test.com", pessoa.getEmail());
        }

        @Test
        @DisplayName("Converte RegistroRequest para DoadorPF")
        void converteParaDoadorPF() {
            com.doatec.dto.request.RegistroRequest request = new com.doatec.dto.request.RegistroRequest(
                    "DOADOR_PF", "Maria Santos", "123.456.789-00",
                    "maria@test.com", "Rua B", "11988888888", "123456"
            );

            Pessoa pessoa = PessoaMapper.toPessoa(request);

            assertNotNull(pessoa);
            assertTrue(pessoa instanceof DoadorPF);
            assertEquals("Maria Santos", pessoa.getNome());
            assertEquals("maria@test.com", pessoa.getEmail());
        }

        @Test
        @DisplayName("Converte RegistroRequest para DoadorPJ")
        void converteParaDoadorPJ() {
            com.doatec.dto.request.RegistroRequest request = new com.doatec.dto.request.RegistroRequest(
                    "DOADOR_PJ", "Empresa LTDA", "12.345.678/0001-90",
                    "empresa@test.com", "Rua C", "11977777777", "123456"
            );

            Pessoa pessoa = PessoaMapper.toPessoa(request);

            assertNotNull(pessoa);
            assertTrue(pessoa instanceof DoadorPJ);
            assertEquals("Empresa LTDA", pessoa.getNome());
            assertEquals("empresa@test.com", pessoa.getEmail());
        }
    }

    // ==================== toResponse com contadores ====================

    @Nested
    @DisplayName("toResponse com contadores")
    class ToResponseComContadoresTests {

        @Test
        @DisplayName("Converte Pessoa para UserLoginResponse com todos os contadores")
        void converteComContadores() {
            DoadorPF pessoa = DoadorPF.builder()
                    .id(1)
                    .nome("João")
                    .email("joao@test.com")
                    .telefone("11999999999")
                    .cpf("123.456.789-00")
                    .role(Role.USER)
                    .ativo(true)
                    .endereco("12345-678")
                    .logradouro("Rua Teste")
                    .numero("100")
                    .bairro("Centro")
                    .cidade("São Paulo")
                    .estado("SP")
                    .build();

            UserLoginResponse response = PessoaMapper.toResponse(pessoa, 10L, 5L, 3L);

            assertNotNull(response);
            assertEquals(10L, response.totalDoacoes());
            assertEquals(5L, response.totalSolicitacoes());
            assertEquals(3L, response.totalTicketsSuporte());
        }
    }
}
