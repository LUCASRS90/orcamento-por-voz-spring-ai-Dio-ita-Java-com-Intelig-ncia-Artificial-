#  Orçamento IA — API inteligente com Spring Boot + Spring AI

Projeto do **Desafio de Projeto Spring AI (DIO)**. Uma API de orçamento pessoal que entende comandos
de voz como *"gastei 35 reais no almoço"* ou *"quanto gastei com transporte este mês?"*.

## O que o projeto faz

```
 áudio (.mp3/.wav)
      │  POST /api/voice
      ▼
 TranscriptionService ── Whisper ──► texto
      ▼
 AssistantService (ChatClient + system prompt)
      ▼  Tool Calling
 FinanceTools ──► TransactionService (validações) ──► H2 (JPA)
      ▼
 resposta em texto  (ou MP3 falado via /api/voice/audio, usando SpeechService)
```

| Camada | Pasta | Responsabilidade |
|---|---|---|
| Web | `web/` | Endpoints REST e tratamento de erros |
| IA | `service/AssistantService`, `tools/` | Interpretar a intenção e escolher a função |
| Negócio | `service/TransactionService` | Regras e validações (a IA **não** fura as regras) |
| Dados | `domain/`, `repository/` | Entidade e consultas JPA |

## Tecnologias

Java 21 · Spring Boot 3.4 · Spring AI 1.0 (OpenAI: chat, Whisper, TTS) · Spring Data JPA · H2 · Maven · JUnit 5

## Como executar (VS Code)

1. Instale **JDK 21** e o **Maven** (ou use o wrapper, se adicionar um).
2. Abra a pasta no VS Code e aceite instalar as extensões recomendadas (*Extension Pack for Java*, *Spring Boot Extension Pack*, *REST Client*).
3. Defina a chave da OpenAI **antes** de abrir o VS Code:
   - Linux/macOS: `export OPENAI_API_KEY="sk-..."`
   - Windows (PowerShell): `setx OPENAI_API_KEY "sk-..."` e reabra o VS Code
4. Rode pela aba **Run and Debug → "Orcamento IA"** (F5), ou no terminal: `mvn spring-boot:run`
5. A API sobe em `http://localhost:8080` (console do banco: `/h2-console`).

>  Nunca coloque a chave no código nem faça commit dela.

## Como testar o fluxo principal

Abra `requests/api.http` (extensão REST Client) e clique em **Send Request**, ou use o cURL:

```bash
# texto (simula o áudio já transcrito)
curl -X POST localhost:8080/api/chat -H "Content-Type: application/json" \
     -d '{"message":"Gastei 35 reais no almoço hoje"}'

# áudio de verdade
curl -X POST localhost:8080/api/voice -F "audio=@comando.mp3"

# resposta falada
curl -X POST localhost:8080/api/voice/audio -F "audio=@comando.mp3" -o resposta.mp3
```

Testes automatizados (não precisam de chave): `mvn test`

##  Melhoria implementada

1. **Nova ferramenta de Tool Calling** — `getSpendingByCategory`: responde *"quanto gastei com alimentação este mês?"*
   (nova query `sumByCategory` no repositório).
2. **Validações antes de salvar** — valor > 0 e ≤ R$ 1.000.000, descrição obrigatória, tipo válido,
   sem data futura, categoria padrão `outros`. Os erros voltam para a IA, que os explica ao usuário.
3. **Testes** do serviço (`TransactionServiceTest`).
4. **Endpoints REST** de apoio: `GET/POST /api/transactions` e `GET /api/transactions/balance`.

