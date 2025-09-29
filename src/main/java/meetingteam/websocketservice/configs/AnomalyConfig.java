package meetingteam.websocketservice.configs;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="simulation.anomalies")
public record AnomalyConfig (
    Boolean enableStressNg,
    Boolean enableTc,
    Boolean enableDownServices,
    List<String> downServicesList,
    Boolean enableMissSpan,
    Boolean enableStrangeFlowCall,
    Boolean enableFanoutCall,
    Boolean enableAppError
) {}
