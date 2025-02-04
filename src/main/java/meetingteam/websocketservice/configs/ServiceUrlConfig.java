package meetingteam.websocketservice.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="meetingteam.services")
public record ServiceUrlConfig (
        String userServiceUrl,
        String teamServiceUrl,
        String schedulerServiceUrl
) {}
