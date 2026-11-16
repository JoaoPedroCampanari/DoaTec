package com.doatec.service;

import com.doatec.repository.PessoaRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;

/**
 * Serviço para confirmação do e-mail de cadastro.
 */
@Service
public class VerificacaoCadastroService {
    private static final String CHAVE = VerificacaoCadastroService.class.getName() + ".codigo";
    private final PessoaRepository pessoas;
    private final EmailService mail;
    private final PasswordEncoder encoder;
    private final HttpServletRequest request;
    private final SecureRandom random = new SecureRandom();

    public VerificacaoCadastroService(PessoaRepository pessoas, EmailService mail, PasswordEncoder encoder, HttpServletRequest request) {
        this.pessoas = pessoas; this.mail = mail; this.encoder = encoder; this.request = request;
    }
    private record Estado(String email, String hash, Instant criado, int tentativas, boolean confirmado) implements java.io.Serializable {}
    /**
     * Padroniza o e-mail informado.
     */
    private String normalizar(String email) { return email.trim().toLowerCase(Locale.ROOT); }

    /**
     * Envia o código de confirmação para o e-mail do cadastro.
     */
    public void enviar(String email) {
        email = normalizar(email);
        HttpSession sessao = request.getSession(true);
        Estado anterior = (Estado) sessao.getAttribute(CHAVE);
        if (anterior != null && anterior.criado().plusSeconds(60).isAfter(Instant.now()))
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Aguarde um minuto antes de solicitar outro código.");
        if (pessoas.emailJaUtilizado(email)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este e-mail já está cadastrado.");
        String codigo = String.format("%08d", random.nextInt(100_000_000));
        try { mail.enviarCodigoCadastro(email, codigo); }
        catch (Exception e) { throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Não foi possível enviar o código. Tente novamente."); }
        sessao.setAttribute(CHAVE, new Estado(email, encoder.encode(codigo), Instant.now(), 0, false));
    }

    /**
     * Valida o código enviado ao e-mail.
     */
    public void confirmar(String email, String codigo) {
        HttpSession sessao = request.getSession(false);
        Estado estado = sessao == null ? null : (Estado) sessao.getAttribute(CHAVE);
        if (estado == null || !estado.email().equals(normalizar(email)) || !estado.criado().plusSeconds(600).isAfter(Instant.now())) {
            if (sessao != null) sessao.removeAttribute(CHAVE);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código expirado. Solicite outro.");
        }
        if (!encoder.matches(codigo, estado.hash())) {
            if (estado.tentativas() >= 4) sessao.removeAttribute(CHAVE);
            else sessao.setAttribute(CHAVE, new Estado(estado.email(), estado.hash(), estado.criado(), estado.tentativas() + 1, false));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código incorreto ou expirado.");
        }
        sessao.setAttribute(CHAVE, new Estado(estado.email(), estado.hash(), Instant.now(), estado.tentativas(), true));
    }

    /**
     * Verifica se o e-mail foi confirmado antes do cadastro.
     */
    public void exigirConfirmacao(String email) {
        HttpSession sessao = request.getSession(false);
        Estado estado = sessao == null ? null : (Estado) sessao.getAttribute(CHAVE);
        if (estado == null || !estado.confirmado() || !estado.email().equals(normalizar(email)) || !estado.criado().plusSeconds(1800).isAfter(Instant.now()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Confirme seu e-mail antes de criar a conta.");
    }
    /**
     * Remove a confirmação de e-mail da sessão.
     */
    public void consumir() {
        HttpSession sessao = request.getSession(false);
        if (sessao != null) sessao.removeAttribute(CHAVE);
    }
}
