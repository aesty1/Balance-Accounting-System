FROM gradle:8.6.0-jdk21 AS build
WORKDIR /app
COPY build.gradle .
COPY settings.gradle .
COPY src ./src
RUN gradle clean build -x test --info

FROM openjdk:21-oracle
WORKDIR /app
COPY --from=build /app/build/libs/*.jar /app/balance.jar
ENTRYPOINT ["java", "-jar", "balance.jar"]