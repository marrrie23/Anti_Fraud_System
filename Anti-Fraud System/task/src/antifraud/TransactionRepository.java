package antifraud;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByNumberAndDateBetween(String number, LocalDateTime start, LocalDateTime end);

    // Add this method to find transactions by card number
    List<Transaction> findByNumber(String number);
}
