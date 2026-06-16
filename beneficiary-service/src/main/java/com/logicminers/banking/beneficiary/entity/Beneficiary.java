package com.logicminers.banking.beneficiary.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "beneficiaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "User ID is required")
    private String userId; // The owner of the address book

    @NotBlank(message = "Beneficiary name is required")
    private String fullName;

    @NotBlank(message = "IBAN is required")
    @Size(min = 24, max = 24, message = "Saudi IBAN must be exactly 24 characters long")
    @Pattern(regexp = "^SA\\d{22}$", message = "Invalid Saudi IBAN format. Must start with SA followed by 22 digits")
    @Column(unique = true, nullable = false)
    private String iban;

    @NotBlank(message = "Bank name is required")
    private String bankName;

    private boolean active = true;
}