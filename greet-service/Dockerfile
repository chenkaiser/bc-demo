FROM docker.io/library/maven:3.9-eclipse-temurin-21 AS build
ARG APP_VERSION=local
WORKDIR /app
COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/
COPY api-gateway/pom.xml api-gateway/
COPY greet-service/pom.xml greet-service/
COPY hello-service/pom.xml hello-service/
RUN mvn dependency:go-offline -pl greet-service -am -Drevision=${APP_VERSION} -q
COPY greet-service/src greet-service/src
RUN mvn test -pl greet-service -Drevision=${APP_VERSION} -q
RUN mvn package -pl greet-service -DskipTests -Drevision=${APP_VERSION} -q

FROM docker.io/library/eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/greet-service/target/greet-service.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
