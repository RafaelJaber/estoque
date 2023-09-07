FROM tiagofsmg/openjdk:11

MAINTAINER Tiago Oliveira <tiago@giganet.psi.br>

COPY app.jar /app/

ENTRYPOINT ["java","-jar", "/app/app.jar"]