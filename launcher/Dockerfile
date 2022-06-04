# -buster is required to have apt available
FROM openjdk:17-slim-buster

# Optional JVM arguments, such as memory settings
ARG JVM_ARGS=""

# Install curl, then delete apt indexes to save image space
RUN apt update \
    && apt install -y curl procps

WORKDIR /app
COPY ./build/libs/app.jar /app

EXPOSE 8181
EXPOSE 9191
EXPOSE 8282

# health status is determined by the availability of the /health endpoint

ENV WEB_HTTP_PORT="8181"
ENV WEB_HTTP_PATH="/api"
ENV WEB_HTTP_DATA_PORT="9191"
ENV WEB_HTTP_DATA_PATH="/api/v1/data"
ENV WEB_HTTP_IDS_PORT="8282"
ENV WEB_HTTP_IDS_PATH="/api/v1/ids"

# Use "exec" for graceful termination (SIGINT) to reach JVM.
# ARG can not be used in ENTRYPOINT so storing value in an ENV variable
ENV ENV_JVM_ARGS=$JVM_ARGS
ENTRYPOINT [ "sh", "-c", \
    "echo XXXX1; echo java ${ENV_JVM_ARGS} ${JVM_RUNTIME_ARGS} -jar app.jar; echo XXXXX2; sleep 10000 "]
