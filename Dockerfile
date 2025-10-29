# Etapa 1: Construcción del proyecto usando Maven con JDK 21
FROM maven:3.9.9-eclipse-temurin-23 AS build

WORKDIR /app

# Copiamos el pom y luego el código fuente
COPY GymAssist/pom.xml .
COPY GymAssist/src ./src

# Copiamos el pom y luego el src para aprovechar cache de Docker
RUN mvn clean package -DskipTests

FROM eclipse-temurin:23-jre

WORKDIR /app

# Copiamos el .jar generado en la etapa build
COPY --from=build /app/target/*.jar app.jar

# Puerto expuesto (ajustar si usas otro)
EXPOSE 8080

# Comando para correr la app
ENTRYPOINT ["java", "-jar", "app.jar"]