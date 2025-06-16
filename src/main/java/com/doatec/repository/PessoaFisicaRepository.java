package com.doatec.repository;

import com.doatec.model.account.PessoaFisica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PessoaFisicaRepository extends JpaRepository<PessoaFisica, UUID> {
    Optional<PessoaFisica> findByCpf(String cpf);
}