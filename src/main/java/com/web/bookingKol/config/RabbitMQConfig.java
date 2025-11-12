package com.web.bookingKol.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Log4j2
public class RabbitMQConfig {

    public static final String CUSTOMER_QUEUE = "customer.notifications";
    public static final String CLEANER_QUEUE = "cleaner.notifications";
    public static final String EXCHANGE_QUEUE = "notificationExchange";

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    Queue customersQueue() {
        return new Queue(CUSTOMER_QUEUE);
    }

    @Bean
    Queue cleanerQueue() {
        return new Queue(CLEANER_QUEUE);
    }

    @Bean
    DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_QUEUE);
    }

    @Bean
    Binding customerBinding(Queue customersQueue, DirectExchange exchange) {
        return BindingBuilder.bind(customersQueue).to(exchange).with(CUSTOMER_QUEUE);
    }

    @Bean
    Binding cleanerBinding(Queue cleanerQueue, DirectExchange exchange) {
        return BindingBuilder.bind(cleanerQueue).to(exchange).with(CLEANER_QUEUE);
    }

    @Bean
    public Queue chatMessageQueue() {
        return new Queue("chat-messages", true);
    }
}
