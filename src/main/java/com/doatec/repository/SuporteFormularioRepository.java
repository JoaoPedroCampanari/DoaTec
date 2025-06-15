package com.doatec.repository;

import com.doatec.model.suporte.SuporteFormulario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SuporteFormularioRepository extends JpaRepository<SuporteFormulario, UUID> {

}
