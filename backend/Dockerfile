FROM openjdk:8-jdk-alpine

EXPOSE 8080

WORKDIR /app
COPY build/libs/guestbook-0.1.0.jar /app/app.jar

ENV SPRING_PROFILES_ACTIVE=memory

CMD ["java", "-jar", "/app/app.jar"]
