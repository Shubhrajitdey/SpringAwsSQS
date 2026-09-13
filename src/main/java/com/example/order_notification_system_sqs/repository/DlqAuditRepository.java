package com.example.order_notification_system_sqs.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.example.order_notification_system_sqs.model.DlqAuditLog;

@Repository
public class DlqAuditRepository {

    private final Map<String, DlqAuditLog> auditLogMap = new ConcurrentHashMap<>();

    public void save(DlqAuditLog log) {
        auditLogMap.put(log.getOrderId(), log);
    }

    public List<DlqAuditLog> findAll() {
        return new ArrayList<>(auditLogMap.values());
    }

    public Optional<DlqAuditLog> findById(String orderId) {
        return Optional.ofNullable(auditLogMap.get(orderId));
    }
}
