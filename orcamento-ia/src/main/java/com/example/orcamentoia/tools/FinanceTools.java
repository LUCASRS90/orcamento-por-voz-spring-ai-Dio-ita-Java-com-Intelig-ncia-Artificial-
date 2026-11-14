package com.example.orcamentoia.tools;

import com.example.orcamentoia.domain.Transaction;
import com.example.orcamentoia.domain.TransactionType;
import com.example.orcamentoia.service.TransactionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Funções reais da aplicação expostas para a IA.
 * A IA decide QUAL chamar; a regra de negócio continua no TransactionService.
 */
@Component
public class FinanceTools {

    private final TransactionService service;

    public FinanceTools(TransactionService service) {
        this.service = service;
    }

    @Tool(description = "Cria uma transação financeira (receita ou despesa).")
    public String createTransaction(
            @ToolParam(description = "Descrição curta, ex.: 'almoço'") String description,
            @ToolParam(description = "Valor positivo em reais, ex.: 35.90") BigDecimal amount,
            @ToolParam(description = "INCOME para receita ou EXPENSE para despesa") String type,
            @ToolParam(description = "Categoria, ex.: alimentação, transporte, salário", required = false) String category,
            @ToolParam(description = "Data no formato AAAA-MM-DD. Se omitida, usa hoje", required = false) String date) {
        try {
            Transaction t = service.create(description, amount,
                    TransactionType.valueOf(type.trim().toUpperCase()),
                    category, date == null || date.isBlank() ? null : LocalDate.parse(date));
            return "Transação criada: id=%d, %s, R$ %s, %s, categoria %s, data %s"
                    .formatted(t.getId(), t.getDescription(), t.getAmount(), t.getType(), t.getCategory(), t.getDate());
        } catch (Exception e) {
            return "ERRO ao criar transação: " + e.getMessage();
        }
    }

    @Tool(description = "Retorna o saldo atual (receitas menos despesas).")
    public String getBalance() {
        return "Saldo atual: R$ " + service.balance();
    }

    @Tool(description = "Lista as transações de um período.")
    public String listTransactions(
            @ToolParam(description = "Data inicial AAAA-MM-DD") String from,
            @ToolParam(description = "Data final AAAA-MM-DD") String to) {
        try {
            List<Transaction> list = service.list(LocalDate.parse(from), LocalDate.parse(to));
            if (list.isEmpty()) return "Nenhuma transação no período.";
            return list.stream()
                    .map(t -> "%s | %s | R$ %s | %s | %s".formatted(
                            t.getDate(), t.getDescription(), t.getAmount(), t.getType(), t.getCategory()))
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "ERRO ao listar: " + e.getMessage();
        }
    }

    // NOVA FERRAMENTA (melhoria): consulta por categoria
    @Tool(description = "Retorna o total gasto (despesas) em uma categoria dentro de um período.")
    public String getSpendingByCategory(
            @ToolParam(description = "Categoria, ex.: alimentação") String category,
            @ToolParam(description = "Data inicial AAAA-MM-DD") String from,
            @ToolParam(description = "Data final AAAA-MM-DD") String to) {
        try {
            BigDecimal total = service.totalSpentByCategory(category, LocalDate.parse(from), LocalDate.parse(to));
            return "Total gasto em '%s' entre %s e %s: R$ %s".formatted(category, from, to, total);
        } catch (Exception e) {
            return "ERRO na consulta: " + e.getMessage();
        }
    }
}
