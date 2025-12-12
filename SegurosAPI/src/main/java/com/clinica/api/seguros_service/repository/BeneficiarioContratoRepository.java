package com.clinica.api.seguros_service.repository;

import com.clinica.api.seguros_service.model.BeneficiarioContrato;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BeneficiarioContratoRepository extends JpaRepository<BeneficiarioContrato, Long> {

    List<BeneficiarioContrato> findByContrato_Id(Long contratoId);
}
