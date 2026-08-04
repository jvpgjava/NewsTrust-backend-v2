# NewsTrust Backend (Java / LangChain4j / RAG)

Reescrita do backend do NewsTrust em Java 25 + Spring Boot, seguindo Arquitetura
Hexagonal (Ports & Adapters). Substitui a versao anterior em Node.js/Express,
implementando o modelo de pontuacao e o pipeline RAG descritos no artigo
apresentado no InovaEduBr Summit 2026.

## Modelo de pontuacao

```
S = (0.35 x R) + (0.25 x T) + (0.30 x V) + (0.10 x D)
```

- **R** - reputacao da fonte (hoje via ScamAdviser)
- **T** - consistencia textual (indicadores linguisticos deterministicos)
- **V** - verificacao cruzada (RAG: similaridade de cosseno com noticias ja processadas via pgvector/HNSW)
- **D** - padrao de disseminacao (assume baseline neutro quando nao ha dado disponivel)

Os pesos sao configuraveis por perfil em `application.properties`
(`newstrust.scoring.*`), sem necessidade de recompilar - ha perfis de exemplo
para contexto padrao, eleitoral (eleva V) e saude publica (eleva R).

A IA (Gemini, orquestrado via LangChain4j) enriquece o contexto de verificacao
cruzada e gera explicacoes textuais adicionais, mas nunca decide o score: o
resultado final e sempre a soma ponderada deterministica calculada por
`CredibilityScoreCalculator`, uma classe Java pura sem nenhuma dependencia de
framework.

Gemini e o unico provedor de LLM, tanto em dev quanto em producao - o que muda
entre os dois ambientes e so a configuracao ao redor dele (chave de API,
modelo e instancia de banco), nunca o provedor. Toda a integracao com
LangChain4j fica isolada em `infrastructure.adapter.out.ai`, atras dos ports
`EmbeddingGeneratorPort`/`LlmExplanationPort` - adicionar um provedor de
fallback no futuro e uma questao de estender `GeminiLlmProvider` (ou compor um
novo provider), sem tocar no dominio.

## Arquitetura

```
com.newstrust
├── domain            # Java puro: modelo (records), regras de negocio, ports (interfaces)
├── application       # Orquestracao fina dos casos de uso, depende so dos ports
└── infrastructure
    ├── adapter.in.web         # Controllers REST, DTOs, ProblemDetail
    │   └── openapi            # Interfaces com @Operation/@ApiResponse - controllers ficam enxutos
    └── adapter.out
        ├── persistence        # JPA + pgvector, Flyway
        ├── ai                 # LangChain4j / Gemini (embeddings + explicacoes)
        │   └── provider       # GeminiLlmProvider - unico ponto de config do provedor de IA
        ├── reputation         # ScamAdviser
        ├── text               # Analise textual deterministica (fator T)
        └── dissemination      # Fator D (ainda sem fonte de dado real)
```

O pacote `domain` nao importa nada de Spring, JPA ou LangChain4j - e testavel
isoladamente com JUnit puro (`./mvnw test -pl :newstrust-backend -Dtest=com.newstrust.domain.**`).

## Pre-requisitos

