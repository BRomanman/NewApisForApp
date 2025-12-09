package com.clinica.api.seguros_service.repository;

import com.clinica.api.seguros_service.model.Seguro;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeguroRepository extends JpaRepository<Seguro, Long> {
}
