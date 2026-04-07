package com.example.notification.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RabbitConfigTest {

    private final RabbitConfig config = new RabbitConfig();

    @Test
    void exchangeQueuesAndBindingsUseExpectedNames() {
        TopicExchange exchange = config.eventExchange();
        Queue notificationQueue = config.notificationQueue();
        Queue emailQueue = config.emailQueue();
        Binding notificationBinding = config.notificationBinding(notificationQueue, exchange);
        Binding emailBinding = config.emailBinding(emailQueue, exchange);

        assertEquals("event_exchange", exchange.getName());
        assertEquals("notification_events_queue", notificationQueue.getName());
        assertEquals("email_events_queue", emailQueue.getName());
        assertEquals("event.notification.#", notificationBinding.getRoutingKey());
        assertEquals("event.email.#", emailBinding.getRoutingKey());
    }

    @Test
    void rabbitTemplateUsesJsonMessageConverter() {
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        MessageConverter converter = config.messageConverter();

        RabbitTemplate template = config.rabbitTemplate(connectionFactory, converter);

        assertSame(converter, template.getMessageConverter());
    }

    @Test
    void applicationContextCreatesBothBindings() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                .withUserConfiguration(RabbitConfig.class, TestConnectionFactoryConfig.class);

        contextRunner.run(context -> {
            assertThat(context.getStartupFailure()).isNull();
            assertThat(context).hasBean("notificationBinding");
            assertThat(context).hasBean("emailBinding");
        });
    }

    @Configuration
    static class TestConnectionFactoryConfig {

        @Bean
        ConnectionFactory connectionFactory() {
            return mock(ConnectionFactory.class);
        }
    }
}
