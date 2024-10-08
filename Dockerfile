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
# Use a smaller base image for Maven build to reduce size
FROM maven:3.9.6-eclipse-temurin-17-alpine as BUILDER

# Set the working directory
WORKDIR /app

# Copy only the necessary files for the build to avoid unnecessary rebuilds on Docker cache misses
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code after dependencies are cached to make use of Docker caching
COPY src ./src

# Build the application (skip tests for faster builds in production)
RUN mvn clean package -Pprod -DskipTests

# ---- Runtime Stage ---- #
# Use an even smaller JRE base image for runtime
FROM eclipse-temurin:17-jre-alpine

# Set working directory in the runtime image
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=BUILDER /app/target/hpm.jar app.jar

# Expose the required port
EXPOSE 8880

# Set the entrypoint to run the application
CMD ["java", "-jar", "app.jar"]
