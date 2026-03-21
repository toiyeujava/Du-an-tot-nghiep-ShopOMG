package poly.edu.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.entity.Notification;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    
    List<Notification> findByAccountIdOrderByCreatedAtDesc(Integer accountId, Pageable pageable);
    
    Long countByAccountIdAndIsReadFalse(Integer accountId);
    
    List<Notification> findByAccountIdAndIsReadFalse(Integer accountId);
}
