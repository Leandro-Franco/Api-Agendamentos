# ---------- estagio de build ----------
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Copiado antes do codigo-fonte: o pom muda raramente, entao a camada
# com as dependencias fica cacheada entre builds.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

COPY src/ src/
# Os testes exigem Docker (Testcontainers) e rodam no pipeline, antes daqui.
RUN ./mvnw package -DskipTests -B

# ---------- estagio de execucao ----------
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

# *.jar casa apenas o artefato repackage; o .jar.original fica de fora.
COPY --from=build --chown=app:app /app/target/*.jar app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
