package com.ecommerce.backend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String ORDER_EVENTS_EXCHANGE = "order.events";
    public static final String INVENTORY_EVENTS_QUEUE = "inventory.events";
    public static final String EMAIL_NOTIFICATIONS_QUEUE = "email.notifications";
    public static final String ORDER_NOTIFICATIONS_QUEUE = "order.notifications";
    public static final String CART_ABANDONED_QUEUE = "cart.abandoned.notifications";

    @Bean
    public FanoutExchange orderEventsExchange() {
        return new  FanoutExchange(ORDER_EVENTS_EXCHANGE);
    }

    @Bean
    public Queue inventoryEventsQueue() {
        return new  Queue(INVENTORY_EVENTS_QUEUE);
    }

    @Bean
    public Queue emailNotificationsQueue() {
        return new  Queue(EMAIL_NOTIFICATIONS_QUEUE);
    }

    @Bean
    public Binding inventoryBinding(Queue inventoryEventsQueue, FanoutExchange orderEventsExchange) {
        return BindingBuilder.bind(inventoryEventsQueue).to(orderEventsExchange);
    }

    @Bean
    public Binding emailBinding(Queue emailNotificationsQueue, FanoutExchange orderEventsExchange) {
        return BindingBuilder.bind(emailNotificationsQueue).to(orderEventsExchange);
    }

    @Bean
    public Queue orderNotificationsQueue() {
        return new  Queue(ORDER_NOTIFICATIONS_QUEUE);
    }

    @Bean
    public Binding orderNotificationsBinding(Queue orderNotificationsQueue, FanoutExchange orderEventsExchange) {
        return BindingBuilder.bind(orderNotificationsQueue).to(orderEventsExchange);
    }

    @Bean
    public Queue cartAbandonedQueue() {
        return new  Queue(CART_ABANDONED_QUEUE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
