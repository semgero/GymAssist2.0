# Etapa de construcción
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

# Copiar solo el backend (la API de Python se despliega aparte)
COPY GymAssist ./GymAssist

# Construir el proyecto
RUN cd GymAssist && mvn -q clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /workspace/GymAssist/target/*.jar app.jar

EXPOSE 8080

# Memoria ajustada para el plan de Railway (512 MB). El puerto lo toma de la variable PORT
# (server.port=${PORT:8080} en application.properties).
# Hora de Colombia (sin esto el servidor usa UTC y después de las 7 p. m. las fechas salen con un día de más)
ENV TZ=America/Bogota
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k -XX:MaxMetaspaceSize=160m -Duser.timezone=America/Bogota"

ENTRYPOINT ["java", "-jar", "app.jar"]
