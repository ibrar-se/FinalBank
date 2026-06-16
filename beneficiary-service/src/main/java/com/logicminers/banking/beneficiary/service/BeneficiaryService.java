package com.logicminers.banking.beneficiary.service;

import com.logicminers.banking.beneficiary.entity.Beneficiary;
import com.logicminers.banking.beneficiary.repository.BeneficiaryRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BeneficiaryService {

    private final BeneficiaryRepository repository;

    public BeneficiaryService(BeneficiaryRepository repository) {
        this.repository = repository;
    }

    public Beneficiary addBeneficiary(Beneficiary beneficiary) {
        repository.findByIban(beneficiary.getIban()).ifPresent(b -> {
            throw new RuntimeException("Beneficiary with this IBAN already exists");
        });
        return repository.save(beneficiary);
    }

    public List<Beneficiary> getBeneficiariesByUser(String userId) {
        return repository.findByUserId(userId);
    }
}