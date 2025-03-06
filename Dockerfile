FROM openjdk:17-oracle
ARG JAR_FILE=target/*.jar
COPY build/libs/dns-service-0.0.1-SNAPSHOT-plain.jar  app.jar
ENTRYPOINT ["java","-jar","/app.jar"]