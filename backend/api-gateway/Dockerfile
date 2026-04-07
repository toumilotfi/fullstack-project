FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml ./
COPY shared-lib/pom.xml shared-lib/pom.xml
COPY service-registry/pom.xml service-registry/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY task-service/pom.xml task-service/pom.xml
COPY messaging-service/pom.xml messaging-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml

RUN mvn -pl api-gateway -am dependency:go-offline

COPY shared-lib/src shared-lib/src
COPY api-gateway/src api-gateway/src

RUN mvn -pl api-gateway -am -DskipTests package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /workspace/api-gateway/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
