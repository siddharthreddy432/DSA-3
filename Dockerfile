# Stage 1: Build environment
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy project definition and source tree
COPY pom.xml ./
COPY src ./src

# Compile sources directly with javac into classes directory and package JAR
RUN mkdir -p target/classes && \
    find src/main/java -name "*.java" > sources.txt && \
    javac -d target/classes @sources.txt && \
    jar --create --file target/kill-chain-correlation-engine-1.0.0.jar \
        --main-class engine.Main -C target/classes .

# Stage 2: Minimal runtime environment
FROM eclipse-temurin:17-jre-alpine AS runner

WORKDIR /app

# Copy compiled JAR and data folders from builder stage
COPY --from=builder /app/target/kill-chain-correlation-engine-1.0.0.jar ./app.jar
COPY data ./data

# Run application entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]
