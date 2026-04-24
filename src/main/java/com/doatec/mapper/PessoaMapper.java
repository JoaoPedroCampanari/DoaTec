package com.doatec.mapper;

import com.doatec.dto.request.*;
import com.doatec.dto.response.UsuarioAdminResponse;
import com.doatec.dto.response.UserLoginResponse;
import com.doatec.model.account.*;
import lombok.experimental.UtilityClass;

/**
 * Mapper para conversão entre DTOs e entidades de Pessoa.
 * Suporta herança JPA com subclasses Aluno, DoadorPF e DoadorPJ.
 */
@UtilityClass
public class PessoaMapper {

    /**
     * Converte RegistroRequest genérico para Pessoa.
     * Determina a subclasse baseada no tipoPessoa.
     * @deprecated Usar métodos específicos: toAluno, toDoadorPF, toDoadorPJ
     */
    @Deprecated
    public static Pessoa toPessoa(RegistroRequest request) {
        TipoPessoa tipoPessoa = TipoPessoa.valueOf(request.tipoPessoa().toUpperCase());

        return switch (tipoPessoa) {
            case ALUNO -> toAluno(request);
            case DOADOR_PF -> toDoadorPF(request);
            case DOADOR_PJ -> toDoadorPJ(request);
        };
    }

    /**
     * Converte RegistroRequest para Aluno (endpoint legado).
     */
    private static Aluno toAluno(RegistroRequest request) {
        return Aluno.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(request.senha())
                .ra(request.documento())
                .role(Role.USER)
                .endereco(request.endereco() != null ? request.endereco() : "")
                .telefone(request.telefone() != null ? request.telefone() : "")
                .ativo(true)
                .build();
    }

    /**
     * Converte RegistroRequest para DoadorPF (endpoint legado).
     */
    private static DoadorPF toDoadorPF(RegistroRequest request) {
        return DoadorPF.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(request.senha())
                .cpf(request.documento())
                .role(Role.USER)
                .endereco(request.endereco() != null ? request.endereco() : "")
                .telefone(request.telefone() != null ? request.telefone() : "")
                .ativo(true)
                .build();
    }

    /**
     * Converte RegistroRequest para DoadorPJ (endpoint legado).
     */
    private static DoadorPJ toDoadorPJ(RegistroRequest request) {
        return DoadorPJ.builder()
                .nome(request.nome()) // nome representante
                .email(request.email())
                .senha(request.senha())
                .cnpj(request.documento())
                .razaoSocial(request.nome()) // Para o legado, usa nome como razão social
                .role(Role.USER)
                .endereco(request.endereco() != null ? request.endereco() : "")
                .telefone(request.telefone() != null ? request.telefone() : "")
                .ativo(true)
                .build();
    }

    /**
     * Converte AlunoRegistroRequest para Aluno.
     */
    public static Aluno toAluno(AlunoRegistroRequest request) {
        return Aluno.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(request.senha())
                .ra(request.ra())
                .role(Role.USER)
                .endereco(request.cep() != null ? request.cep() : "")
                .logradouro(request.logradouro())
                .numero(request.numero())
                .bairro(request.bairro())
                .cidade(request.cidade())
                .estado(request.estado())
                .telefone(request.telefone() != null ? request.telefone() : "")
                .ativo(true)
                .build();
    }

    /**
     * Converte DoadorPFRegistroRequest para DoadorPF.
     */
    public static DoadorPF toDoadorPF(DoadorPFRegistroRequest request) {
        return DoadorPF.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(request.senha())
                .cpf(request.cpf())
                .role(Role.USER)
                .endereco(request.cep() != null ? request.cep() : "")
                .logradouro(request.logradouro())
                .numero(request.numero())
                .bairro(request.bairro())
                .cidade(request.cidade())
                .estado(request.estado())
                .telefone(request.telefone() != null ? request.telefone() : "")
                .ativo(true)
                .build();
    }

    /**
     * Converte DoadorPJRegistroRequest para DoadorPJ.
     */
    public static DoadorPJ toDoadorPJ(DoadorPJRegistroRequest request) {
        return DoadorPJ.builder()
                .nome(request.nomeRepresentante())
                .email(request.email())
                .senha(request.senha())
                .cnpj(request.cnpj())
                .razaoSocial(request.razaoSocial())
                .role(Role.USER)
                .endereco(request.cep() != null ? request.cep() : "")
                .logradouro(request.logradouro())
                .numero(request.numero())
                .bairro(request.bairro())
                .cidade(request.cidade())
                .estado(request.estado())
                .telefone(request.telefone() != null ? request.telefone() : "")
                .ativo(true)
                .build();
    }

    public static void updatePessoaFromRequest(PessoaUpdateRequest request, Pessoa pessoa) {
        if (request.email() != null) pessoa.setEmail(request.email());
        if (request.senha() != null) pessoa.setSenha(request.senha());
        if (request.endereco() != null) pessoa.setEndereco(request.endereco());
        if (request.telefone() != null) pessoa.setTelefone(request.telefone());
    }

    /**
     * Converte Pessoa para UserLoginResponse.
     * Usa metodos polimorficos getDocumento() e getTipoPessoa() das subclasses.
     */
    public static UserLoginResponse toResponse(Pessoa pessoa) {
        return toResponse(pessoa, 0L, 0L, 0L);
    }

    public static UserLoginResponse toResponse(Pessoa pessoa, Long totalDoacoes, Long totalSolicitacoes, Long totalTicketsSuporte) {
        return UserLoginResponse.fromWithAddress(
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.getEmail(),
                pessoa.getTelefone(),
                pessoa.getTipoPessoa(),
                pessoa.getRole(),
                pessoa.getDocumento(),
                pessoa.getAtivo(),
                pessoa.getEndereco(),
                pessoa.getLogradouro(),
                pessoa.getNumero(),
                pessoa.getBairro(),
                pessoa.getCidade(),
                pessoa.getEstado(),
                totalDoacoes,
                totalSolicitacoes,
                totalTicketsSuporte
        );
    }

    public static UsuarioAdminResponse toAdminResponse(Pessoa pessoa) {
        return UsuarioAdminResponse.from(pessoa);
    }
}