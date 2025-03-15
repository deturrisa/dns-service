FROM openjdk:17-oracle
ARG JAR_FILE=target/*.jar
COPY build/libs/*jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]