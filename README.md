# CARE Deep Data Security Spring Demo

CARE means Care Access and Retrieval Enforced. This is a Spring Boot and Spring AI demo that shows how a Java application can pass an authenticated end-user context to Oracle AI Database and let Oracle Deep Data Security enforce data access for relational reads, vector search, assistant tools, and denied-write paths.

The demo uses two synthetic users:

- `clara`, a care coordinator
- `drew`, a clinician

Both users call the same Spring services. Oracle AI Database returns different rows, cell values, and RAG policy documents based on the active Oracle Deep Data Security end-user context.

## What This Demo Covers

- Spring Security resource-server validation with locally signed test JWTs.
- Oracle JDBC end-user security context APIs.
- Oracle Deep Data Security data roles, end users, data grants, and mandatory data-grant enforcement.
- Spring Data JPA reads over secured relational data.
- Oracle AI Vector Search over policy documents stored in Oracle AI Database.
- Spring AI `@Tool` callbacks that use the same secured service layer.
- A deterministic assistant endpoint that summarizes only database-returned CARE context.
- An opt-in live OpenAI smoke test for the Spring AI `ChatClient` path.

## Prerequisites

- Java 21 or newer.
- Maven 3.9 or newer.
- Docker or a Docker-compatible runtime available to Testcontainers.
- Network access to pull the configured Oracle Database container image.
- An OpenAI API key only when running the opt-in live model test or live assistant path.

The default test image is:

```text
gvenzl/oracle-free:23.26.2-slim-faststart
```

You can override it when needed:

```bash
CARE_ORACLE_IMAGE=container-registry.oracle.com/database/free:latest mvn clean test
```

## Run The Tests

```bash
mvn clean test
```

The default suite starts Oracle Database with Testcontainers, installs the CARE schema and Oracle Deep Data Security policy setup, and runs the Spring integration tests.

The suite fails early if the selected database image does not expose the Oracle Deep Data Security objects, Oracle AI Vector Search support, or Oracle JDBC APIs required by the demo.

## Run The Opt-In Live OpenAI Test

The default suite does not call an external model. To validate the live Spring AI `ChatClient` path, enable the live test explicitly:

```bash
CARE_LIVE_OPENAI_TEST=true \
OPENAI_API_KEY="..." \
CARE_OPENAI_MODEL=gpt-4.1-mini \
mvn -Dtest=LiveOpenAiAssistantIntegrationTest test
```

That test checks that the assistant receives only the database-authorized context returned through CARE tools.

When enabled, the test logs the live smoke-test path so we can see what happened without attaching a debugger. It runs the same prompt as `clara` and as `drew`, then prints blocks such as `Running "..." as clara, output is:` and `Running "..." as drew, output is:`. Each block includes the assistant summary, database-authorized visible cases, and database-authorized policy matches. It does not log the OpenAI API key.

## Application Shape

```text
src/main/java/com/oracle/demo/care/
├── CareController.java
├── CareService.java
├── CareAssistantService.java
├── CareAssistantTools.java
├── deepsec/
├── patient/
├── policy/
└── security/
```

The SQL setup lives in:

```text
src/main/resources/db/deepsec.sql
```

The integration tests execute the same SQL resource that the demo documents, so schema setup and security behavior stay together.

## Optional Observability

The `observability/` directory contains a local SigNoz setup and OpenTelemetry Java agent instructions. It is useful for inspecting the Spring Boot request path, service methods, repository calls, SQL spans, and Spring AI tool boundaries.

Start SigNoz:

```bash
docker compose -f observability/docker-compose.signoz.yaml up -d
```

Then follow [observability/README.md](observability/README.md) for the OpenTelemetry Java agent command.

## Notes

- The local tests use synthetic care-coordination data and locally signed JWTs.
- Production deployments normally use an identity provider, production issuer/JWKS configuration, and database connectivity configured for the target environment.
- Token-based Oracle JDBC end-user context authentication requires TLS/TCPS database connectivity.
- The runnable sample focuses on relational reads, vector retrieval, assistant context construction, and fail-closed denied writes.
