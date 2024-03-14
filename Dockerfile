# Stage 1: Build with Maven
FROM maven:3.8.7-openjdk-18 AS builder
COPY . .
RUN mvn clean package -DskipTests
# Stage 2: Create the final image
FROM openjdk:18-jdk-slim
COPY --from=builder /target/*.jar app.jar
EXPOSE 8880
ENTRYPOINT [ "java","-jar","/app.jar" ]


# ARG JAR_FILE=target/*.jar
# COPY ${JAR_FILE} app.jar
# EXPOSE 8880
# ENTRYPOINT ["java","-jar","/app.jar"]