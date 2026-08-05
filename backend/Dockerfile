# The JAR is built on the host with the project's Maven wrapper.
# Build a Java truststore from Alpine's current system CA bundle so outbound
# HTTPS endpoint execution works with the same roots trusted by the container.
FROM eclipse-temurin:21-jdk-alpine AS certs
RUN apk add --no-cache ca-certificates && update-ca-certificates
RUN cp "$JAVA_HOME/lib/security/cacerts" /tmp/cacerts && \
    for cert in /etc/ssl/certs/*.pem; do \
      alias_name=$(basename "$cert" .pem | tr ':' '_'); \
      keytool -importcert -noprompt -trustcacerts \
        -keystore /tmp/cacerts -storepass changeit \
        -alias "$alias_name" -file "$cert" || true; \
    done

FROM eclipse-temurin:21-jre-alpine
COPY --from=certs /tmp/cacerts /opt/java/openjdk/lib/security/cacerts
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
