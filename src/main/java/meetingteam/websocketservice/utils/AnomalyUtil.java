package meetingteam.websocketservice.utils;

import io.opentelemetry.api.trace.Span;

public class AnomalyUtil {
    public static void markAnomalySpan(String anomalyEnvName){
       Span.current().setAttribute("IS_ANOMALY", "true");
       Span.current().setAttribute(anomalyEnvName, "true");
    }
}
