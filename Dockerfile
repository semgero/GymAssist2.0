# Etapa de construcción
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

# Copiar TODO el proyecto
COPY . .

# Construir el proyecto - apunta a la subcarpeta GymAssist
RUN cd GymAssist && mvn clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiar el JAR desde la subcarpeta
COPY --from=builder /workspace/GymAssist/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]