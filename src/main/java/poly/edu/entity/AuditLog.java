package poly.edu.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * AuditLog - Entity for tracking admin actions.
 * 
 * Rubber Duck Explanation:
 * -------------------------
 * "Why add audit logging?"
 * 
 * For enterprise applications, audit trail is essential for:
 * 1. Security compliance (who did what, when)
 * 2. Debugging (trace issues to specific actions)
 * 3. Undo operations (see previous state)
 * 4. Legal requirements (data modification proof)
 * 
 * "What gets logged?"
 * 
 * - CREATE: New entity created (product, category, etc.)
 * - UPDATE: Entity modified
 * - DELETE: Entity deleted (soft or hard)
 * - LOCK: Account locked
 * - UNLOCK: Account unlocked
 * - RESET_PASSWORD: Password reset
 * 
 * "Why store details as String (JSON)?"
 * 
 * - Flexible: Can log any entity type
 * - Searchable: Can query JSON in SQL Server
 * - Human readable: Easy to display in admin UI
 * - No schema changes: Add new fields without migration
 * 
 * Space Complexity: O(1) per audit entry (fixed size fields)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "AuditLogs", indexes = {
        @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
        @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_audit_admin", columnList = "admin_id")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Admin who performed the action.
     * Can be null if action was performed by system.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Account admin;

    /**
     * Type of action performed.
     * Values: CREATE, UPDATE, DELETE, LOCK, UNLOCK, RESET_PASSWORD, etc.
     */
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    /**
     * Type of entity affected.
     * Values: Product, Category, Order, Account, etc.
     */
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    /**
     * ID of the affected entity.
     */
    @Column(name = "entity_id")
    private Integer entityId;

    /**
     * Human-readable description or JSON of changes.
     * Example: {"field": "price", "old": 100, "new": 150}
     */
    @Column(name = "details", length = 2000)
    private String details;

    /**
     * When the action was performed.
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * IP address of the admin (for security).
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @PrePersist
    public void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    // Builder pattern for convenient creation
    public static AuditLogBuilder builder() {
        return new AuditLogBuilder();
    }

    public static class AuditLogBuilder {
        private Account admin;
        private String action;
        private String entityType;
        private Integer entityId;
        private String details;
        private String ipAddress;

        public AuditLogBuilder admin(Account admin) {
            this.admin = admin;
            return this;
        }

        public AuditLogBuilder action(String action) {
            this.action = action;
            return this;
        }

        public AuditLogBuilder entityType(String entityType) {
            this.entityType = entityType;
            return this;
        }

        public AuditLogBuilder entityId(Integer entityId) {
            this.entityId = entityId;
            return this;
        }

        public AuditLogBuilder details(String details) {
            this.details = details;
            return this;
        }

        public AuditLogBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public AuditLog build() {
            AuditLog log = new AuditLog();
            log.setAdmin(admin);
            log.setAction(action);
            log.setEntityType(entityType);
            log.setEntityId(entityId);
            log.setDetails(details);
            log.setIpAddress(ipAddress);
            log.setTimestamp(LocalDateTime.now());
            return log;
        }
    }
}
