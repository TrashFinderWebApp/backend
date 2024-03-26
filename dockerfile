FROM amazoncorretto:17

WORKDIR /app
COPY build/libs/tfinder.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
