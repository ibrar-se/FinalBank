package com.logicminers.banking.beneficiary.controller;

import com.logicminers.banking.beneficiary.entity.Beneficiary;
import com.logicminers.banking.beneficiary.service.BeneficiaryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryService service;

    public BeneficiaryController(BeneficiaryService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Beneficiary> add(@Valid @RequestBody Beneficiary beneficiary) {
        return ResponseEntity.ok(service.addBeneficiary(beneficiary));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Beneficiary>> getByUser(@PathVariable String userId) {
        return ResponseEntity.ok(service.getBeneficiariesByUser(userId));
    }
}