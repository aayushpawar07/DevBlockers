package com.devblocker.solution.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String EXCHANGE_NAME = "solution.events";
    public static final String SOLUTION_ADDED_QUEUE = "solution.added.queue";
    public static final String SOLUTION_UPVOTED_QUEUE = "solution.upvoted.queue";
    public static final String SOLUTION_ACCEPTED_QUEUE = "solution.accepted.queue";
    
    public static final String SOLUTION_ADDED_ROUTING_KEY = "solution.added";
    public static final String SOLUTION_UPVOTED_ROUTING_KEY = "solution.upvoted";
    public static final String SOLUTION_ACCEPTED_ROUTING_KEY = "solution.accepted";
    
    @Bean
    public TopicExchange solutionEventsExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }
    
    @Bean
    public Queue solutionAddedQueue() {
        return QueueBuilder.durable(SOLUTION_ADDED_QUEUE).build();
    }
    
    @Bean
    public Queue solutionUpvotedQueue() {
        return QueueBuilder.durable(SOLUTION_UPVOTED_QUEUE).build();
    }
    
    @Bean
    public Queue solutionAcceptedQueue() {
        return QueueBuilder.durable(SOLUTION_ACCEPTED_QUEUE).build();
    }
    
    @Bean
    public Binding solutionAddedBinding() {
        return BindingBuilder
                .bind(solutionAddedQueue())
                .to(solutionEventsExchange())
                .with(SOLUTION_ADDED_ROUTING_KEY);
    }
    
    @Bean
    public Binding solutionUpvotedBinding() {
        return BindingBuilder
                .bind(solutionUpvotedQueue())
                .to(solutionEventsExchange())
                .with(SOLUTION_UPVOTED_ROUTING_KEY);
    }
    
    @Bean
    public Binding solutionAcceptedBinding() {
        return BindingBuilder
                .bind(solutionAcceptedQueue())
                .to(solutionEventsExchange())
                .with(SOLUTION_ACCEPTED_ROUTING_KEY);
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

