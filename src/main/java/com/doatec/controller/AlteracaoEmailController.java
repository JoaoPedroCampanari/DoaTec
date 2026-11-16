package com.doatec.controller;

import com.doatec.model.account.Pessoa;
import com.doatec.repository.PessoaRepository;
import com.doatec.service.EmailService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;

/**
 * Controller para alteração do e-mail do usuário.
 */
@RestController
@RequestMapping("/api/users/me/email")
public class AlteracaoEmailController {
    private static final String PENDENTE = AlteracaoEmailController.class.getName() + ".pendente";
    private final PessoaRepository pessoas;
    private final PasswordEncoder senhas;
    private final EmailService mail;
    private final SecureRandom random = new SecureRandom();

    public AlteracaoEmailController(PessoaRepository pessoas, PasswordEncoder senhas, EmailService mail) {
        this.pessoas = pessoas;
        this.senhas = senhas;
        this.mail = mail;
    }

    public record Solicitacao(@NotBlank @Email @Size(max = 120) String novoEmail, @NotBlank String senhaAtual) {}
    public record Confirmacao(@NotBlank String codigo) {}
    private record Pendente(Integer pessoaId, String email, String codigoHash, Instant criadoEm, int tentativas) implements java.io.Serializable {}

    /**
     * Envia um código de confirmação para o novo e-mail.
     */
    @PostMapping("/solicitar")
    public Map<String, String> solicitar(@AuthenticationPrincipal User usuario, @Valid @RequestBody Solicitacao pedido, HttpSession sessao) {
        Pessoa pessoa = pessoas.findByEmail(usuario.getUsername()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (!senhas.matches(pedido.senhaAtual(), pessoa.getSenha())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha atual incorreta.");
        String email = pedido.novoEmail().trim().toLowerCase(Locale.ROOT);
        if (email.equalsIgnoreCase(pessoa.getEmail()) || pessoas.emailJaUtilizado(email)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este e-mail já está em uso.");
        Pendente anterior = (Pendente) sessao.getAttribute(PENDENTE);
        if (anterior != null && anterior.criadoEm().plusSeconds(60).isAfter(Instant.now())) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Aguarde um minuto antes de solicitar outro código.");
        String codigo = String.format("%08d", random.nextInt(100_000_000));
        try { mail.enviarCodigoAlteracaoEmail(email, codigo); }
        catch (Exception ex) { throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Não foi possível enviar o código. Tente novamente."); }
        sessao.setAttribute(PENDENTE, new Pendente(pessoa.getId(), email, senhas.encode(codigo), Instant.now(), 0));
        return Map.of("message", "Código enviado ao novo e-mail.");
    }

    /**
     * Confirma o código e atualiza o e-mail do usuário.
     */
    @PostMapping("/confirmar")
    @Transactional
    public Map<String, String> confirmar(@AuthenticationPrincipal User usuario, @Valid @RequestBody Confirmacao pedido, HttpSession sessao) {
        Pendente pendente = (Pendente) sessao.getAttribute(PENDENTE);
        Pessoa pessoa = pessoas.findByEmail(usuario.getUsername()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (pendente == null || !pendente.pessoaId().equals(pessoa.getId()) || pendente.criadoEm().plusSeconds(600).isBefore(Instant.now())) {
            sessao.removeAttribute(PENDENTE);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código expirado. Solicite outro.");
        }
        if (!senhas.matches(pedido.codigo(), pendente.codigoHash())) {
            if (pendente.tentativas() >= 4) sessao.removeAttribute(PENDENTE);
            else sessao.setAttribute(PENDENTE, new Pendente(pendente.pessoaId(), pendente.email(), pendente.codigoHash(), pendente.criadoEm(), pendente.tentativas() + 1));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código incorreto ou expirado.");
        }
        if (pessoas.emailJaUtilizado(pendente.email())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está em uso.");
        pessoa.setEmail(pendente.email());
        pessoa.markUpdated();
        pessoas.saveAndFlush(pessoa);
        sessao.invalidate();
        return Map.of("message", "E-mail atualizado. Entre novamente.");
    }
}
