# ===============================
# STAGE 1: BUILD (Compiling the code)
# ===============================
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the pom and source to build the jar
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# ===============================
# STAGE 2: RUNTIME (Running the app)
# ===============================
# We changed from 'alpine' to the standard 'jre' to fix the library errors
FROM eclipse-temurin:17-jre
WORKDIR /app

# This 'from=build' now correctly points to STAGE 1 above
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]