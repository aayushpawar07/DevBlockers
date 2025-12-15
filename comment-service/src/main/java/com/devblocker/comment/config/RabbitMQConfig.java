package com.devblocker.comment.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String EXCHANGE_NAME = "comment.events";
    public static final String COMMENT_ADDED_QUEUE = "comment.added.queue";
    public static final String COMMENT_ADDED_ROUTING_KEY = "comment.added";
    
    @Bean
    public TopicExchange commentEventsExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }
    
    @Bean
    public Queue commentAddedQueue() {
        return QueueBuilder.durable(COMMENT_ADDED_QUEUE).build();
    }
    
    @Bean
    public Binding commentAddedBinding() {
        return BindingBuilder
                .bind(commentAddedQueue())
                .to(commentEventsExchange())
                .with(COMMENT_ADDED_ROUTING_KEY);
    }
    
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}

