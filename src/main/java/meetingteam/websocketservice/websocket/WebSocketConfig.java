package meetingteam.websocketservice.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer{
	@Value("${cors.origins}")
	private String origins;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		var originList= origins.split(",");
		registry.addEndpoint("/wss")
				.setAllowedOriginPatterns(originList)
				.withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.setUserDestinationPrefix("/user");
		registry.enableSimpleBroker("/topic","/queue");
	}
}
