#Build the application
FROM maven:3.8.0-openjdk-11 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean install

#Run the application
FROM openjdk:11
WORKDIR /app
COPY --from=build /app/target/ShowTime-0.0.1-SNAPSHOT.jar showTime.jar
EXPOSE 8080
CMD ["java", "-jar", "showTime.jar"]