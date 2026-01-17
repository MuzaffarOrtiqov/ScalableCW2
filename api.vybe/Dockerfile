# ===============================
# STAGE 1: BUILD (Compiling)
# ===============================
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the project files
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src

# Create the JAR file
RUN mvn clean package -DskipTests

# ===============================
# STAGE 2: RUNTIME (Running)
# ===============================
# Using the full JRE (not alpine) to ensure native Azure libraries load
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the jar from the 'build' stage above
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]