FROM maven:3.9.9-eclipse-temurin-8 AS builder

WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:8-jdk

WORKDIR /app
COPY --from=builder /build/target/yuoj-code-sandbox-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
