package com.doatec.service;

import com.doatec.model.account.Pessoa;
import com.doatec.model.account.Role;
import com.doatec.model.arquivo.ArquivoUpload;
import com.doatec.repository.ArquivoUploadRepository;
import com.doatec.repository.DoacaoRepository;
import com.doatec.repository.PessoaRepository;
import com.doatec.repository.SolicitacaoHardwareRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.Arrays;

@Service
public class ArquivoService {
    private final ArquivoUploadRepository arquivos;
    private final PessoaRepository pessoas;
    private final DoacaoRepository doacoes;
    private final SolicitacaoHardwareRepository solicitacoes;
    public ArquivoService(ArquivoUploadRepository arquivos, PessoaRepository pessoas, DoacaoRepository doacoes, SolicitacaoHardwareRepository solicitacoes) {
        this.arquivos = arquivos; this.pessoas = pessoas; this.doacoes = doacoes; this.solicitacoes = solicitacoes;
    }
    @Transactional
    public void registrar(String nome, String email, String tipo, String nomeOriginal) {
        Pessoa pessoa = pessoas.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        arquivos.saveAndFlush(new ArquivoUpload(nome, pessoa, tipo, limparNome(nomeOriginal)));
    }
    private String limparNome(String original) {
        if (original == null) return null;
        String limpo = original.replace('\\', '/');
        limpo = limpo.substring(limpo.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "").trim();
        if (limpo.isEmpty()) return null;
        return limpo.length() > 200 ? limpo.substring(limpo.length() - 200) : limpo;
    }
    @Transactional(readOnly = true)
    public String nomeOriginal(String url) {
        if (url == null || !url.startsWith("/uploads/")) return null;
        return arquivos.findById(url.substring("/uploads/".length())).map(ArquivoUpload::getNomeOriginal).orElse(null);
    }
    @Transactional(readOnly = true)
    public boolean podeLer(String nome, String email) {
        Pessoa pessoa = pessoas.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (pessoa.getRole() == Role.ADMIN || pessoa.getRole() == Role.SUPER_ADMIN) return true;
        return pertence(nome, pessoa);
    }
    @Transactional(readOnly = true)
    public void validarVinculo(String urls, Pessoa proprietario) {
        if (urls == null || urls.isBlank()) return;
        String[] lista = urls.split(",", -1);
        for (String url : lista) {
            String valor = url.trim();
            if (!valor.matches("/uploads/[a-fA-F0-9-]{36}\\.[a-zA-Z0-9]{1,10}") || !pertence(valor.substring("/uploads/".length()), proprietario))
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Arquivo inválido ou sem permissão.");
        }
    }
    private boolean pertence(String nome, Pessoa pessoa) {
        var registro = arquivos.findById(nome);
        if (registro.isPresent()) return registro.get().getProprietario().getId().equals(pessoa.getId());
        String url = "/uploads/" + nome;
        boolean doacao = doacoes.findByDoadorId(pessoa.getId()).stream()
                .anyMatch(d -> d.getUrlFoto() != null && Arrays.stream(d.getUrlFoto().split(",")).anyMatch(u -> u.trim().equals(url)));
        return doacao || solicitacoes.findByAlunoId(pessoa.getId()).stream()
                .anyMatch(s -> url.equals(s.getUrlComprovante()));
    }
}
