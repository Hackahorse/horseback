FROM adoptopenjdk/openjdk11:latest
EXPOSE 8081
VOLUME /tmp
ADD /build/libs/horseback-1.1.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]