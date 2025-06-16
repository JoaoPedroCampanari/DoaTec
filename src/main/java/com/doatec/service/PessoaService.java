package com.doatec.service;

import com.doatec.dtos.LoginDto;
import com.doatec.dtos.RegistroDto;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.PessoaFisica;
import com.doatec.model.account.PessoaJuridica;
import com.doatec.model.account.Aluno;
import com.doatec.model.account.TipoUsuario;
import com.doatec.repository.AlunoRepository;
import com.doatec.repository.PessoaFisicaRepository;
import com.doatec.repository.PessoaJuridicaRepository;
import com.doatec.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;


/**
 * Classe de serviço para gerenciar a lógica de negócio de Pessoas (usuários),
 * incluindo registro e verificação de credenciais.
 */
@Service
public class PessoaService {

    // Repositório para operações genéricas de Pessoa, como busca por email.
    @Autowired
    private PessoaRepository pessoaRepository;

    // Repositório para operações específicas de PessoaFisica, como busca por CPF.
    @Autowired
    private PessoaFisicaRepository pessoaFisicaRepository;

    // Repositório para operações específicas de PessoaJuridica, como busca por CNPJ.
    @Autowired
    private PessoaJuridicaRepository pessoaJuridicaRepository;

    // Repositório para operações específicas de Aluno, como busca por RA.
    @Autowired
    private AlunoRepository alunoRepository;


    public boolean verificarCredenciais(LoginDto loginDto) {
        // Busca um usuário pelo email fornecido.
        Optional<Pessoa> pessoaOptional = pessoaRepository.findByEmail(loginDto.getEmail());

        // Se nenhum usuário for encontrado, as credenciais são inválidas.
        if (pessoaOptional.isEmpty()) {
            return false;
        }

        // Se o usuário for encontrado, obtém o objeto.
        Pessoa pessoa = pessoaOptional.get();
        // Compara a senha do banco com a senha fornecida e retorna true (se iguais) ou false (se diferentes).
        return pessoa.getSenha().equals(loginDto.getSenha());
    }

    @Transactional
    public Pessoa registrarPessoa(RegistroDto registroDto){
        // --- INÍCIO DAS VALIDAÇÕES ---

        // 1. Validação de Email: Garante que o email seja único em todo o sistema.
        if (pessoaRepository.findByEmail(registroDto.getEmail()).isPresent()) {
            throw new RuntimeException("O email informado já está cadastrado.");
        }

        Pessoa novaPessoa;
        TipoUsuario tipoUsuarioEnum;

        // Validação de Documento: Garante que o campo de identificação (CPF, CNPJ ou RA) não está em branco.
        if (registroDto.getIdentidade() == null || registroDto.getIdentidade().isBlank()) {
            throw new RuntimeException("O documento de identificação deve ser preenchido.");
        }

        // Verifica o tipo de usuário para criar a entidade correta.
        if (registroDto.getTipoUsuario().equals("pf")) {
            // 2. Validação de CPF: Garante que o CPF seja único entre as Pessoas Físicas.
            if (pessoaFisicaRepository.findByCpf(registroDto.getIdentidade()).isPresent()) {
                throw new RuntimeException("O CPF informado já está cadastrado.");
            }

            // Define o tipo de usuário e cria uma nova instância de PessoaFisica.
            tipoUsuarioEnum = TipoUsuario.DOADOR_PF;
            novaPessoa = new PessoaFisica(
                    registroDto.getNome(),
                    registroDto.getEmail(),
                    registroDto.getSenha(),
                    registroDto.getEndereco(),
                    "", // Telefone é opcional
                    tipoUsuarioEnum,
                    registroDto.getIdentidade() // CPF
            );
        }
        else if (registroDto.getTipoUsuario().equals("pj")) {
            // 3. Validação de CNPJ: Garante que o CNPJ seja único entre as Pessoas Jurídicas.
            if (pessoaJuridicaRepository.findByCnpj(registroDto.getIdentidade()).isPresent()) {
                throw new RuntimeException("O CNPJ informado já está cadastrado.");
            }

            // Define o tipo de usuário e cria uma nova instância de PessoaJuridica.
            tipoUsuarioEnum = TipoUsuario.DOADOR_PJ;
            novaPessoa = new PessoaJuridica(
                    registroDto.getNome(),
                    registroDto.getEmail(),
                    registroDto.getSenha(),
                    registroDto.getEndereco(),
                    "", // Telefone é opcional
                    tipoUsuarioEnum,
                    registroDto.getIdentidade() // CNPJ
            );
        }
        else { // Assumindo que o tipo é "aluno"
            // 4. Validação de RA: Garante que o RA seja único entre os Alunos.
            if (alunoRepository.findByRa(registroDto.getIdentidade()).isPresent()) {
                throw new RuntimeException("O RA informado já está cadastrado.");
            }

            // Define o tipo de usuário e cria uma nova instância de Aluno.
            tipoUsuarioEnum = TipoUsuario.ALUNO;
            Aluno aluno = new Aluno(
                    registroDto.getNome(),
                    registroDto.getEmail(),
                    registroDto.getSenha(),
                    registroDto.getEndereco(),
                    "", // Telefone é opcional
                    tipoUsuarioEnum,
                    registroDto.getIdentidade() // RA
            );

            // Define valores iniciais vazios para os campos de solicitação do aluno.
            aluno.setJustificativa("");
            aluno.setPreferenciaEquipamento("");
            novaPessoa = aluno;
        }

        // Salva a nova pessoa (seja PF, PJ ou Aluno) no banco de dados.
        return pessoaRepository.save(novaPessoa);
    }

}