package com.finflow.finflowbackend.modules.payment.service;

import com.finflow.finflowbackend.modules.payment.dto.TransferRequest;
import com.finflow.finflowbackend.modules.payment.dto.TransferResponse;

public interface TransferService {
    TransferResponse transferFunds(TransferRequest request);
}
