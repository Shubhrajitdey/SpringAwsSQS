package com.example.order_notification_system_sqs.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import com.example.order_notification_system_sqs.dto.OrderRequest;
import com.example.order_notification_system_sqs.model.DlqAuditLog;
import com.example.order_notification_system_sqs.repository.DlqAuditRepository;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DlqReDriveService {
    private final SqsTemplate sqsTemplate;
    private final DlqAuditRepository auditRepository;
    private final String mainQueueName;
    private final String dlqName;

    public DlqReDriveService(SqsTemplate sqsTemplate, 
        DlqAuditRepository auditRepository,
        @Value("${app.queue.order-events}") String mainQueueName, 
        @Value("${app.queue.order-events-dlq}") String dlqName) {
        this.sqsTemplate = sqsTemplate;
        this.auditRepository = auditRepository;
        this.mainQueueName = mainQueueName;
        this.dlqName = dlqName;
    }

    public List<DlqAuditLog> getAllAuditLogs() {
        return auditRepository.findAll();
    }

    public int redriveMessages(int maxMessages) {
        int movedCount = 0;
        for (int i = 0; i < maxMessages; i++) {
            Optional<Message<OrderRequest>> optionalMessage = sqsTemplate.receive(from -> from
                    .queue(dlqName)
                    .pollTimeout(Duration.ofSeconds(2)), OrderRequest.class);

            if (optionalMessage.isEmpty()) {
                log.info("No more messages available in DLQ: {}", dlqName);
                break;
            }

            OrderRequest order = optionalMessage.get().getPayload();
            log.info("Redriving message ID={} from DLQ to main queue", order.getOrderId());

            // Re-publish to main queue
            sqsTemplate.send(to -> to.queue(mainQueueName).payload(order));

            // Update audit log
            auditRepository.findById(order.getOrderId()).ifPresent(audit -> {
                audit.setStatus("REDRIVED");
                audit.setRedrivedAt(LocalDateTime.now());
            });

            movedCount++;
        }

        return movedCount;
    }

    public boolean redriveSpecificOrder(String orderId, Integer correctedQuantity) {
        Optional<DlqAuditLog> auditOpt = auditRepository.findById(orderId);
        if (auditOpt.isEmpty()) {
            log.warn("Order [{}] not found in DLQ audit repository", orderId);
            throw new IllegalArgumentException("Order [" + orderId + "] was not found in DLQ audit repository");
        }

        DlqAuditLog audit = auditOpt.get();
        if ("RESOLVED_AND_REDRIVED".equalsIgnoreCase(audit.getStatus()) || "REDRIVED".equalsIgnoreCase(audit.getStatus())) {
            log.warn("Order [{}] has already been resolved/redrived at {}", orderId, audit.getRedrivedAt());
            throw new IllegalArgumentException("Order [" + orderId + "] has already been resolved and redrived on " + audit.getRedrivedAt());
        }

        int quantityToSend = (correctedQuantity != null && correctedQuantity > 0) ? correctedQuantity : 1;
        
        OrderRequest correctedOrder = OrderRequest.builder()
                .orderId(audit.getOrderId())
                .productId(audit.getProductId())
                .quantity(quantityToSend)
                .build();

        log.info("Redriving corrected order [{}] with quantity={} to main queue", orderId, quantityToSend);
        sqsTemplate.send(to -> to.queue(mainQueueName).payload(correctedOrder));

        audit.setStatus("RESOLVED_AND_REDRIVED");
        audit.setQuantity(quantityToSend);
        audit.setRedrivedAt(LocalDateTime.now());

        return true;
    }

}
