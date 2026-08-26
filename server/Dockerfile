FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY gradlew gradlew
COPY gradle gradle
COPY gradle.properties gradle.properties
COPY server/docker server/docker
COPY server/src server/src
COPY src/main/java/com/timeback/backup src/main/java/com/timeback/backup
RUN ./gradlew --no-daemon -p server/docker bootJar

FROM eclipse-temurin:17-jre
WORKDIR /opt/timeback
COPY --from=build /workspace/server/docker/build/libs/timeback-server.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/opt/timeback/app.jar"]
