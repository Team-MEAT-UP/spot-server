FROM eclipse-temurin:21-alpine

ARG JAR_FILE=./*.jar
COPY ${JAR_FILE} spot.jar

ENTRYPOINT ["java", "-jar", "spot.jar"]
