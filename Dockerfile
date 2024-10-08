# ---- Build ---- #
# Set up build image
FROM maven:3.9.6-amazoncorretto-21 as BUILDER
# Copy source files to project
COPY . .
# Build the application
RUN mvn clean package -Pprod -DskipTests

# ---- Package ---- #
# Create the runtime image
FROM amazoncorretto:21-alpine-jdk
# Copy the built jar file from the build stage to the runtime image
COPY --from=BUILDER /target/hpm.jar demo.jar
EXPOSE 8880
# Set the entrypoint
CMD ["java", "-jar", "demo.jar"]