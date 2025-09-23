# =======================
# 1. Build stage
# =======================
FROM gradle:8.10.2-jdk21 AS builder
WORKDIR /app

# Copy project
COPY . .

# Build jar (bỏ qua test để nhanh hơn)
RUN gradle clean bootJar --no-daemon -x test

# =======================
# 2. Runtime stage
# =======================
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy jar từ stage build
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=dev

ENTRYPOINT ["java","-jar","app.jar"]
