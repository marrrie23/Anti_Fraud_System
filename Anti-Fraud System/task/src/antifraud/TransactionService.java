package antifraud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public List<Transaction> getTransactionsByNumberAndDate(String number, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByNumberAndDateBetween(number, startDate, endDate);
    }
}
