package com.example.orcamentoia;

import static org.junit.jupiter.api.Assertions.*;

import com.example.orcamentoia.domain.TransactionType;
import com.example.orcamentoia.service.TransactionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TransactionService.class)
class TransactionServiceTest {

    @Autowired
    TransactionService service;

    @Test
    void rejeitaValorNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                service.create("almoço", new BigDecimal("-10"), TransactionType.EXPENSE, "alimentação", null));
    }

    @Test
    void rejeitaDataFutura() {
        assertThrows(IllegalArgumentException.class, () ->
                service.create("curso", BigDecimal.TEN, TransactionType.EXPENSE, "estudo", LocalDate.now().plusDays(1)));
    }

    @Test
    void calculaSaldo() {
        service.create("salário", new BigDecimal("1000"), TransactionType.INCOME, "salário", null);
        service.create("mercado", new BigDecimal("250.50"), TransactionType.EXPENSE, "alimentação", null);
        assertEquals(0, new BigDecimal("749.50").compareTo(service.balance()));
    }

    @Test
    void somaGastosPorCategoria() {
        LocalDate hoje = LocalDate.now();
        service.create("pizza", new BigDecimal("60"), TransactionType.EXPENSE, "Alimentação", hoje);
        service.create("uber", new BigDecimal("20"), TransactionType.EXPENSE, "transporte", hoje);
        BigDecimal total = service.totalSpentByCategory("alimentação", hoje.minusDays(1), hoje);
        assertEquals(0, new BigDecimal("60").compareTo(total));
    }
}
