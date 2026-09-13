package com.example.order_notification_system_sqs.consumer;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.example.order_notification_system_sqs.dto.OrderRequest;
import com.example.order_notification_system_sqs.model.DlqAuditLog;
import com.example.order_notification_system_sqs.repository.DlqAuditRepository;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DlqConsumer {

    private final DlqAuditRepository auditRepository;

    public DlqConsumer(DlqAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @SqsListener("${app.queue.order-events-dlq}")
    public void listenDlq(OrderRequest order) {
        log.warn("Captured failed message from DLQ: orderId={}, quantity={}", 
                order.getOrderId(), order.getQuantity());

        DlqAuditLog auditLog = DlqAuditLog.builder()
                .orderId(order.getOrderId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .failureReason("Quantity is invalid (" + order.getQuantity() + ") - Failed 3 retries in main queue")
                .status("FAILED_IN_DLQ")
                .failedAt(LocalDateTime.now())
                .build();

        auditRepository.save(auditLog);
        log.info("Audited failed order [{}] in DlqAuditRepository", order.getOrderId());
    }
}
