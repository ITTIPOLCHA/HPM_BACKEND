FROM openjdk:21-jdk-oraclelinux8

COPY ./src src/
COPY ./pom.xml pom.xml

RUN mvn clean package -DskipTests

FROM openjdk:21-jdk-oraclelinux8
COPY --from=builder target/*.jar app.jar
EXPOSE 8880
CMD ["java", "-jar", "app.jar"]

# ARG JAR_FILE=target/*.jar
# COPY ${JAR_FILE} app.jar
# EXPOSE 8880
# ENTRYPOINT ["java","-jar","/app.jar"]