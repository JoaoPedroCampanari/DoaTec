package com.doatec.repository;

import com.doatec.model.account.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para a entidade Aluno.
 */
@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Integer> {

    Optional<Aluno> findByRa(String ra);

    Optional<Aluno> findByEmail(String email);

    List<Aluno> findByAtivoTrue();

    boolean existsByRa(String ra);

    boolean existsByRaAndIdNot(String ra, Integer id);

    boolean existsByEmail(String email);
}