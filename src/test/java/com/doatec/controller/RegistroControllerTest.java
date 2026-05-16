package com.doatec.controller;

import com.doatec.dto.request.AlunoRegistroRequest;
import com.doatec.dto.request.DoadorPFRegistroRequest;
import com.doatec.dto.request.DoadorPJRegistroRequest;
import com.doatec.dto.request.RegistroRequest;
import com.doatec.dto.response.UserLoginResponse;
import com.doatec.model.account.Aluno;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.DoadorPJ;
import com.doatec.model.account.Pessoa;
import com.doatec.service.PessoaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegistroController")
class RegistroControllerTest {

    @Mock
    private PessoaService pessoaService;

    @InjectMocks
    private RegistroController registroController;

    // --- Fixtures ---

    private AlunoRegistroRequest buildAlunoRequest() {
        return AlunoRegistroRequest.builder()
                .nome("Maria Silva")
                .email("maria@teste.com")
                .senha("senha123")
                .ra("RA000123456")
                .telefone("11999999999")
                .cep("01001000")
                .logradouro("Praça da Sé")
                .numero("100")
                .bairro("Sé")
                .cidade("São Paulo")
                .estado("SP")
                .build();
    }

    private DoadorPFRegistroRequest buildDoadorPFRequest() {
        return DoadorPFRegistroRequest.builder()
                .nome("João Souza")
                .email("joao@teste.com")
                .senha("senha123")
                .cpf("529.982.247-25")
                .telefone("11988887777")
                .cep("01310100")
                .logradouro("Avenida Paulista")
                .numero("1578")
                .bairro("Bela Vista")
                .cidade("São Paulo")
                .estado("SP")
                .build();
    }

    private DoadorPJRegistroRequest buildDoadorPJRequest() {
        return DoadorPJRegistroRequest.builder()
                .razaoSocial("TechDoe LTDA")
                .nomeRepresentante("Carlos Empresa")
                .email("contato@techdoe.com")
                .senha("senha123")
                .cnpj("11.222.333/0001-81")
                .telefone("1133334444")
                .cep("04547000")
                .logradouro("Rua Funchal")
                .numero("500")
                .bairro("Vila Olímpia")
                .cidade("São Paulo")
                .estado("SP")
                .build();
    }

    private RegistroRequest buildRegistroRequest() {
        return RegistroRequest.builder()
                .tipoPessoa("ALUNO")
                .nome("Ana Legado")
                .documento("RA999999999")
                .email("ana.legado@teste.com")
                .endereco("01001000")
                .telefone("11977776666")
                .senha("senha123")
                .build();
    }

    private Aluno buildAluno() {
        Aluno aluno = Aluno.builder()
                .nome("Maria Silva")
                .email("maria@teste.com")
                .ra("RA000123456")
                .telefone("11999999999")
                .endereco("01001000")
                .logradouro("Praça da Sé")
                .numero("100")
                .bairro("Sé")
                .cidade("São Paulo")
                .estado("SP")
                .ativo(true)
                .build();
        aluno.setId(10);
        return aluno;
    }

    private DoadorPF buildDoadorPF() {
        DoadorPF doador = DoadorPF.builder()
                .nome("João Souza")
                .email("joao@teste.com")
                .cpf("529.982.247-25")
                .telefone("11988887777")
                .endereco("01310100")
                .logradouro("Avenida Paulista")
                .numero("1578")
                .bairro("Bela Vista")
                .cidade("São Paulo")
                .estado("SP")
                .ativo(true)
                .build();
        doador.setId(20);
        return doador;
    }

    private DoadorPJ buildDoadorPJ() {
        DoadorPJ doador = DoadorPJ.builder()
                .nome("Carlos Empresa")
                .email("contato@techdoe.com")
                .cnpj("11.222.333/0001-81")
                .razaoSocial("TechDoe LTDA")
                .telefone("1133334444")
                .endereco("04547000")
                .logradouro("Rua Funchal")
                .numero("500")
                .bairro("Vila Olímpia")
                .cidade("São Paulo")
                .estado("SP")
                .ativo(true)
                .build();
        doador.setId(30);
        return doador;
    }

    // --- Testes ---

    @Nested
    @DisplayName("POST /api/register/aluno")
    class RegistrarAlunoTests {

        @Test
        @DisplayName("Deve registrar aluno e retornar 201 com UserLoginResponse")
        void registrarAluno_sucesso() {
            AlunoRegistroRequest request = buildAlunoRequest();
            Aluno aluno = buildAluno();

            when(pessoaService.registrarAluno(any(AlunoRegistroRequest.class))).thenReturn(aluno);

            ResponseEntity<UserLoginResponse> response = registroController.registrarAluno(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());

            UserLoginResponse body = response.getBody();
            assertEquals(10, body.id());
            assertEquals("Maria Silva", body.nome());
            assertEquals("maria@teste.com", body.email());
            assertEquals("11999999999", body.telefone());
            assertEquals("ALUNO", body.tipoPessoa());
            assertEquals("RA000123456", body.documento());
            assertTrue(body.ativo());

            verify(pessoaService).registrarAluno(request);
        }

