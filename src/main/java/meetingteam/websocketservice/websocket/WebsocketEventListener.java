package meetingteam.websocketservice.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meetingteam.websocketservice.services.UserService;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebsocketEventListener {
    private final UserService userService;
    private final RabbitAdmin rabbitAdmin;
    private final Queue websocketQueue;
    private final Exchange websocketExchange;
    private final Map<String, Set<String>> sessionIdSubscriptionsMap= new ConcurrentHashMap();
    private final Map<String, AtomicInteger> subscriptionNumMap = new ConcurrentHashMap();

    @EventListener
    public void handleWebSocketConnection(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor=StompHeaderAccessor.wrap(event.getMessage());
        Authentication auth=(Authentication) headerAccessor.getUser();
        String jwtToken= ((Jwt) auth.getPrincipal()).getTokenValue();
        userService.changeUserStatus(jwtToken, true);
    }

    @EventListener
    public void handleWebSocketDisconnection(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor=StompHeaderAccessor.wrap(event.getMessage());

        String sessionId=headerAccessor.getSessionId();
        sessionIdSubscriptionsMap.computeIfPresent(sessionId, (key, value)->{
            value.forEach(this::checkSubscriptionNumMap);
            return null;
        });

        Authentication auth=(Authentication) headerAccessor.getUser();
        String jwtToken= ((Jwt) auth.getPrincipal()).getTokenValue();
        userService.changeUserStatus(jwtToken, false);
    }

    @EventListener
    public void handleSubscription(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor=StompHeaderAccessor.wrap(event.getMessage());
        String subscribedTopic = accessor.getDestination();

        if(subscribedTopic!=null){
            String sessionId= accessor.getSessionId();

            sessionIdSubscriptionsMap.computeIfAbsent(sessionId,
                        key->new HashSet()).add(subscribedTopic);
            subscriptionNumMap.computeIfAbsent(subscribedTopic,
                    key -> new AtomicInteger(0)).incrementAndGet();

            Binding binding= BindingBuilder
                    .bind(websocketQueue)
                    .to(websocketExchange)
                    .with(subscribedTopic)
                    .noargs();
            rabbitAdmin.declareBinding(binding);
        }
    }

    @EventListener
    public void handleUnSubscription(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor=StompHeaderAccessor.wrap(event.getMessage());
        String subscribedTopic = accessor.getDestination();

        if(subscribedTopic!=null){
            String sessionId= accessor.getSessionId();
            sessionIdSubscriptionsMap.computeIfPresent(sessionId,
                    (key, value)->{
                        value.remove(subscribedTopic);
                        return value;
                    });
            checkSubscriptionNumMap(subscribedTopic);
        }
    }

    private void checkSubscriptionNumMap(String subscribedTopic) {
        int count = subscriptionNumMap.get(subscribedTopic).decrementAndGet();
        if(count<=0){
            Binding binding= BindingBuilder
                    .bind(websocketQueue)
                    .to(websocketExchange)
                    .with(subscribedTopic)
                    .noargs();
            rabbitAdmin.removeBinding(binding);
        }
    }
}
