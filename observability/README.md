# Optional Observability with SigNoz

This optional path runs SigNoz in one local container and uses the OpenTelemetry Java agent to instrument the Spring Boot demo without changing application code.

It is intentionally outside the default `mvn clean test` proof. The security tests should remain deterministic and should not require an observability backend.

## Start SigNoz

```bash
docker compose -f observability/docker-compose.signoz.yaml up -d
```

SigNoz UI:

```text
http://localhost:8080
```

The compose file publishes OTLP on:

```text
4317  OTLP gRPC
4318  OTLP HTTP
```

## Download the OpenTelemetry Java Agent

```bash
curl -L -o observability/opentelemetry-javaagent.jar \
  https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar
```

## Run the Demo with Auto-Instrumentation

```bash
JAVA_TOOL_OPTIONS="-javaagent:$(pwd)/observability/opentelemetry-javaagent.jar" \
OTEL_SERVICE_NAME=care-deepsec-spring \
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317 \
OTEL_EXPORTER_OTLP_PROTOCOL=grpc \
OTEL_RESOURCE_ATTRIBUTES=service.namespace=care,deployment.environment=local-demo \
mvn spring-boot:run
```

To opt in to the live Spring AI OpenAI path while tracing, add:

```bash
CARE_ASSISTANT_SPRING_AI_ENABLED=true \
OPENAI_API_KEY="..." \
JAVA_TOOL_OPTIONS="-javaagent:$(pwd)/observability/opentelemetry-javaagent.jar" \
OTEL_SERVICE_NAME=care-deepsec-spring \
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317 \
OTEL_EXPORTER_OTLP_PROTOCOL=grpc \
OTEL_RESOURCE_ATTRIBUTES=service.namespace=care,deployment.environment=local-demo \
mvn spring-boot:run
```

## Generate a Trace

Call an endpoint with a signed test JWT, or run the Spring application test client from your IDE while the app is running. In SigNoz, look for service name:

```text
care-deepsec-spring
```

## Stop SigNoz

```bash
docker compose -f observability/docker-compose.signoz.yaml down
```

Use `-v` only when you intentionally want to delete the local SigNoz data volume.

## References

- SigNoz Docker install: https://signoz.io/docs/install/docker/
- SigNoz self-hosted ingestion overview: https://signoz.io/docs/ingestion/self-hosted/overview/
- OpenTelemetry Java instrumentation: https://github.com/open-telemetry/opentelemetry-java-instrumentation
