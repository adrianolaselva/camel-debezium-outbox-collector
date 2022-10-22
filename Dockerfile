FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn -DskipTests package --batch-mode

FROM openjdk:17-slim

COPY --from=build /build/target/*.jar /app.jar

COPY docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh
ENTRYPOINT [ "/docker-entrypoint.sh" ]
