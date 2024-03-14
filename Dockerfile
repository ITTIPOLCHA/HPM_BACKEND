# ---- Build ---- #

# Set up build image
FROM maven:3.9.6-amazoncorretto-21 as BUILDER
WORKDIR /build

# Copy source files to project
COPY . .

# Build the application
RUN mvn -v
RUN mvn clean package -Pprod -DskipTests

# ---- Package ---- #

# Create the runtime image
FROM amazoncorretto:21-alpine-jdk

# Copy the built jar file to the runtime image
COPY --from=BUILDER /app/target/*.jar hpm.jar

# Set the entrypoint
CMD ["java", "-jar", "hpm.jar"]


# ARG JAR_FILE=target/*.jar
# COPY ${JAR_FILE} app.jar
# EXPOSE 8880
# ENTRYPOINT ["java","-jar","/app.jar"]