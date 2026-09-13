package com.example.order_notification_system_sqs.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.order_notification_system_sqs.model.DlqAuditLog;
import com.example.order_notification_system_sqs.service.DlqReDriveService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/admin/dlq")
@Slf4j
public class AdminController {

    private final DlqReDriveService dlqReDriveService;

    public AdminController(DlqReDriveService dlqReDriveService) {
        this.dlqReDriveService = dlqReDriveService;
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<DlqAuditLog>> getAuditLogs() {
        return ResponseEntity.ok(dlqReDriveService.getAllAuditLogs());
    }

    @PostMapping("/redrive")
    public ResponseEntity<String> redriveDlq(@RequestParam(defaultValue = "5") int maxMessages) {
        log.info("Received request to redrive {} messages from DLQ", maxMessages);
        int movedCount = dlqReDriveService.redriveMessages(maxMessages);
        String result = String.format("Successfully redrived %d messages from DLQ", movedCount);
        log.info(result);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/redrive/{orderId}")
    public ResponseEntity<String> redriveSpecificOrder(
            @PathVariable String orderId,
            @RequestParam(required = false) Integer correctedQuantity) {
        dlqReDriveService.redriveSpecificOrder(orderId, correctedQuantity);
        return ResponseEntity.ok("Successfully corrected and redrived order [" + orderId + "] to main queue");
    }

}
