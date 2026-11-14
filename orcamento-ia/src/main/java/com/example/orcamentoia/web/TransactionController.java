package com.example.orcamentoia.web;

import com.example.orcamentoia.domain.Transaction;
import com.example.orcamentoia.domain.TransactionType;
import com.example.orcamentoia.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    public record TransactionRequest(
            @NotBlank String description,
            @NotNull @Positive BigDecimal amount,
            @NotNull TransactionType type,
            String category,
            LocalDate date) {}

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @GetMapping
    public List<Transaction> list(@RequestParam(required = false) LocalDate from,
                                  @RequestParam(required = false) LocalDate to) {
        LocalDate end = to != null ? to : LocalDate.now();
        LocalDate start = from != null ? from : end.withDayOfMonth(1);
        return service.list(start, end);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction create(@Valid @RequestBody TransactionRequest r) {
        return service.create(r.description(), r.amount(), r.type(), r.category(), r.date());
    }

    @GetMapping("/balance")
    public Map<String, BigDecimal> balance() {
        return Map.of("balance", service.balance());
    }
}
