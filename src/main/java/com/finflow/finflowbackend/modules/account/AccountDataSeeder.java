package com.finflow.finflowbackend.modules.account;

import com.finflow.finflowbackend.modules.account.domain.Account;
import com.finflow.finflowbackend.modules.account.domain.AccountStatus;
import com.finflow.finflowbackend.modules.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountDataSeeder implements CommandLineRunner {

    private final AccountRepository accountRepository;

    @Override
    public void run(String... args) throws Exception {

        if(accountRepository.count() == 0){
            Account account1 = Account.builder()
                    .id(UUID.randomUUID().toString())
                    .accountNumber("ACC-1001")
                    .holderName("Siva Prasanna")
                    .currency("INR")
                    .balance(new BigDecimal("5000.0000"))
                    .status(AccountStatus.ACTIVE)
                    .build();

            Account account2 = Account.builder()
                    .id(UUID.randomUUID().toString())
                    .accountNumber("ACC-1002")
                    .holderName("European Retail Client")
                    .currency("INR")
                    .balance(new BigDecimal("10000.0000"))
                    .status(AccountStatus.ACTIVE)
                    .build();

            accountRepository.saveAll(List.of(account1,account2));

            System.out.println("=================================================");
            System.out.println(">>> Sample test accounts seeded into MySQL!");
            System.out.println(">>> ACC-1001 Balance: ₹50,000.0000");
            System.out.println(">>> ACC-1002 Balance: ₹10,000.0000");
            System.out.println("=================================================");
        }

    }
}
