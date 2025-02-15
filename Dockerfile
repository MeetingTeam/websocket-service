FROM openjdk:17-jdk-alpine

## Change directory
WORKDIR /app

## Create non-root user
RUN adduser -D websocket_service
RUN chown -R websocket_service:websocket_service /app
USER websocket_service

## Copy war file and run app
COPY target/websocket-service-0.0.1-SNAPSHOT.war websocket_service.war
ENTRYPOINT ["java","-jar","websocket_service.war"]

## Expose port 8084
EXPOSE 8084