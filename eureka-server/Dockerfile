FROM docker.io/library/maven:3.9-eclipse-temurin-21 AS build
ARG APP_VERSION=local
WORKDIR /app
COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/
COPY api-gateway/pom.xml api-gateway/
COPY greet-service/pom.xml greet-service/
COPY hello-service/pom.xml hello-service/
RUN mvn dependency:go-offline -pl eureka-server -am -Drevision=${APP_VERSION} -q
COPY eureka-server/src eureka-server/src
RUN mvn test -pl eureka-server -Drevision=${APP_VERSION} -q
RUN mvn package -pl eureka-server -DskipTests -Drevision=${APP_VERSION} -q

FROM docker.io/library/eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
# JAR name is now stable: <artifactId>.jar — no version suffix, no Dockerfile edit on release
COPY --from=build /app/eureka-server/target/eureka-server.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
