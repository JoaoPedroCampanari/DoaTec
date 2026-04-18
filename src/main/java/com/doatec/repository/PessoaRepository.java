package com.doatec.repository;

import com.doatec.model.account.Pessoa;
import com.doatec.model.account.Role;
import com.doatec.model.account.TipoPessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, Integer> {
    Optional<Pessoa> findByEmail(String email);
    Optional<Pessoa> findByDocumento(String documento);
    List<Pessoa> findByTipoPessoa(TipoPessoa tipoPessoa);
    List<Pessoa> findByRole(Role role);
    List<Pessoa> findByAtivoTrue();
}