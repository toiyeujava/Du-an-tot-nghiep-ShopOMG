package poly.edu.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String name);
}
