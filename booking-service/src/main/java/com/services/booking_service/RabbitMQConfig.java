package com.services.booking_service;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String CUSTOMER_QUEUE = "customer.notifications";
    public static final String PROVIDER_QUEUE = "provider.notifications";

    public static final String BOOKING_EXCHANGE = "booking.exchange";

    public static final String CUSTOMER_KEY = "booking.customer";
    public static final String PROVIDER_KEY = "booking.provider";

    @Bean
    public Queue customerQueue() {
        return new Queue(CUSTOMER_QUEUE, true);
    }

    @Bean
    public Queue providerQueue() {
        return new Queue(PROVIDER_QUEUE, true);
    }

    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(BOOKING_EXCHANGE);
    }

    @Bean
    public Binding customerBinding() {
        return BindingBuilder
                .bind(customerQueue())
                .to(bookingExchange())
                .with(CUSTOMER_KEY);
    }

    @Bean
    public Binding providerBinding() {
        return BindingBuilder
                .bind(providerQueue())
                .to(bookingExchange())
                .with(PROVIDER_KEY);
    }
}