FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY . .
RUN chmod +x gradlew \
    && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache tzdata \
    && addgroup -S spring && adduser -S spring -G spring
COPY --from=build /workspace/build/libs/*.jar app.jar
USER spring
EXPOSE 8080
ENV TZ=Asia/Seoul
ENV JAVA_TOOL_OPTIONS="-Duser.timezone=Asia/Seoul"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

