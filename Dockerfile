FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./

RUN ./gradlew build -x test --no-daemon || true

COPY . .

RUN ./gradlew clean build -x test --no-daemon



FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

RUN chown spring:spring app.jar

USER spring:spring

EXPOSE 8888

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]