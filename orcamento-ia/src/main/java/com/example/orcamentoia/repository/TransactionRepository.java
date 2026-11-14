package com.example.orcamentoia.repository;

import com.example.orcamentoia.domain.Transaction;
import com.example.orcamentoia.domain.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByDateBetweenOrderByDateDesc(LocalDate from, LocalDate to);

    @Query("select coalesce(sum(t.amount), 0) from Transaction t where t.type = :type")
    BigDecimal sumByType(@Param("type") TransactionType type);

    // NOVA CONSULTA (melhoria): total gasto em uma categoria dentro de um período
    @Query("""
           select coalesce(sum(t.amount), 0) from Transaction t
           where t.type = :type
             and lower(t.category) = lower(:category)
             and t.date between :from and :to
           """)
    BigDecimal sumByCategory(@Param("type") TransactionType type,
                             @Param("category") String category,
                             @Param("from") LocalDate from,
                             @Param("to") LocalDate to);
}
