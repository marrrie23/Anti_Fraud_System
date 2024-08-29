package antifraud;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    Optional<User> findByUsernameIgnoreCase(String username);
}

/* UserRepository:
   This interface is used to interact with the database for CRUD operations on the User entity.
 */
