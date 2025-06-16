package com.doatec.service;

import com.doatec.dtos.SolicitacaoDto;
import com.doatec.model.account.Aluno;
import com.doatec.model.account.Pessoa;
import com.doatec.model.solicitacao.SolicitacaoHardware;
import com.doatec.repository.PessoaRepository;
import com.doatec.repository.SolicitacaoHardwareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Classe de serviço com a lógica de negócio para criar solicitações de hardware.
 */
@Service
public class SolicitacaoService {

    // Repositório para operações de acesso a dados de Pessoa/Aluno.
    @Autowired
    private PessoaRepository pessoaRepository;

    // Repositório para operações de acesso a dados de SolicitacaoHardware.
    @Autowired
    private SolicitacaoHardwareRepository solicitacaoHardwareRepository;

    @Transactional
    public SolicitacaoHardware criarSolicitacao(SolicitacaoDto dto) {

        // Busca o usuário no banco de dados pelo email fornecido no formulário.
        Optional<Pessoa> pessoaOptional = pessoaRepository.findByEmail(dto.getEmail());

        // Se o usuário não for encontrado, lança um erro e interrompe a operação.
        if (pessoaOptional.isEmpty()) {
            throw new RuntimeException("Aluno com email " + dto.getEmail() + " não está cadastrado.");
        }

        // Se for encontrado, obtém o objeto Pessoa.
        Pessoa pessoaEncontrada = pessoaOptional.get();

        // Valida se o nome fornecido no formulário corresponde ao nome cadastrado.
        if (!(pessoaEncontrada.getNome().equals(dto.getNome()))){
            throw new RuntimeException("Nome incorreta!");
        }

        // Valida se a senha fornecida no formulário corresponde à senha cadastrada.
        if (!(pessoaEncontrada.getSenha().equals(dto.getSenha()))){
            throw new RuntimeException("Senha incorreta!");
        }

        // Garante que a pessoa encontrada é de fato um aluno, e não outro tipo de usuário.
        if (!(pessoaEncontrada instanceof Aluno)) {
            throw new RuntimeException("O email " + dto.getEmail() + " já está cadastrado, mas não pertence a um aluno.");
        }

        // Faz a conversão (casting) do objeto Pessoa para Aluno, agora que o tipo foi verificado.
        Aluno aluno = (Aluno) pessoaEncontrada;

        // Valida se o RA fornecido no formulário corresponde ao RA cadastrado para este aluno.
        if (!(aluno.getRa().equals(dto.getRa()))){
            throw new RuntimeException("O RA " + dto.getRa() + " Não é válido!");
        }

        // Atualiza o perfil do aluno com a justificativa e a preferência da solicitação.
        aluno.setJustificativa(dto.getJustificativa());
        aluno.setPreferenciaEquipamento(dto.getPreferenciaEquipamento());


        // Cria uma nova instância do registro de solicitação, vinculando-a ao aluno.
        SolicitacaoHardware novaSolicitacao = new SolicitacaoHardware(aluno);

        // Salva o novo registro de solicitação. A anotação @Transactional garante que as alterações no aluno também sejam salvas.
        return solicitacaoHardwareRepository.save(novaSolicitacao);
    }

}