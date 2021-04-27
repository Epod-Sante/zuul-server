package ca.uqtr.zuulserver.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {

    @Value("${rabbitmq.exchange}")
    private String exchange;
    @Value("${rabbitmq.dead-letter-exchange}")
    private String deadLetterExchange;

    @Value("${rabbitmq.queue}")
    private String queue;
    @Value("${rabbitmq.dead-letter-queue}")
    private String deadLetterQueue;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;
    @Value("${rabbitmq.dlq-routing-key}")
    private String DLQRoutingKey;

    @Bean
    DirectExchange exchange() {
        return new DirectExchange(exchange);
    }
    @Bean
    DirectExchange DLX() {
        return new DirectExchange("dead-letter-exchange");
    }

    @Bean
    Queue queue() {
        return QueueBuilder.durable(queue)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", deadLetterQueue)
                .build();
    }
    @Bean
    Queue DLQ() {
        return QueueBuilder.durable(deadLetterQueue).build();
    }

    @Bean
    Binding binding() {
        return BindingBuilder.bind(queue()).to(exchange()).with(routingKey);
    }
    @Bean
    Binding DLQBinding() {
        return BindingBuilder.bind(DLQ()).to(DLX()).with(DLQRoutingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("fish.rmq.cloudamqp.com");
        connectionFactory.setUsername("bklaoghl");
        connectionFactory.setPassword("PPdAiJgyoVKYPdNdIdOvMt8F8F7l2B9K");
        connectionFactory.setVirtualHost("bklaoghl");
        return connectionFactory;
    }


    @Bean
    public RabbitTemplate rabbitTemplate() {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
