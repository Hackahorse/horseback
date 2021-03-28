FROM adoptopenjdk/openjdk11:latest
EXPOSE 8081
VOLUME /tmp
ADD /build/libs/backend-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]