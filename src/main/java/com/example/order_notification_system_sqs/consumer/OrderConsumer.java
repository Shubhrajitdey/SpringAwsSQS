package com.example.order_notification_system_sqs.consumer;

import com.example.order_notification_system_sqs.dto.OrderRequest;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderConsumer {
    @SqsListener("${app.queue.order-events}")
    public void listen(OrderRequest order) {
        log.info("Processing order: ID={}, Product={}, Quantity={}", 
                order.getOrderId(), order.getProductId(), order.getQuantity());

        // Error condition to test DLQ redirection
        if (order.getQuantity() <= 0) {
            log.error("Invalid quantity [{}] for order {}. Triggering exception.", 
                    order.getQuantity(), order.getOrderId());
            throw new IllegalArgumentException("Quantity must be greater than zero!");
        }

        // Simulated business logic (Email / Push notification)
        log.info("Notification sent successfully for order: {}", order.getOrderId());
    }
}
