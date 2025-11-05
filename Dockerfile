FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY target/bank_rest-0.0.1-SNAPSHOT.jar bank-rest.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "bank-rest.jar"]