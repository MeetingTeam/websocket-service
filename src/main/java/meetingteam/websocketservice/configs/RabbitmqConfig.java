package meetingteam.websocketservice.configs;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;
import java.util.UUID;

@Configuration
public class RabbitmqConfig {
    private final String websocketQueueName = UUID.randomUUID().toString();

    @Value("${rabbitmq.exchange-name}")
    private String websocketExchangeName;

    @Bean
    public Queue websocketQueue() {
        return new Queue(websocketQueueName, false, true, false);
    }

    @Bean
    public TopicExchange websocketTopicExchange() {
        return new TopicExchange(websocketExchangeName);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(RabbitTemplate rabbitTemplate) {
        return new RabbitAdmin(rabbitTemplate);
    }
}
