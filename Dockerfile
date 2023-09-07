FROM adoptopenjdk/openjdk11:latest

MAINTAINER Rafael Jaber <rafael.jaber@gmail.com>

COPY app.jar /app/

ENTRYPOINT ["java","-jar", "/app/app.jar"]