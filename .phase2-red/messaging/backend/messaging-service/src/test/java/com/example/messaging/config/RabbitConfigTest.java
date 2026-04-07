package com.example.messaging.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
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
    void exchangeAndQueuesUseLegacyNames() {
        DirectExchange exchange = config.exchange();
        Queue adminQueue = config.adminQueue();
        Queue userQueue = config.userQueue();
        Binding adminBinding = config.bindingAdmin(adminQueue, exchange);
        Binding userBinding = config.bindingUser(userQueue, exchange);

        assertEquals("chat_exchange", exchange.getName());
        assertEquals("admin_queue", adminQueue.getName());
        assertEquals("user_queue", userQueue.getName());
        assertEquals("admin", adminBinding.getRoutingKey());
        assertEquals("user", userBinding.getRoutingKey());
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
            assertThat(context).hasBean("bindingAdmin");
            assertThat(context).hasBean("bindingUser");
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
