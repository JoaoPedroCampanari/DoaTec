package com.doatec.repository;

import com.doatec.model.account.PessoaJuridica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PessoaJuridicaRepository extends JpaRepository<PessoaJuridica, UUID> {
    Optional<PessoaJuridica> findByCnpj(String cnpj);
}