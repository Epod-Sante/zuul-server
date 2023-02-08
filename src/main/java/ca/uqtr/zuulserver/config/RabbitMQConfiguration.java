package ca.uqtr.zuulserver.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
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
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
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
        //CachingConnectionFactory connectionFactory = new CachingConnectionFactory("b-6b2f3e20-0a87-48cc-bcea-9eabd0c63114.mq.ca-central-1.amazonaws.com");
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("ip-172-31-66-146.ca-central-1.compute.internal");
        connectionFactory.setUsername("admin");
        connectionFactory.setPassword("pod-isante2022");
        connectionFactory.setPort(5672);
        return connectionFactory;
    }


    @Bean
    public RabbitTemplate rabbitTemplate() {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public AmqpAdmin rabbitAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

}