- Java 25 (JDK)
- PostgreSQL 16+ com a extensao [pgvector](https://github.com/pgvector/pgvector) instalada no servidor
- Nao precisa de Maven instalado globalmente - use o wrapper (`./mvnw` / `mvnw.cmd`)

## Setup do banco de dados

```sql
CREATE DATABASE newstrust;
CREATE USER newstrust WITH PASSWORD 'sua-senha';
GRANT ALL PRIVILEGES ON DATABASE newstrust TO newstrust;
```

A extensao `vector` e criada automaticamente pela migration Flyway
(`V1__init_schema.sql`), desde que o usuario tenha privilegio para `CREATE EXTENSION`
(em geral requer superuser na primeira vez - rode `CREATE EXTENSION vector;`
manualmente como superuser se a migration falhar por permissao).

## Variaveis de ambiente

Veja [application-example.properties](application-example.properties) para a
lista completa (banco de dados, chaves da API do Gemini, chave do ScamAdviser).
Nunca commite segredos reais - os `application*.properties` versionados so
referenciam `${VAR_NAME}`.

**Dev e producao usam chaves de API do Gemini separadas**
(`GEMINI_API_KEY_DEV` vs `GEMINI_API_KEY_PROD`), configuradas respectivamente
em `application-dev.properties` e `application-prod.properties` - de proposito,
para nao misturar quota, billing e logs dos dois ambientes na mesma chave.
Por padrao, dev usa um modelo mais leve/barato (`gemini-2.5-flash-lite`) e
producao usa `gemini-2.5-flash`; troque via `GEMINI_CHAT_MODEL_DEV`/`_PROD` se
quiser outra variante - nada disso e hardcoded no adapter, e so configuracao.

## Rodando localmente

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/newstrust"
$env:DB_USERNAME = "newstrust"
$env:DB_PASSWORD = "sua-senha"
$env:GEMINI_API_KEY_DEV = "sua-chave-de-dev-do-gemini"

./mvnw.cmd spring-boot:run
```

A aplicacao sobe em `http://localhost:8080` com o profile `dev` ativo por
padrao, apontando para o Postgres/pgvector local da sua maquina
(`application-dev.properties` ja assume `localhost:5432` se `DB_URL` nao for
definida). Em producao (profile `prod`), `DB_URL`/`DB_USERNAME`/`DB_PASSWORD` e
`GEMINI_API_KEY_PROD` sao obrigatorias - a aplicacao falha na subida se
faltarem, em vez de silenciosamente cair para valores de desenvolvimento.

## Documentacao da API (Swagger)

Com a aplicacao rodando:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

A documentacao de cada endpoint (`@Operation`, `@ApiResponse`, exemplos) vive
em interfaces dedicadas em `infrastructure.adapter.in.web.openapi`
(`AnalysisApi`, `TrustGraphApi`), nao nos controllers - o controller so
implementa a interface e fica com o corpo dos metodos, sem nenhuma anotacao de
mapeamento ou Swagger. Isso funciona porque o Spring MVC herda anotacoes de
mapeamento (`@GetMapping`/`@PostMapping`) declaradas em interfaces (suportado
desde o Spring 4.3) - ha um teste de regressao para isso em
`TrustGraphControllerWebMvcTest`.

## Testes

```powershell
./mvnw.cmd test
```

O nucleo do dominio (`CredibilityScoreCalculator`, `CrossVerificationScorer` e
os value objects) e testado com JUnit 5 puro, sem contexto Spring. A camada de
aplicacao (`AnalyzeNewsContentService`, `AnalyzeSourceService`,
`GetTrustGraphService`) e testada com JUnit 5 + Mockito, mockando os ports de saida.

## Endpoints

| Metodo | Caminho                  | Descricao                                                |
|--------|---------------------------|-----------------------------------------------------------|
| POST   | `/api/analysis/content`   | Analisa titulo + conteudo de uma noticia                   |
| POST   | `/api/analysis/source`    | Analisa a credibilidade de uma fonte (URL)                 |
| GET    | `/api/graph/sources`      | Rede de fontes (nos = dominios)                            |
| GET    | `/api/graph/news`         | Rede de noticias (nos = noticias, cor por faixa de risco)  |
| GET    | `/api/graph/news/{id}`    | Detalhe de um no especifico da rede de noticias            |
| GET    | `/api/events/analyses`    | Stream SSE - evento `analysis-created` a cada nova analise |

As respostas de grafo ja vem no formato `{ nodes: [...], edges: [...] }` com
`source`/`target`/`weight` nas arestas, pronto para consumo por D3.js
(d3-force) no frontend Angular.

Erros seguem o formato padrao do Spring (`ProblemDetail`, RFC 7807) - nunca
stack trace bruta.

## Deploy (VPS Arch Linux, Jenkins, sem Docker)

O build gera um JAR executavel (`./mvnw clean package`), rodado via `systemd`
na VPS, em dois ambientes (producao e dev/homolog) na mesma maquina.

Guia completo passo a passo (DNS, PostgreSQL/pgvector, systemd, Nginx incluindo
o tratamento especial do endpoint SSE, HTTPS, e os 4 jobs Jenkins):
**[docs/DEPLOY.md](docs/DEPLOY.md)**.
