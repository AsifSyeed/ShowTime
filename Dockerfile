FROM amazoncorretto:17
COPY . /app
WORKDIR /app
RUN ./mvnw clean package
#COPY /app/target/ShowTime-0.0.1-SNAPSHOT.jar showTime.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/target/ShowTime-0.0.1-SNAPSHOT.jar"]
#ENTRYPOINT ["/bin/sh"]