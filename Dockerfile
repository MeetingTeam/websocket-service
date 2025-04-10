FROM openjdk:17-jdk-alpine

# Change directory
WORKDIR /app

# Copy war file
COPY target/websocket-service-0.0.1-SNAPSHOT.war websocket-service.war

# Create non-root user
RUN adduser -D websocket_service
RUN chown -R websocket_service:websocket_service /app
USER websocket_service

# Run app
ENTRYPOINT ["sh","-c","java -jar -Dspring.config.location=$CONFIG_PATH websocket-service.war"]

## Expose port 8084
EXPOSE 8084