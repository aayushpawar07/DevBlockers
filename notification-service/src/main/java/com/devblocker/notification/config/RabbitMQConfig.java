package com.devblocker.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    // Queues for consuming events from other services
    public static final String BLOCKER_CREATED_QUEUE = "notification.blocker.created.queue";
    public static final String COMMENT_ADDED_QUEUE = "notification.comment.added.queue";
    public static final String SOLUTION_ADDED_QUEUE = "notification.solution.added.queue";
    public static final String SOLUTION_ACCEPTED_QUEUE = "notification.solution.accepted.queue";
    
    // Exchanges from other services (we consume from these)
    public static final String BLOCKER_EVENTS_EXCHANGE = "blocker.events";
    public static final String COMMENT_EVENTS_EXCHANGE = "comment.events";
    public static final String SOLUTION_EVENTS_EXCHANGE = "solution.events";
    
    @Bean
    public Queue blockerCreatedQueue() {
        return QueueBuilder.durable(BLOCKER_CREATED_QUEUE).build();
    }
    
    @Bean
    public Queue commentAddedQueue() {
        return QueueBuilder.durable(COMMENT_ADDED_QUEUE).build();
    }
    
    @Bean
    public Queue solutionAddedQueue() {
        return QueueBuilder.durable(SOLUTION_ADDED_QUEUE).build();
    }
    
    @Bean
    public Queue solutionAcceptedQueue() {
        return QueueBuilder.durable(SOLUTION_ACCEPTED_QUEUE).build();
    }
    
    // Exchange beans for consuming from other services
    @Bean
    public TopicExchange blockerEventsExchange() {
        return new TopicExchange(BLOCKER_EVENTS_EXCHANGE, true, false);
    }
    
    @Bean
    public TopicExchange commentEventsExchange() {
        return new TopicExchange(COMMENT_EVENTS_EXCHANGE, true, false);
    }
    
    @Bean
    public TopicExchange solutionEventsExchange() {
        return new TopicExchange(SOLUTION_EVENTS_EXCHANGE, true, false);
    }
    
    // Bindings to consume from other services' exchanges
    @Bean
    public Binding blockerCreatedBinding() {
        return BindingBuilder
                .bind(blockerCreatedQueue())
                .to(blockerEventsExchange())
                .with("blocker.created");
    }
    
    @Bean
    public Binding commentAddedBinding() {
        return BindingBuilder
                .bind(commentAddedQueue())
                .to(commentEventsExchange())
                .with("comment.added");
    }
    
    @Bean
    public Binding solutionAddedBinding() {
        return BindingBuilder
                .bind(solutionAddedQueue())
                .to(solutionEventsExchange())
                .with("solution.added");
    }
    
    @Bean
    public Binding solutionAcceptedBinding() {
        return BindingBuilder
                .bind(solutionAcceptedQueue())
                .to(solutionEventsExchange())
                .with("solution.accepted");
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

