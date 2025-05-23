package meetingteam.websocketservice.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meetingteam.websocketservice.services.TeamService;
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
	private final TeamService teamService;

	@Value("${websocket.security.auth-header}")
	private String authHeader;

	private final String userTopicPrefix="/topic/user.";
	private final String teamTopicPrefix="/topic/team.";

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(new ChannelInterceptor() {
			@Override
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				StompHeaderAccessor accessor= MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
				if(accessor!=null && StompCommand.CONNECT.equals(accessor.getCommand())) {
					try{
						String jwtToken = accessor.getFirstNativeHeader(authHeader).substring("Bearer ".length());

						Jwt jwt=jwtDecoder.decode(jwtToken);
						var authToken=jwtAuthConverter.convert(jwt);
						accessor.setUser(authToken);
					}
					catch(Exception ex){
						throw new AccessDeniedException("Validation exception: "+ex.getMessage());
					}
					
				}
				else if(accessor!=null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
					String subscribedTopic = accessor.getDestination();

					Authentication authentication = (Authentication) accessor.getUser();
					if (authentication == null || !authentication.isAuthenticated()) {
						throw new AccessDeniedException("Unauthorized subscription attempt to " + subscribedTopic);
					}

					if(subscribedTopic.startsWith(userTopicPrefix)){
						String subscriberId=subscribedTopic.substring(userTopicPrefix.length());
						if(!subscriberId.equals(authentication.getName())){
							throw new AccessDeniedException("Unauthorized subscription attempt to " + subscribedTopic);
						}
					}
					else if(subscribedTopic.startsWith(teamTopicPrefix)){
						String teamId=subscribedTopic.substring(teamTopicPrefix.length());
						if(!teamService.isMemberOfTeam(authentication.getName(), teamId, null))
							throw new AccessDeniedException("Unauthorized subscription attempt to " + subscribedTopic);
					}
					else throw new AccessDeniedException("Unauthorized subscription attempt to " + subscribedTopic);
				}
				else if(StompCommand.SEND.equals(accessor.getCommand())){
					throw new AccessDeniedException("Unauthorized send attempt to " + accessor.getDestination());
				}
				return message;
			}
		});
	}
}
