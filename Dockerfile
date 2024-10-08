# # ---- Build ---- #
# # Set up build image
# FROM maven:3.9.6-amazoncorretto-21 as BUILDER
# # Copy source files to project
# COPY . .
# # Build the application
# RUN mvn clean package -Pprod -DskipTests

# # ---- Package ---- #
# # Create the runtime image
# FROM amazoncorretto:21-alpine-jdk
# # Copy the built jar file from the build stage to the runtime image
# COPY --from=BUILDER /target/hpm.jar demo.jar
# EXPOSE 8880
# # Set the entrypoint
# CMD ["java", "-jar", "demo.jar"]


# ---- Build Stage ---- #
FROM maven:3.9.6-eclipse-temurin-21-alpine as BUILDER

WORKDIR /app

# Copy pom.xml and install dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -Pprod

# Copy the source code and build the application
COPY src ./src
RUN mvn clean package -Pprod -DskipTests

# ---- Runtime Stage ---- #
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the jar from the builder stage
COPY --from=BUILDER /app/target/hpm.jar app.jar

# Expose the port
EXPOSE 8880

# Run the application
CMD ["java", "-jar", "app.jar"]