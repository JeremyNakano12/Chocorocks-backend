# Etapa de construcción con Gradle y JDK 21
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

# Copiamos el código fuente
COPY . .

# Compilamos el proyecto y generamos el JAR
RUN gradle bootJar

# Etapa de ejecución con JRE 21 (más liviana)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamos el JAR generado desde la etapa anterior
COPY --from=builder /app/build/libs/*.jar app.jar

# Expón el puerto de la aplicación (ajusta si usas otro)
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
