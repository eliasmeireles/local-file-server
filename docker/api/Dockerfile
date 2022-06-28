FROM openjdk:11
MAINTAINER Elias Meireles
ENV PORT 80
COPY file-server.jar /service/
WORKDIR /service
EXPOSE $PORT
ENTRYPOINT ["java", "-jar", "file-server.jar"]
