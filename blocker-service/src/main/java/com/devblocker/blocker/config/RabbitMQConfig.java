package com.devblocker.blocker.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String EXCHANGE_NAME = "blocker.events";
    public static final String BLOCKER_CREATED_QUEUE = "blocker.created.queue";
    public static final String BLOCKER_UPDATED_QUEUE = "blocker.updated.queue";
    public static final String BLOCKER_RESOLVED_QUEUE = "blocker.resolved.queue";
    
    // Consumed events
    public static final String ATTACHMENT_UPLOADED_QUEUE = "attachment.uploaded.queue";
    public static final String SOLUTION_ADDED_QUEUE = "solution.added.queue";
    public static final String SOLUTION_ACCEPTED_QUEUE = "solution.accepted.queue";
    
    // Solution service exchange
    public static final String SOLUTION_EVENTS_EXCHANGE = "solution.events";
    
    @Bean
    public TopicExchange blockerEventsExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }
    
    @Bean
    public TopicExchange solutionEventsExchange() {
        return new TopicExchange(SOLUTION_EVENTS_EXCHANGE);
    }
    
    @Bean
    public Queue blockerCreatedQueue() {
        return QueueBuilder.durable(BLOCKER_CREATED_QUEUE).build();
    }
    
    @Bean
    public Queue blockerUpdatedQueue() {
        return QueueBuilder.durable(BLOCKER_UPDATED_QUEUE).build();
    }
    
    @Bean
    public Queue blockerResolvedQueue() {
        return QueueBuilder.durable(BLOCKER_RESOLVED_QUEUE).build();
    }
    
    @Bean
    public Queue attachmentUploadedQueue() {
        return QueueBuilder.durable(ATTACHMENT_UPLOADED_QUEUE).build();
    }
    
    @Bean
    public Queue solutionAddedQueue() {
        return QueueBuilder.durable(SOLUTION_ADDED_QUEUE).build();
    }
    
    @Bean
    public Queue solutionAcceptedQueue() {
        return QueueBuilder.durable(SOLUTION_ACCEPTED_QUEUE).build();
    }
    
    @Bean
    public Binding blockerCreatedBinding() {
        return BindingBuilder
                .bind(blockerCreatedQueue())
                .to(blockerEventsExchange())
                .with("blocker.created");
    }
    
    @Bean
    public Binding blockerUpdatedBinding() {
        return BindingBuilder
                .bind(blockerUpdatedQueue())
                .to(blockerEventsExchange())
                .with("blocker.updated");
    }
    
    @Bean
    public Binding blockerResolvedBinding() {
        return BindingBuilder
                .bind(blockerResolvedQueue())
                .to(blockerEventsExchange())
                .with("blocker.resolved");
    }
    
    // Note: AttachmentUploaded and SolutionAdded events come from other services
    // They may use a different exchange. Adjust bindings based on actual event source.
    // For now, binding to blocker.events exchange - update when other services are ready
    @Bean
    public Binding attachmentUploadedBinding() {
        return BindingBuilder
                .bind(attachmentUploadedQueue())
                .to(blockerEventsExchange())
                .with("attachment.uploaded");
    }
    
    @Bean
    public Binding solutionAddedBinding() {
        return BindingBuilder
                .bind(solutionAddedQueue())
                .to(blockerEventsExchange())
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
        // Using Jackson2JsonMessageConverter for JSON serialization
        // Note: In Spring Boot 4.0+, consider using ObjectMapper directly
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
    
    @Bean
    public org.springframework.web.client.RestTemplate restTemplate() {
        return new org.springframework.web.client.RestTemplate();
    }
}

