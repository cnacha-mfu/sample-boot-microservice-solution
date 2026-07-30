# Ultra-small Dockerfile using Alpine Linux
FROM maven:3.8.1-openjdk-11 AS build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# Use current Alpine-based OpenJDK image
FROM eclipse-temurin:11-jre-alpine

# Install minimal packages
RUN apk add --no-cache dumb-init

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8761

# Minimal JVM options for demo
ENV JAVA_OPTS="-Xmx128m -Xms64m"

# Use dumb-init for proper signal handling
ENTRYPOINT ["dumb-init", "sh", "-c", "java $JAVA_OPTS -jar app.jar"]
