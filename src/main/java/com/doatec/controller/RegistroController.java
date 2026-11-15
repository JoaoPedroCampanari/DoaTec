package com.doatec.controller;

import com.doatec.dto.request.AlunoRegistroRequest;
import com.doatec.dto.request.DoadorPFRegistroRequest;
import com.doatec.dto.request.DoadorPJRegistroRequest;
import com.doatec.dto.request.RegistroRequest;
import com.doatec.dto.response.UserLoginResponse;
import com.doatec.mapper.PessoaMapper;
import com.doatec.model.account.Pessoa;
import com.doatec.service.PessoaService;
import com.doatec.service.VerificacaoCadastroService;
import com.doatec.repository.PessoaRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

/**
 * Controlador para o registro de novos usuários.
 *
 * Endpoints especializados:
 * - POST /api/register/aluno - Registro de alunos
 * - POST /api/register/doador-pf - Registro de doadores pessoa física
 * - POST /api/register/doador-pj - Registro de doadores pessoa jurídica
 *
 * Endpoint legado (mantido para compatibilidade):
 * - POST /api/register - Usa RegistroRequest genérico
 */
@RestController
@RequestMapping("/api/register")
public class RegistroController {

    @Autowired
    private PessoaService pessoaService;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private VerificacaoCadastroService verificacaoCadastro;

    public record CodigoCadastroRequest(@NotBlank @Size(max = 120) @jakarta.validation.constraints.Email String email, @NotBlank @Pattern(regexp = "[0-9]{8}") String codigo) {}

    @PostMapping("/enviar-codigo")
    public Map<String, String> enviarCodigo(@Valid @RequestBody ConsultaEmailRequest request) {
        verificacaoCadastro.enviar(request.email());
        return Map.of("message", "Código enviado.");
    }

    @PostMapping("/confirmar-codigo")
    public Map<String, String> confirmarCodigo(@Valid @RequestBody CodigoCadastroRequest request) {
        verificacaoCadastro.confirmar(request.email(), request.codigo());
        return Map.of("message", "E-mail confirmado.");
    }

    public record ConsultaEmailRequest(
            @NotBlank @Size(max = 120)
            @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") String email) {}

    @PostMapping("/verificar-email")
    public Map<String, Boolean> verificarEmail(@Valid @RequestBody ConsultaEmailRequest request) {
        return Map.of("disponivel", !pessoaRepository.emailJaUtilizado(request.email().trim()));
    }

    public enum TipoConsultaDocumento { CPF, CNPJ, RA }

    public record ConsultaDocumentoRequest(
            @jakarta.validation.constraints.NotNull TipoConsultaDocumento tipo,
            @NotBlank @Pattern(regexp = "[0-9]{11,14}") String documento) {}

    @PostMapping("/verificar-documento")
    public Map<String, Boolean> verificarDocumento(@Valid @RequestBody ConsultaDocumentoRequest request) {
        int tamanho = switch (request.tipo()) { case CPF -> 11; case CNPJ -> 14; case RA -> 13; };
        if (request.documento().length() != tamanho) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Documento incompleto.");
        }
        boolean utilizado = switch (request.tipo()) {
            case CPF -> pessoaRepository.cpfJaUtilizado(request.documento());
            case CNPJ -> pessoaRepository.cnpjJaUtilizado(request.documento());
            case RA -> pessoaRepository.raJaUtilizado(request.documento());
        };
        return Map.of("disponivel", !utilizado);
    }

    /**
     * Registro de aluno (beneficiário).
     * Alunos podem solicitar equipamentos disponíveis.
     */
    @PostMapping("/aluno")
    public ResponseEntity<UserLoginResponse> registrarAluno(@Valid @RequestBody AlunoRegistroRequest request) {
        verificacaoCadastro.exigirConfirmacao(request.email());
        Pessoa novaPessoa = pessoaService.registrarAluno(request);
        verificacaoCadastro.consumir();
        UserLoginResponse response = PessoaMapper.toResponse(novaPessoa);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Registro de doador pessoa física.
     * Doadores podem registrar doações de equipamentos.
     */
    @PostMapping("/doador-pf")
    public ResponseEntity<UserLoginResponse> registrarDoadorPF(@Valid @RequestBody DoadorPFRegistroRequest request) {
        verificacaoCadastro.exigirConfirmacao(request.email());
        Pessoa novaPessoa = pessoaService.registrarDoadorPF(request);
        verificacaoCadastro.consumir();
        UserLoginResponse response = PessoaMapper.toResponse(novaPessoa);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Registro de doador pessoa jurídica (empresas).
     * Doadores podem registrar doações de equipamentos.
     */
    @PostMapping("/doador-pj")
    public ResponseEntity<UserLoginResponse> registrarDoadorPJ(@Valid @RequestBody DoadorPJRegistroRequest request) {
        verificacaoCadastro.exigirConfirmacao(request.email());
        Pessoa novaPessoa = pessoaService.registrarDoadorPJ(request);
        verificacaoCadastro.consumir();
        UserLoginResponse response = PessoaMapper.toResponse(novaPessoa);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint legado para compatibilidade.
     * Usa RegistroRequest genérico — será removido em v2.0.
     * Use os endpoints especializados: /aluno, /doador-pf, /doador-pj
     */
    @Deprecated(since = "1.1", forRemoval = true)
    @PostMapping
    public ResponseEntity<UserLoginResponse> registerUser(@Valid @RequestBody RegistroRequest registroRequest) {
        verificacaoCadastro.exigirConfirmacao(registroRequest.email());
        Pessoa novaPessoa = pessoaService.registrarPessoa(registroRequest);
        verificacaoCadastro.consumir();
        UserLoginResponse response = PessoaMapper.toResponse(novaPessoa);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
