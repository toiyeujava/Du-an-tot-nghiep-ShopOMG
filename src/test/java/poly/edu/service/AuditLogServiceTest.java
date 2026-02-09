package poly.edu.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import poly.edu.entity.Account;
import poly.edu.entity.AuditLog;
import poly.edu.repository.AuditLogRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuditLogServiceTest - Unit tests for audit logging.
 * 
 * Test Coverage:
 * - Logging operations
 * - Query operations
 * - Statistics calculation
 */
@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    private Account testAdmin;
    private AuditLog testLog;

    @BeforeEach
    void setUp() {
        testAdmin = new Account();
        testAdmin.setId(1);
        testAdmin.setUsername("admin");

        testLog = new AuditLog();
        testLog.setId(1);
        testLog.setAdmin(testAdmin);
        testLog.setAction("CREATE");
        testLog.setEntityType("Product");
        testLog.setEntityId(100);
        testLog.setDetails("Created product: Basic T-Shirt");
        testLog.setTimestamp(LocalDateTime.now());
    }

    // ==================== log ====================

    @Nested
    @DisplayName("log")
    class Log {

        @Test
        @DisplayName("Should save audit log entry")
        void log_validData_savesEntry() {
            // Arrange
            when(auditLogRepository.save(any(AuditLog.class)))
                    .thenAnswer(invocation -> {
                        AuditLog saved = invocation.getArgument(0);
                        saved.setId(1);
                        return saved;
                    });

            // Act
            auditLogService.log(testAdmin, "CREATE", "Product", 100, "Test details");

            // Assert
            verify(auditLogRepository, times(1)).save(argThat(log -> log.getAction().equals("CREATE") &&
                    log.getEntityType().equals("Product") &&
                    log.getEntityId().equals(100) &&
                    log.getAdmin().equals(testAdmin)));
        }

        @Test
        @DisplayName("Should save log with IP address")
        void log_withIpAddress_savesWithIp() {
            // Arrange
            when(auditLogRepository.save(any(AuditLog.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            auditLogService.log(testAdmin, "UPDATE", "Order", 50, "Updated order", "192.168.1.1");

            // Assert
            verify(auditLogRepository).save(argThat(log -> log.getIpAddress().equals("192.168.1.1")));
        }

        @Test
        @DisplayName("Should allow null admin for system actions")
        void log_nullAdmin_savesEntry() {
            // Arrange
            when(auditLogRepository.save(any(AuditLog.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            auditLogService.log(null, "SYSTEM", "Config", 1, "System startup");

            // Assert
            verify(auditLogRepository).save(argThat(log -> log.getAdmin() == null));
        }
    }

    // ==================== getLogsByEntity ====================

    @Nested
    @DisplayName("getLogsByEntity")
    class GetLogsByEntity {

        @Test
        @DisplayName("Should return logs for specific entity")
        void getLogsByEntity_existingEntity_returnsLogs() {
            // Arrange
            AuditLog log2 = new AuditLog();
            log2.setId(2);
            log2.setAction("UPDATE");
            log2.setEntityType("Product");
            log2.setEntityId(100);

            when(auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc("Product", 100))
                    .thenReturn(Arrays.asList(testLog, log2));

            // Act
            List<AuditLog> result = auditLogService.getLogsByEntity("Product", 100);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).extracting("action").containsExactly("CREATE", "UPDATE");
        }
    }

    // ==================== getActionStatistics ====================

    @Nested
    @DisplayName("getActionStatistics")
    class GetActionStatistics {

        @Test
        @DisplayName("Should return action counts")
        void getActionStatistics_existingLogs_returnsCounts() {
            // Arrange
            when(auditLogRepository.countActionsSince(any(LocalDateTime.class)))
                    .thenReturn(Arrays.asList(
                            new Object[] { "CREATE", 10L },
                            new Object[] { "UPDATE", 25L },
                            new Object[] { "DELETE", 5L }));

            // Act
            Map<String, Long> result = auditLogService.getActionStatistics(7);

            // Assert
            assertThat(result).containsEntry("CREATE", 10L);
            assertThat(result).containsEntry("UPDATE", 25L);
            assertThat(result).containsEntry("DELETE", 5L);
        }
    }

    // ==================== getRecentLogsForDashboard ====================

    @Nested
    @DisplayName("getRecentLogsForDashboard")
    class GetRecentLogsForDashboard {

        @Test
        @DisplayName("Should return top 20 recent logs")
        void getRecentLogsForDashboard_callsRepository() {
            // Arrange
            when(auditLogRepository.findTop20ByOrderByTimestampDesc())
                    .thenReturn(List.of(testLog));

            // Act
            List<AuditLog> result = auditLogService.getRecentLogsForDashboard();

            // Assert
            assertThat(result).hasSize(1);
            verify(auditLogRepository, times(1)).findTop20ByOrderByTimestampDesc();
        }
    }
}
