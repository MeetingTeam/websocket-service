package meetingteam.websocketservice.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meetingteam.websocketservice.services.TeamService;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class WebsocketInterceptor implements WebSocketMessageBrokerConfigurer {
	private final JwtDecoder jwtDecoder;
	private final JwtAuthenticationConverter jwtAuthConverter;
	private final RabbitAdmin rabbitAdmin;
	private final Queue websocketQueue;
	private final Exchange websocketExchange;
	private final TeamService teamService;

	@Value("${websocket.security.auth-header}")
	private String authHeader;

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(new ChannelInterceptor() {
			@Override
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				StompHeaderAccessor accessor= MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
				if(accessor!=null && StompCommand.CONNECT.equals(accessor.getCommand())) {
					String jwtToken = accessor.getFirstNativeHeader(authHeader);

					Jwt jwt=jwtDecoder.decode(jwtToken);
					var authToken=jwtAuthConverter.convert(jwt);
					accessor.setUser(authToken);
				}
				else if(accessor!=null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
					String subscribedTopic = accessor.getDestination();

					Authentication authentication = (Authentication) accessor.getUser();
					if (authentication == null || !authentication.isAuthenticated()) {
						throw new AccessDeniedException("Unauthorized subscription attempt to " + subscribedTopic);
					}

					if(subscribedTopic.startsWith("user.")){
						String subscriberId=subscribedTopic.substring("user.".length());
						if(!subscriberId.equals(authentication.getName())){
							throw new AccessDeniedException("Unauthorized subscription attempt to " + subscribedTopic);
						}
					}
					else if(subscribedTopic.startsWith("team.")){
						String teamId=subscribedTopic.substring("team.".length());
						if(!teamService.isMemberOfTeam(authentication.getName(), teamId, null))
							throw new AccessDeniedException("Unauthorized subscription attempt to " + subscribedTopic);

						Binding binding= BindingBuilder
								.bind(websocketQueue)
								.to(websocketExchange)
								.with(subscribedTopic)
								.noargs();
						rabbitAdmin.declareBinding(binding);
					}
				}
				else if( StompCommand.SEND.equals(accessor.getCommand())){
					throw new AccessDeniedException("Unauthorized send attempt to " + accessor.getDestination());
				}
				return message;
			}
		});
	}
}
