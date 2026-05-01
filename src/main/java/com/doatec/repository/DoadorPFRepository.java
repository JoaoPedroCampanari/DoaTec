package com.doatec.repository;

import com.doatec.model.account.DoadorPF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para a entidade DoadorPF.
 */
@Repository
public interface DoadorPFRepository extends JpaRepository<DoadorPF, Integer> {

    Optional<DoadorPF> findByCpf(String cpf);

    Optional<DoadorPF> findByEmail(String email);

    List<DoadorPF> findByAtivoTrue();

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Integer id);

    boolean existsByEmail(String email);
}