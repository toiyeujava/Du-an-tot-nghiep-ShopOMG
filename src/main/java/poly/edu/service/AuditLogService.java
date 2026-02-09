package poly.edu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.Account;
import poly.edu.entity.AuditLog;
import poly.edu.repository.AuditLogRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AuditLogService - Service for audit logging operations.
 * 
 * Rubber Duck Explanation:
 * -------------------------
 * "How does audit logging work?"
 * 
 * 1. Admin performs action (create, update, delete, etc.)
 * 2. Controller calls AuditLogService.log()
 * 3. Log entry is created with:
 * - who (admin)
 * - what (action, entity type, entity ID)
 * - when (timestamp)
 * - details (JSON of changes)
 * 4. Log is saved asynchronously (doesn't block main operation)
 * 
 * "Why not use AOP for automatic logging?"
 * 
 * For this project, explicit logging is preferred because:
 * - More control over what gets logged
 * - Can customize details per action
 * - Easier to understand and maintain
 * - AOP adds complexity
 * 
 * Future improvement: Use @AuditLog annotation for automatic logging
 * 
 * Time Complexity:
 * - log(): O(1) - single insert
 * - getRecentLogs(): O(n) - limited by pagination
 * - getLogsByEntity(): O(n) - limited by result size
 * 
 * Space Complexity: O(1) per operation
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Log an admin action.
     * 
     * @param admin      Admin performing action (null for system)
     * @param action     Action type (CREATE, UPDATE, DELETE, etc.)
     * @param entityType Entity type (Product, Order, Account, etc.)
     * @param entityId   Entity ID
     * @param details    Human-readable description or JSON
     */
    @Transactional
    public void log(Account admin, String action, String entityType, Integer entityId, String details) {
        AuditLog auditLog = AuditLog.builder()
                .admin(admin)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .build();

        auditLogRepository.save(auditLog);
    }

    /**
     * Log with IP address.
     */
    @Transactional
    public void log(Account admin, String action, String entityType, Integer entityId,
            String details, String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
                .admin(admin)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .ipAddress(ipAddress)
                .build();

        auditLogRepository.save(auditLog);
    }

    /**
     * Get recent audit logs with pagination.
     */
    public Page<AuditLog> getRecentLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    /**
     * Get logs for a specific entity.
     * Useful for viewing entity history.
     */
    public List<AuditLog> getLogsByEntity(String entityType, Integer entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }

    /**
     * Get logs by admin.
     */
    public Page<AuditLog> getLogsByAdmin(Integer adminId, Pageable pageable) {
        return auditLogRepository.findByAdminIdOrderByTimestampDesc(adminId, pageable);
    }

    /**
     * Get recent logs (top 20 for dashboard).
     */
    public List<AuditLog> getRecentLogsForDashboard() {
        return auditLogRepository.findTop20ByOrderByTimestampDesc();
    }

    /**
     * Get action statistics for last N days.
     */
    public Map<String, Long> getActionStatistics(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Object[]> results = auditLogRepository.countActionsSince(since);

        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]));
    }

    /**
     * Get logs in date range.
     */
    public List<AuditLog> getLogsInDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByDateRange(start, end);
    }
}
