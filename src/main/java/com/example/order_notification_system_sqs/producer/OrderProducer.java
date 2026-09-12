package com.example.order_notification_system_sqs.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.order_notification_system_sqs.dto.OrderRequest;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.extern.slf4j.Slf4j;

@Service 
@Slf4j 
public class OrderProducer {
    private final SqsTemplate sqsTemplate;
    private final String queueName;

    public OrderProducer(SqsTemplate sqsTemplate,
        @Value("${app.queue.order-events}") String queueName) {
        this.sqsTemplate = sqsTemplate;
        this.queueName = queueName;
    }

    public void sendOrder(OrderRequest orderRequest) {
        log.info("Sending order to queue: {}", orderRequest);
        sqsTemplate.send(queueName, orderRequest);
    }
}
