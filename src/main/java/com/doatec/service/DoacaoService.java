package com.doatec.service;


import com.doatec.dtos.DoacaoDto;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.PessoaFisica;
import com.doatec.model.account.PessoaJuridica;
import com.doatec.model.donation.Doacao;
import com.doatec.model.donation.ItemDoado;
import com.doatec.model.donation.StatusDoacao;
import com.doatec.repository.DoacaoRepository;
import com.doatec.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


/**
 * Classe de serviço responsável pela lógica de negócio para registrar novas doações.
 */
@Service
public class DoacaoService {

    // Injeção de dependência para o repositório de Doacao, para salvar os dados da doação.
    @Autowired
    private DoacaoRepository doacaoRepository;

    // Injeção de dependência para o repositório de Pessoa, para buscar e validar os doadores.
    @Autowired
    private PessoaRepository pessoaRepository;

    @Transactional
    public Doacao registrarDoacao(DoacaoDto dto) {
        // Busca o doador no banco de dados pelo email fornecido no DTO.
        Optional<Pessoa> doadorOptional = pessoaRepository.findByEmail(dto.getEmail());

        // Se o doador não for encontrado, lança uma exceção para interromper o processo.
        if (doadorOptional.isEmpty()) {
            throw new RuntimeException("Doador com email " + dto.getEmail() + " não está cadastrado. Por favor, registre-se primeiro.");
        }

        // Se o doador for encontrado, obtém o objeto Pessoa.
        Pessoa doador = doadorOptional.get();

        // Inicia a validação do tipo de documento (CPF ou CNPJ).
        if (dto.getTipoDocumento().equals("cpf")) {
            // 1. Verifica se o doador encontrado é realmente uma PessoaFisica.
            if (!(doador instanceof PessoaFisica)) {
                throw new RuntimeException("O usuário encontrado com este email não é uma Pessoa Física.");
            }
            // 2. Faz a conversão (casting) segura do objeto para PessoaFisica.
            PessoaFisica doadorPf = (PessoaFisica) doador;
            // 3. Valida se o CPF do banco é igual ao CPF fornecido no formulário.
            if (!doadorPf.getCpf().equals(dto.getNumeroDocumento())) {
                throw new RuntimeException("O CPF informado não corresponde ao CPF cadastrado para este email.");
            }
        } else if (dto.getTipoDocumento().equals("cnpj")) {
            // 1. Verifica se o doador encontrado é realmente uma PessoaJuridica.
            if (!(doador instanceof PessoaJuridica)) {
                throw new RuntimeException("O usuário encontrado com este email não é uma Pessoa Jurídica.");
            }
            // 2. Faz a conversão (casting) segura do objeto para PessoaJuridica.
            PessoaJuridica doadorPj = (PessoaJuridica) doador;
            // 3. Valida se o CNPJ do banco é igual ao CNPJ fornecido no formulário.
            if (!doadorPj.getCnpj().equals(dto.getNumeroDocumento())) {
                throw new RuntimeException("O CNPJ informado não corresponde ao CNPJ cadastrado para este email.");
            }
        }

        // Cria uma nova instância da entidade Doacao que será salva.
        Doacao novaDoacao = new Doacao();

        // Valida se o nome informado no formulário é o mesmo do cadastro.
        if (!(doador.getNome().equals(dto.getNome()))){
            throw new RuntimeException("Nome incorreta!");
        }

        // Se um telefone foi informado no formulário, atualiza o cadastro do doador.
        if (dto.getTelefone() != null){
            doador.setTelefone(dto.getTelefone());
        }

        // Define os atributos da nova doação.
        novaDoacao.setDoador(doador); // Associa o doador à doação.
        novaDoacao.setPreferenciaEntrega(dto.getPreferenciaEntrega()); // Define a preferência de entrega.
        novaDoacao.setStatus(StatusDoacao.EM_ANALISE); // Define o status inicial.

        // Cria a instância do item que está sendo doado.
        ItemDoado itemDoado = new ItemDoado();
        itemDoado.setDoacao(novaDoacao); // Associa o item à doação.
        itemDoado.setTipoItem(dto.getTipoItem());
        itemDoado.setDescricao(dto.getDescricaoItem());

        // Adiciona o item à lista de itens da doação.
        novaDoacao.getItens().add(itemDoado);

        // Salva a doação no banco de dados. O ItemDoado será salvo junto por causa do Cascade.
        return doacaoRepository.save(novaDoacao);
    }
}