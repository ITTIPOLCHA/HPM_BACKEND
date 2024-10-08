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
# Use a smaller base image for Maven build
FROM maven:3.9.6-amazoncorretto-21-alpine as BUILDER

# Set working directory
WORKDIR /app

# Copy only the pom.xml and install dependencies first (leveraging Docker cache)
COPY pom.xml .
RUN mvn dependency:go-offline -Pprod

# Copy the rest of the source code
COPY src ./src

# Build the application
RUN mvn clean package -Pprod -DskipTests

# ---- Runtime Stage ---- #
# Use a smaller runtime JDK image for Amazon Corretto
FROM amazoncorretto:21-alpine-jdk

# Set working directory in the runtime container
WORKDIR /app

# Copy the built jar file from the build stage
COPY --from=BUILDER /app/target/hpm.jar app.jar

# Expose port 8880
EXPOSE 8880

# Run the application
CMD ["java", "-jar", "app.jar"]
