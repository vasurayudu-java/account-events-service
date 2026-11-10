package io.github.vasurayudu.accounts.web;

import io.github.vasurayudu.accounts.domain.Account;
import io.github.vasurayudu.accounts.domain.AccountType;
import io.github.vasurayudu.accounts.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    public record OpenAccountRequest(@NotBlank String customerName, @NotNull AccountType type) {}

    public record AmountRequest(@NotNull @DecimalMin(value = "0.01") BigDecimal amount) {}

    public record AccountResponse(UUID id, String customerName, AccountType type, BigDecimal balance, Instant createdAt) {
        static AccountResponse from(Account a) {
            return new AccountResponse(a.getId(), a.getCustomerName(), a.getType(), a.getBalance(), a.getCreatedAt());
        }
    }

    @PostMapping
    public ResponseEntity<AccountResponse> open(@Valid @RequestBody OpenAccountRequest request) {
        Account account = service.open(request.customerName(), request.type());
        return ResponseEntity.created(URI.create("/api/v1/accounts/" + account.getId()))
                .body(AccountResponse.from(account));
    }

    @GetMapping("/{id}")
    public AccountResponse get(@PathVariable UUID id) {
        return AccountResponse.from(service.get(id));
    }

    @PostMapping("/{id}/deposits")
    public AccountResponse deposit(@PathVariable UUID id, @Valid @RequestBody AmountRequest request) {
        return AccountResponse.from(service.deposit(id, request.amount()));
    }

    @PostMapping("/{id}/withdrawals")
    public AccountResponse withdraw(@PathVariable UUID id, @Valid @RequestBody AmountRequest request) {
        return AccountResponse.from(service.withdraw(id, request.amount()));
    }
}
