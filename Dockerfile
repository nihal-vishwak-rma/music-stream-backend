FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/music-app.jar music-app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","music-app.jar"]
