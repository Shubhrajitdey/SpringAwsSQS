package com.example.order_notification_system_sqs.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DlqAuditLog {
    private String orderId;
    private String productId;
    private Integer quantity;
    private String failureReason;
    private String status; // e.g. "FAILED_IN_DLQ", "REDRIVED", "RESOLVED"
    private LocalDateTime failedAt;
    private LocalDateTime redrivedAt;
}
