# Build somente da aplicação mobile; o módulo admin não é empacotado.
FROM gradle:8.14.4-jdk21 AS build
WORKDIR /workspace
COPY . .
RUN gradle --no-daemon :mobile-api:bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/mobile-api/build/libs/mobile-api-0.0.1-SNAPSHOT.jar /app/mobile-api.jar
EXPOSE 8081
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/mobile-api.jar"]
