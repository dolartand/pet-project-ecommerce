package com.ecommerce.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String ORDER_EVENTS_EXCHANGE = "order.events.exchange";
    public static final String USER_EVENTS_EXCHANGE = "user.events.exchange";
    public static final String CART_EVENTS_EXCHANGE = "cart.events.exchange";
    public static final String DEAD_LETTER_EXCHANGE = "dead.letter.exchange";

    public static final String ORDER_NOTIFICATIONS_QUEUE = "order.notifications.queue";
    public static final String INVENTORY_EVENTS_QUEUE = "inventory.events.queue";
    public static final String USER_REGISTRATION_NOTIFICATIONS_QUEUE = "user.registration.notifications.queue";
    public static final String CART_ABANDONED_NOTIFICATIONS_QUEUE = "cart.abandoned.notifications.queue";

    public static final String ORDER_NOTIFICATIONS_DLQ = ORDER_NOTIFICATIONS_QUEUE + ".dlq";
    public static final String INVENTORY_EVENTS_DLQ = INVENTORY_EVENTS_QUEUE + ".dlq";
    public static final String USER_REGISTRATION_NOTIFICATIONS_DLQ = USER_REGISTRATION_NOTIFICATIONS_QUEUE + ".dlq";
    public static final String CART_ABANDONED_NOTIFICATIONS_DLQ = CART_ABANDONED_NOTIFICATIONS_QUEUE + ".dlq";

    @Bean
    public TopicExchange orderEventsExchange() {
        return new TopicExchange(ORDER_EVENTS_EXCHANGE);
    }

    @Bean
    public TopicExchange userEventsExchange() {
        return new TopicExchange(USER_EVENTS_EXCHANGE);
    }

    @Bean
    public TopicExchange cartEventsExchange() {
        return new TopicExchange(CART_EVENTS_EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Queue inventoryEventsQueue() {
        return QueueBuilder.durable(INVENTORY_EVENTS_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", INVENTORY_EVENTS_DLQ)
                .build();
    }

    @Bean
    public Queue inventoryEventsDlq() {
        return new Queue(INVENTORY_EVENTS_DLQ);
    }

    @Bean
    public Queue orderNotificationsQueue() {
        return QueueBuilder.durable(ORDER_NOTIFICATIONS_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ORDER_NOTIFICATIONS_DLQ)
                .build();
    }

    @Bean
    public Queue orderNotificationsDlq() {
        return new Queue(ORDER_NOTIFICATIONS_DLQ);
    }

    @Bean
    public Queue userRegistrationNotificationsQueue() {
        return QueueBuilder.durable(USER_REGISTRATION_NOTIFICATIONS_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", USER_REGISTRATION_NOTIFICATIONS_DLQ)
                .build();
    }

    @Bean
    public Queue userRegistrationNotificationsDlq() {
        return new Queue(USER_REGISTRATION_NOTIFICATIONS_DLQ);
    }

    @Bean
    public Queue cartAbandonedNotificationsQueue() {
        return QueueBuilder.durable(CART_ABANDONED_NOTIFICATIONS_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", CART_ABANDONED_NOTIFICATIONS_DLQ)
                .build();
    }

    @Bean
    public Queue cartAbandonedNotificationsDlq() {
        return new Queue(CART_ABANDONED_NOTIFICATIONS_DLQ);
    }

    @Bean
    public Binding inventoryBinding() {
        return BindingBuilder.bind(inventoryEventsQueue()).to(orderEventsExchange()).with("order.#");
    }

    @Bean
    public Binding orderNotificationBinding() {
        return BindingBuilder.bind(orderNotificationsQueue()).to(orderEventsExchange()).with("order.#");
    }

    @Bean
    public Binding userRegistrationBinding() {
        return BindingBuilder.bind(userRegistrationNotificationsQueue()).to(userEventsExchange()).with("user.registered");
    }

    @Bean
    public Binding cartAbandonedBinding() {
        return BindingBuilder.bind(cartAbandonedNotificationsQueue()).to(cartEventsExchange()).with("cart.abandoned");
    }

    @Bean
    public Binding inventoryDlqBinding() {
        return BindingBuilder.bind(inventoryEventsDlq()).to(deadLetterExchange()).with(INVENTORY_EVENTS_DLQ);
    }

    @Bean
    public Binding orderNotificationsDlqBinding() {
        return BindingBuilder.bind(orderNotificationsDlq()).to(deadLetterExchange()).with(ORDER_NOTIFICATIONS_DLQ);
    }

    @Bean
    public Binding userRegistrationDlqBinding() {
        return BindingBuilder.bind(userRegistrationNotificationsDlq()).to(deadLetterExchange()).with(USER_REGISTRATION_NOTIFICATIONS_DLQ);
    }

    @Bean
    public Binding cartAbandonedDlqBinding() {
        return BindingBuilder.bind(cartAbandonedNotificationsDlq()).to(deadLetterExchange()).with(CART_ABANDONED_NOTIFICATIONS_DLQ);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
