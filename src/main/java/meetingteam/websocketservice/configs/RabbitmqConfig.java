package meetingteam.websocketservice.configs;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
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
    private final String websocketQueueName = "websocket-queue-"+UUID.randomUUID().toString();

    @Value("${rabbitmq.exchange-name}")
    private String exchangeName;

    @Bean
    public Queue websocketQueue() {
        return new Queue(websocketQueueName, false, true, false);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding bindUserTopic(Queue websocketQueue, TopicExchange exchange){
        return BindingBuilder
            .bind(websocketQueue)
            .to(exchange)
            .with("/topic/user.#");
    }

    @Bean
    public Binding bindTeamTopic(Queue websocketQueue, TopicExchange exchange){
        return BindingBuilder
            .bind(websocketQueue)
            .to(exchange)
            .with("/topic/team.#");
    }

    @Bean
    public RabbitAdmin rabbitAdmin(RabbitTemplate rabbitTemplate) {
        return new RabbitAdmin(rabbitTemplate);
    }
}
