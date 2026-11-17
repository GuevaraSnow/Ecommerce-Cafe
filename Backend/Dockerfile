# Empaqueta el jar que genera ./gradlew bootJar (hay que correrlo antes: docker compose up --build)
FROM eclipse-temurin:21-jre
# Nombre explicito: en build/libs tambien queda el "-plain.jar" y, si el proyecto se sincroniza con OneDrive,
# copias del jar con el nombre del equipo; un comodin podria copiar varios archivos y fallar.
COPY build/libs/Backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
