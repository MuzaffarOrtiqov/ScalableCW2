# Replace the old RUNTIME STAGE with this:
FROM eclipse-temurin:17-jre
WORKDIR /app

# The rest remains the same
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]