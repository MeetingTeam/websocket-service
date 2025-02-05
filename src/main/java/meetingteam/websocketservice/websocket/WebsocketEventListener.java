package meetingteam.websocketservice.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meetingteam.websocketservice.services.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebsocketEventListener {
    private final UserService userService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor=StompHeaderAccessor.wrap(event.getMessage());
        Authentication auth=(Authentication) headerAccessor.getUser();
        String jwtToken= ((Jwt) auth.getPrincipal()).getTokenValue();
        userService.changeUserStatus(jwtToken, true);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor=StompHeaderAccessor.wrap(event.getMessage());
        Authentication auth=(Authentication) headerAccessor.getUser();
        String jwtToken= ((Jwt) auth.getPrincipal()).getTokenValue();
        userService.changeUserStatus(jwtToken, false);
    }
}
