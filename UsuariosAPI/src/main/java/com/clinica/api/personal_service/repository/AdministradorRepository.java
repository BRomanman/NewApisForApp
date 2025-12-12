package com.clinica.api.personal_service.repository;

import com.clinica.api.personal_service.model.Administrador;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    Optional<Administrador> findByCorreoAndActivoTrue(String correo);
    Optional<Administrador> findByIdAndActivoTrue(Long id);
}
