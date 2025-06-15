# Etapa de construcción
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app
COPY . .

# 👉 Usa el perfil production para compilar y evitar errores de dev
RUN mvn clean install -Pproduction -DskipTests

# Etapa final (solo JAR)
FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
