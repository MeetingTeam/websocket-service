package meetingteam.websocketservice.services.impls;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import meetingteam.websocketservice.configs.ServiceUrlConfig;
import meetingteam.websocketservice.services.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final ServiceUrlConfig serviceUrlConfig;
    private final RestClient restClient;

    @Override
    @Retry(name="restApi")
    @CircuitBreaker(name="restCircuitBreaker")
    public void changeUserStatus(String jwtToken, Boolean isOnline) {
        URI uri= UriComponentsBuilder.fromHttpUrl(serviceUrlConfig.userServiceUrl())
                .path("/user/private/user-status")
                .queryParam("isOnline", isOnline)
                .build().toUri();

        restClient.post()
                .uri(uri)
                .headers(h->h.setBearerAuth(jwtToken))
                .retrieve()
                .body(Void.class);
    }
}
