package poly.edu.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AuditLogRepository - Repository for audit log operations.
 * 
 * Optimized queries for:
 * - Recent logs viewing (dashboard)
 * - Entity history lookup
 * - Admin activity tracking
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {

    /**
     * Find logs by entity type and ID.
     * Useful for viewing history of a specific entity.
     */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(
            String entityType, Integer entityId);

    /**
     * Find logs by admin.
     * Useful for tracking admin activities.
     */
    Page<AuditLog> findByAdminIdOrderByTimestampDesc(Integer adminId, Pageable pageable);

    /**
     * Find recent logs.
     * Used for admin dashboard.
     */
    List<AuditLog> findTop20ByOrderByTimestampDesc();

    /**
     * Find logs in date range.
     * Useful for reports.
     */
    @Query("SELECT al FROM AuditLog al WHERE al.timestamp BETWEEN :start AND :end ORDER BY al.timestamp DESC")
    List<AuditLog> findByDateRange(@Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Count actions by type in last N days.
     * Useful for activity statistics.
     */
    @Query("SELECT al.action, COUNT(al) FROM AuditLog al " +
            "WHERE al.timestamp > :since GROUP BY al.action")
    List<Object[]> countActionsSince(@Param("since") LocalDateTime since);
}
