package com.finflow.finflowbackend.common.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private Instant timestamp;
}
