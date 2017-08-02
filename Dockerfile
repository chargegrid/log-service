FROM openjdk:8-jre-alpine
COPY target/uberjar/log-service.jar /app/log-service.jar
WORKDIR /app
EXPOSE 8084
CMD ["java", "-jar", "log-service.jar"]
