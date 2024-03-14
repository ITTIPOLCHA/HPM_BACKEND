# Stage 1: Build with Maven
FROM maven:3.8.4-openjdk-17 AS builder

WORKDIR /app

COPY ./src ./src
COPY ./pom.xml .

RUN mvn clean package

# Stage 2: Create the final image
FROM openjdk:17-oraclelinux8

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8880

CMD ["java", "-jar", "app.jar"]


# ARG JAR_FILE=target/*.jar
# COPY ${JAR_FILE} app.jar
# EXPOSE 8880
# ENTRYPOINT ["java","-jar","/app.jar"]