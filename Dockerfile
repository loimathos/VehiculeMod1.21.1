FROM --platform=linux/amd64 eclipse-temurin:21-jdk

WORKDIR /workspace

# Copy gradle wrapper first for caching
COPY gradlew gradlew
COPY gradle gradle
RUN chmod +x gradlew

# Copy build files
COPY build.gradle .
COPY gradle.properties .
COPY settings.gradle .

# Download dependencies (cached layer)
RUN ./gradlew --no-daemon dependencies || true

# Copy source
COPY src src

# Build - capture all errors to a file
RUN ./gradlew --no-daemon compileJava -Xmaxerrs=99999 > /tmp/gradle.log 2>&1; cat /tmp/gradle.log | grep "error:" | head -500
