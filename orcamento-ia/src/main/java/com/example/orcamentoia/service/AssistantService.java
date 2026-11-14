package com.example.orcamentoia.service;

import com.example.orcamentoia.tools.FinanceTools;
import java.time.LocalDate;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/** Entende a intenção do usuário e deixa o modelo chamar as ferramentas (Tool Calling). */
@Service
public class AssistantService {

    private static final String SYSTEM_PROMPT = """
            Você é um assistente de orçamento pessoal. Responda sempre em português do Brasil,
            de forma curta e simpática. A data de hoje é %s.
            Use SEMPRE as ferramentas disponíveis para criar ou consultar transações;
            nunca invente valores. Se uma ferramenta retornar erro, explique o motivo ao usuário.
            Se faltar o valor de uma transação, peça-o ao usuário em vez de supor.
            """;

    private final ChatClient chatClient;
    private final FinanceTools financeTools;

    public AssistantService(ChatClient.Builder builder, FinanceTools financeTools) {
        this.chatClient = builder.build();
        this.financeTools = financeTools;
    }

    public String ask(String userText) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT.formatted(LocalDate.now()))
                .user(userText)
                .tools(financeTools)
                .call()
                .content();
    }
}
