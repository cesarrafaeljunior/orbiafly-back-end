FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
RUN mvn -B dependency:resolve -DincludeScope=runtime

COPY src ./src
RUN mvn -B package -Dmaven.test.skip=true

FROM eclipse-temurin:21.0.8_9-jre

WORKDIR /app
RUN groupadd --system orbiafly && useradd --system --gid orbiafly orbiafly

COPY --from=build /workspace/target/orbiafly-api-*.jar /app/orbiafly-api.jar

USER orbiafly
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/orbiafly-api.jar"]
