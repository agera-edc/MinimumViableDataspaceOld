FROM alpine as dependencies

RUN mkdir /deps
RUN wget https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.2.11/applicationinsights-agent-3.2.11.jar -O /deps/applicationinsights-agent.jar

# -buster is required to have apt available
FROM openjdk:17-slim-buster

# by default curl is not available, so install it
RUN apt update && apt install curl -y

WORKDIR /app
COPY ./build/libs/dataspaceconnector-basic.jar /app

COPY --from=dependencies /deps/applicationinsights-agent.jar /app

EXPOSE 8181
EXPOSE 9191

# health status is determined by the availability of the /health endpoint
HEALTHCHECK --interval=5s --timeout=5s --retries=10 CMD curl --fail -X GET http://localhost:8181/api/check/health || exit 1

ENTRYPOINT java \
    -javaagent:applicationinsights-agent.jar \
    -Djava.security.edg=file:/dev/.urandom \
    -Dedc.ids.id="urn:connector:connector" \
    -Dedc.ids.title="Eclipse Dataspace Connector" \
    -Dedc.ids.description="Eclipse Dataspace Connector with IDS extensions" \
    -Dedc.ids.maintainer="https://example.maintainer.com" \
    -Dedc.ids.curator="https://example.maintainer.com" \
    -Dweb.http.port="8181" \
    -Dweb.http.path="/api" \
    -Dweb.http.data.port="9191" \
    -Dweb.http.data.path="/api/v1/data" \
    -Dweb.http.ids.port="8282" \
    -Dweb.http.ids.path="/api/v1/ids" \
    -jar dataspaceconnector-basic.jar
