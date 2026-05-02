FROM docker.io/library/maven:3.9-eclipse-temurin-21 AS build
ARG APP_VERSION=local
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -Drevision=${APP_VERSION} -q
COPY src src
RUN mvn test -Drevision=${APP_VERSION} -q
RUN mvn package -DskipTests -Drevision=${APP_VERSION} -q

FROM docker.io/library/eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/target/api-gateway.jar app.jar
EXPOSE 8443
ENTRYPOINT ["java", "-jar", "app.jar"]
