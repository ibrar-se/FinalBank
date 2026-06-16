package com.logicminers.banking.beneficiary.repository;

import com.logicminers.banking.beneficiary.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    List<Beneficiary> findByUserId(String userId);
    Optional<Beneficiary> findByIban(String iban);
}