FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copy thư mục beatboxcompany
COPY beatboxcompany/ .

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081
CMD ["java", "-jar", "app.jar"]
