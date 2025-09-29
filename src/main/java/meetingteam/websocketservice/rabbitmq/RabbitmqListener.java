package meetingteam.websocketservice.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meetingteam.commonlibrary.dtos.SocketDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitmqListener {
    private final SimpMessagingTemplate messageTemplate;
    private final ObjectMapper objectMapper=new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    @Value("${simulation.processed-delay:0}")
    private Integer processedDelay;
    @Value("${simulation.enable-app-bug:false}")
    private boolean enableAppBug;

    @RabbitListener(queues = "#{websocketQueue.name}")
    public void listenWsMessages(String socketDtoStr){
        if (enableAppBug){
            throw new RuntimeException("Test application bug");
        }
        try{
            log.info("Delay {}", processedDelay);
            Thread.sleep(processedDelay);
            
            SocketDto socketDto=objectMapper.readValue(socketDtoStr, SocketDto.class);
            messageTemplate.convertAndSend(socketDto.getDest(), socketDto);
            System.out.println("Receive ws message");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
