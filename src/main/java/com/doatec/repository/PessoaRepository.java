package com.doatec.repository;

import com.doatec.model.account.Pessoa;
import com.doatec.model.account.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository base para a entidade Pessoa.
 * Suporta herança JPA com estratégia JOINED.
 */
@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, Integer> {

    Optional<Pessoa> findByEmail(String email);

    List<Pessoa> findByRole(Role role);

    List<Pessoa> findByAtivoTrue();

    long countByAtivoTrue();

    long countByRole(Role role);

    boolean existsByRole(Role role);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Integer id);

    Page<Pessoa> findByRoleIn(Collection<Role> roles, Pageable pageable);

    /**
     * Busca pessoas por tipo específico (Aluno, DoadorPF, DoadorPJ).
     * Usa a função TYPE do JPA para filtrar pela classe concreta.
     */
    @Query("SELECT p FROM Pessoa p WHERE TYPE(p) = :tipo")
    <T extends Pessoa> List<T> findByTipo(@Param("tipo") Class<T> tipo);

    /**
     * Busca qualquer pessoa por documento (RA, CPF ou CNPJ).
     * Retorna Optional<Pessoa> que pode ser Aluno, DoadorPF ou DoadorPJ.
     */
    @Query("SELECT p FROM Pessoa p WHERE " +
           "(TYPE(p) = Aluno AND CAST(p.ra AS string) = :documento) OR " +
           "(TYPE(p) = DoadorPF AND CAST(p.cpf AS string) = :documento) OR " +
           "(TYPE(p) = DoadorPJ AND CAST(p.cnpj AS string) = :documento)")
    Optional<Pessoa> findByDocumento(@Param("documento") String documento);
}