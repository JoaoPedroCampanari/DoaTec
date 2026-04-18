package com.doatec.repository;

import com.doatec.model.account.DoadorPJ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para a entidade DoadorPJ.
 */
@Repository
public interface DoadorPJRepository extends JpaRepository<DoadorPJ, Integer> {

    Optional<DoadorPJ> findByCnpj(String cnpj);

    Optional<DoadorPJ> findByEmail(String email);

    List<DoadorPJ> findByAtivoTrue();

    boolean existsByCnpj(String cnpj);

    boolean existsByEmail(String email);
}