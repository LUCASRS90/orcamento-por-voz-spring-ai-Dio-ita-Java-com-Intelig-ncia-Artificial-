package com.example.orcamentoia.service;

import com.example.orcamentoia.domain.Transaction;
import com.example.orcamentoia.domain.TransactionType;
import com.example.orcamentoia.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000");

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    /** MELHORIA: validações antes de salvar. */
    @Transactional
    public Transaction create(String description, BigDecimal amount, TransactionType type,
                              String category, LocalDate date) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("A descrição não pode ser vazia.");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero.");
        }
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new IllegalArgumentException("O valor excede o limite de R$ 1.000.000,00.");
        }
        if (type == null) {
            throw new IllegalArgumentException("O tipo deve ser INCOME ou EXPENSE.");
        }
        LocalDate effectiveDate = (date != null) ? date : LocalDate.now();
        if (effectiveDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Não é permitido lançar transações com data futura.");
        }
        String effectiveCategory = (category == null || category.isBlank()) ? "outros" : category.trim();

        return repository.save(new Transaction(description.trim(), amount, type, effectiveCategory, effectiveDate));
    }

    @Transactional(readOnly = true)
    public List<Transaction> list(LocalDate from, LocalDate to) {
        return repository.findByDateBetweenOrderByDateDesc(from, to);
    }

    @Transactional(readOnly = true)
    public BigDecimal balance() {
        return repository.sumByType(TransactionType.INCOME)
                .subtract(repository.sumByType(TransactionType.EXPENSE));
    }

    @Transactional(readOnly = true)
    public BigDecimal totalSpentByCategory(String category, LocalDate from, LocalDate to) {
        return repository.sumByCategory(TransactionType.EXPENSE, category, from, to);
    }
}
