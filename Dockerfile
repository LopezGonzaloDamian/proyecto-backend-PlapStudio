FROM gradle:8.7-jdk21 AS build

WORKDIR /home/gradle/src

COPY --chown=gradle:gradle . .

RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21.0.2_13-jre-jammy

EXPOSE 9000

COPY --from=build /home/gradle/src/build/libs/*.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
