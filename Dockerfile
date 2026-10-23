# Imagen de producción: compila el WAR con Maven y lo despliega en Tomcat 11.
FROM maven:3.9-eclipse-temurin-21 AS compilacion
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
CMD ["catalina.sh", "run"]