        @Test
        @DisplayName("Deve delegar validacao para PessoaService")
        void registrarAluno_deveDelegarParaService() {
            AlunoRegistroRequest request = buildAlunoRequest();
            Aluno aluno = buildAluno();

            when(pessoaService.registrarAluno(request)).thenReturn(aluno);

            registroController.registrarAluno(request);

            verify(pessoaService, times(1)).registrarAluno(request);
            verifyNoMoreInteractions(pessoaService);
        }
    }

    @Nested
    @DisplayName("POST /api/register/doador-pf")
    class RegistrarDoadorPFTests {

        @Test
        @DisplayName("Deve registrar doador PF e retornar 201 com UserLoginResponse")
        void registrarDoadorPF_sucesso() {
            DoadorPFRegistroRequest request = buildDoadorPFRequest();
            DoadorPF doador = buildDoadorPF();

            when(pessoaService.registrarDoadorPF(any(DoadorPFRegistroRequest.class))).thenReturn(doador);

            ResponseEntity<UserLoginResponse> response = registroController.registrarDoadorPF(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());

            UserLoginResponse body = response.getBody();
            assertEquals(20, body.id());
            assertEquals("João Souza", body.nome());
            assertEquals("joao@teste.com", body.email());
            assertEquals("11988887777", body.telefone());
            assertEquals("DOADOR_PF", body.tipoPessoa());
            assertEquals("529.982.247-25", body.documento());
            assertTrue(body.ativo());

            verify(pessoaService).registrarDoadorPF(request);
        }

        @Test
        @DisplayName("Deve delegar validacao para PessoaService")
        void registrarDoadorPF_deveDelegarParaService() {
            DoadorPFRegistroRequest request = buildDoadorPFRequest();
            DoadorPF doador = buildDoadorPF();

            when(pessoaService.registrarDoadorPF(request)).thenReturn(doador);

            registroController.registrarDoadorPF(request);

            verify(pessoaService, times(1)).registrarDoadorPF(request);
            verifyNoMoreInteractions(pessoaService);
        }
    }

    @Nested
    @DisplayName("POST /api/register/doador-pj")
    class RegistrarDoadorPJTests {

        @Test
        @DisplayName("Deve registrar doador PJ e retornar 201 com UserLoginResponse")
        void registrarDoadorPJ_sucesso() {
            DoadorPJRegistroRequest request = buildDoadorPJRequest();
            DoadorPJ doador = buildDoadorPJ();

            when(pessoaService.registrarDoadorPJ(any(DoadorPJRegistroRequest.class))).thenReturn(doador);

            ResponseEntity<UserLoginResponse> response = registroController.registrarDoadorPJ(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());

            UserLoginResponse body = response.getBody();
            assertEquals(30, body.id());
            assertEquals("Carlos Empresa", body.nome());
            assertEquals("contato@techdoe.com", body.email());
            assertEquals("1133334444", body.telefone());
            assertEquals("DOADOR_PJ", body.tipoPessoa());
            assertEquals("11.222.333/0001-81", body.documento());
            assertTrue(body.ativo());

            verify(pessoaService).registrarDoadorPJ(request);
        }

        @Test
        @DisplayName("Deve delegar validacao para PessoaService")
        void registrarDoadorPJ_deveDelegarParaService() {
            DoadorPJRegistroRequest request = buildDoadorPJRequest();
            DoadorPJ doador = buildDoadorPJ();

            when(pessoaService.registrarDoadorPJ(request)).thenReturn(doador);

            registroController.registrarDoadorPJ(request);

            verify(pessoaService, times(1)).registrarDoadorPJ(request);
            verifyNoMoreInteractions(pessoaService);
        }
    }

    @Nested
    @DisplayName("POST /api/register (legado)")
    class RegisterUserLegacyTests {

        @Test
        @DisplayName("Deve registrar pessoa via endpoint legado e retornar 201")
        void registerUser_legado_sucesso() {
            RegistroRequest request = buildRegistroRequest();
            Aluno aluno = buildAluno();

            when(pessoaService.registrarPessoa(any(RegistroRequest.class))).thenReturn(aluno);

            ResponseEntity<UserLoginResponse> response = registroController.registerUser(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());

            UserLoginResponse body = response.getBody();
            assertEquals(10, body.id());
            assertEquals("Maria Silva", body.nome());
            assertEquals("maria@teste.com", body.email());

            verify(pessoaService).registrarPessoa(request);
        }
    }
}
