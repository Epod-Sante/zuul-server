package ca.uqtr.zuulserver.controller;

import ca.uqtr.zuulserver.entity.Message;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQSender {

    private final AmqpTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key}")
    private String routingkey;

    @Autowired
    public RabbitMQSender(AmqpTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(Message message) {
        System.out.println("Exchange : " + exchange + ", Routing key : " + routingkey + ", Message : " + message);
        rabbitTemplate.convertAndSend(exchange, routingkey, message);
        System.out.println("Success");

    }
}
