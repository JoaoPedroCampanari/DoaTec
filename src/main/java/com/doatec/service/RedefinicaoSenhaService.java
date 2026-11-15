package com.doatec.service;

import com.doatec.model.account.Pessoa;
import com.doatec.repository.PessoaRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class RedefinicaoSenhaService {
    private static final String CHAVE = RedefinicaoSenhaService.class.getName() + ".pendente";
    private static final long JANELA_SEGUNDOS = 900;
    private final PessoaRepository pessoas;
    private final EmailService emailService;
    private final PasswordEncoder encoder;
    private final HttpServletRequest request;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Limite> limites = new HashMap<>();
    private record Limite(Instant inicio, int quantidade) {}
    private record Pendente(Integer pessoaId, String email, String senhaAnterior, String codigoHash, Instant criado, int tentativas) implements java.io.Serializable {}

    public RedefinicaoSenhaService(PessoaRepository pessoas, EmailService emailService, PasswordEncoder encoder, HttpServletRequest request) {
        this.pessoas = pessoas; this.emailService = emailService; this.encoder = encoder; this.request = request;
    }

    private synchronized void limitar(String chave, int maximo) {
        Instant agora = Instant.now();
        limites.entrySet().removeIf(e -> !e.getValue().inicio().plusSeconds(JANELA_SEGUNDOS).isAfter(agora));
        Limite anterior = limites.get(chave);
        if (anterior != null && anterior.quantidade() >= maximo)
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Muitas tentativas. Aguarde alguns minutos e tente novamente.");
        limites.put(chave, anterior == null ? new Limite(agora, 1) : new Limite(anterior.inicio(), anterior.quantidade() + 1));
    }

    public void solicitar(String email) {
        String normalizado = email.trim().toLowerCase(Locale.ROOT);
        limitar("solicitar-ip:" + request.getRemoteAddr(), 20);
        limitar("solicitar-email:" + normalizado, 5);
        HttpSession sessao = request.getSession(true);
        synchronized (sessao) {
            sessao.removeAttribute(CHAVE);
            var pessoa = pessoas.findByEmailIgnoreCase(normalizado).filter(p -> Boolean.TRUE.equals(p.getAtivo()));
            String codigo = String.format("%08d", random.nextInt(100_000_000));
            String hash = encoder.encode(codigo);
            if (pessoa.isPresent()) {
                Pessoa conta = pessoa.get();
                try {
                    emailService.enviarCodigoRedefinicaoSenha(conta.getEmail(), codigo);
                    sessao.setAttribute(CHAVE, new Pendente(conta.getId(), conta.getEmail(), conta.getSenha(), hash, Instant.now(), 0));
                } catch (Exception e) {
                    // A resposta não revela se a conta existe ou se o envio falhou.
                    org.slf4j.LoggerFactory.getLogger(RedefinicaoSenhaService.class).warn("Falha ao enviar código de redefinição.", e);
                }
            }
        }
    }

    @Transactional
    public void confirmar(String codigo, String novaSenha) {
        limitar("confirmar-ip:" + request.getRemoteAddr(), 30);
        HttpSession sessao = request.getSession(false);
        if (sessao == null) throw codigoInvalido();
        synchronized (sessao) {
            Pendente pendente = (Pendente) sessao.getAttribute(CHAVE);
            if (pendente == null || !pendente.criado().plusSeconds(600).isAfter(Instant.now())) {
                sessao.removeAttribute(CHAVE);
                throw codigoInvalido();
            }
            if (!encoder.matches(codigo, pendente.codigoHash())) {
                if (pendente.tentativas() >= 4) sessao.removeAttribute(CHAVE);
                else sessao.setAttribute(CHAVE, new Pendente(pendente.pessoaId(), pendente.email(), pendente.senhaAnterior(), pendente.codigoHash(), pendente.criado(), pendente.tentativas() + 1));
                throw codigoInvalido();
            }
            Pessoa conta = pessoas.findByIdForPasswordReset(pendente.pessoaId()).orElseThrow(RedefinicaoSenhaService::codigoInvalido);
            if (!Boolean.TRUE.equals(conta.getAtivo()) || !conta.getEmail().equals(pendente.email()) || !conta.getSenha().equals(pendente.senhaAnterior())) {
                sessao.removeAttribute(CHAVE);
                throw codigoInvalido();
            }
            conta.setSenha(encoder.encode(novaSenha));
            conta.markUpdated();
            pessoas.saveAndFlush(conta);
            sessao.removeAttribute(CHAVE);
        }
    }
    private static ResponseStatusException codigoInvalido() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido ou expirado.");
    }
}
