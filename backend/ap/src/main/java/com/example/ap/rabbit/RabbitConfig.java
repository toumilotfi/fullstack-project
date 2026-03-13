package com.example.ap.rabbit;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "chat_exchange";
    public static final String ADMIN_QUEUE = "admin_queue";
    public static final String USER_QUEUE = "user_queue";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue adminQueue() {
        return new Queue(ADMIN_QUEUE);
    }

    @Bean
    public Queue userQueue() {
        return new Queue(USER_QUEUE);
    }

    @Bean
    public Binding bindingAdmin(Queue adminQueue, DirectExchange exchange) {
        return BindingBuilder.bind(adminQueue).to(exchange).with("admin");
    }

    @Bean
    public Binding bindingUser(Queue userQueue, DirectExchange exchange) {
        return BindingBuilder.bind(userQueue).to(exchange).with("user");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    @Bean
    public AmqpTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                       MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}