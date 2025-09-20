# Use official OpenJDK image as a base
FROM openjdk:17-jdk-slim

# Set working directory inside the container
WORKDIR /app

# Copy the JAR into the container
COPY target/Tutorverse-0.0.1-SNAPSHOT.jar app.jar

# Expose port (match your Spring Boot server.port, default 8080)
EXPOSE 8080

# Set JVM memory limits and garbage collection options
# -Xms: Initial heap size (512MB)
# -Xmx: Maximum heap size (1GB)
# -XX:MaxMetaspaceSize: Maximum metaspace size (256MB)
# -XX:+UseG1GC: Use G1 garbage collector for better performance
# -XX:MaxGCPauseMillis: Target maximum GC pause time
ENTRYPOINT ["java", "-Xms512m", "-Xmx1g", "-XX:MaxMetaspaceSize=256m", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200", "-jar", "app.jar"]
