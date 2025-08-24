package meetingteam.websocketservice.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import meetingteam.commonlibrary.dtos.SocketDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitmqListener {
    private final SimpMessagingTemplate messageTemplate;
    private final ObjectMapper objectMapper=new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @RabbitListener(queues = "#{websocketQueue.name}")
    public void listenWsMessages(String socketDtoStr){
        try{
            SocketDto socketDto=objectMapper.readValue(socketDtoStr, SocketDto.class);
            messageTemplate.convertAndSend(socketDto.getDest(), socketDto);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
