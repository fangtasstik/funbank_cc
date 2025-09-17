FROM openjdk:17-jdk-slim as build

WORKDIR /workspace
COPY pom.xml .
COPY src src

RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

FROM openjdk:17-jre-slim

WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]