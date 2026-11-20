package com.finflow.finflowbackend.modules.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferRequest {
    @NotBlank
    private String sourceAccountNumber;
    @NotBlank
    private String destinationAccountNumber;
    @NotNull @Positive
    private BigDecimal amount;
    @NotBlank
    @Size(min = 3,max=3)
    private String currency;
    @NotBlank
    private String idempotencyKey;
}
