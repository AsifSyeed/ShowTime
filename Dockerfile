# Build the application
FROM maven:3.8.6-openjdk-11 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean install

# Run the application
FROM openjdk:11
WORKDIR /app
COPY --from=build /app/target/ShowTime-0.0.1-SNAPSHOT.jar showTime.jar

# Copy PDF files
COPY src/main/resources/assets/genericTickets /app/src/main/resources/assets/genericTickets

EXPOSE 8080
CMD ["java", "-jar", "showTime.jar"]

# Build Command
# docker buildx build --platform linux/amd64 -t asifsyeed/counters_bd:0.0.3 .

# Push Command
# docker push asifsyeed/counters_bd:0.0.3

# Pull and run Command
# sudo docker run -d -p 80:8080 asifsyeed/counters_bd:0.0.3