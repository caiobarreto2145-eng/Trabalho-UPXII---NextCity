# Etapa 1: compila o projeto com Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# Etapa 2: imagem final, só com o Java e o .jar
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV TZ=America/Sao_Paulo
EXPOSE 8080
# -Xmx350m: cabe nos 512 MB do plano gratuito; timezone de Brasília para o limite diário virar à meia-noite daqui
ENTRYPOINT ["java", "-Xmx350m", "-Duser.timezone=America/Sao_Paulo", "-jar", "app.jar"]
