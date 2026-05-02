FROM docker.io/library/maven:3.9-eclipse-temurin-21 AS build
ARG APP_VERSION=local
WORKDIR /app

# Install the parent POM into the local cache — non-recursive, no sibling modules needed.
# Adding a new service to the repo never requires touching this file.
COPY pom.xml .
RUN mvn install -N -Drevision=${APP_VERSION} -q

COPY hello-service/pom.xml hello-service/pom.xml
RUN mvn dependency:go-offline -f hello-service/pom.xml -Drevision=${APP_VERSION} -q

COPY hello-service/src hello-service/src
RUN mvn test -f hello-service/pom.xml -Drevision=${APP_VERSION} -q
RUN mvn package -f hello-service/pom.xml -DskipTests -Drevision=${APP_VERSION} -q

FROM docker.io/library/eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/hello-service/target/hello-service.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
