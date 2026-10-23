# Imagen de producción: compila el WAR con Maven y lo despliega en Tomcat 11.
FROM maven:3.9-eclipse-temurin-26 AS compilacion
WORKDIR /src
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

FROM tomcat:11.0-jdk21-temurin
# Sin las aplicaciones de ejemplo de Tomcat y sin ejecutar como root.
RUN rm -rf /usr/local/tomcat/webapps/* \
    && groupadd --system app && useradd --system --gid app --home /usr/local/tomcat app \
    && chown -R app:app /usr/local/tomcat
COPY --from=compilacion --chown=app:app /src/target/universidad.war /usr/local/tomcat/webapps/ROOT.war
USER app
EXPOSE 8080
# Docker (y docker compose up --wait) dan el contenedor por bueno cuando la app llega a la base de datos.
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=4 \
    CMD curl -fsS http://localhost:8080/api/salud > /dev/null || exit 1
CMD ["catalina.sh", "run"]
