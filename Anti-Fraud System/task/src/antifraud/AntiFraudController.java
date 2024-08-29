package antifraud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import org.springframework.web.server.ResponseStatusException;

/* The AntiFraudController class handles requests to check transactions for possible fraud based
   on the transaction amount. Depending on the amount, the transaction is allowed,
   requires manual review, or is prohibited. The result of this check is then
   returned to the client that made the request.
*/

@RestController
public class AntiFraudController {

    @Autowired
    private SuspiciousIpService suspiciousIpService;

    @Autowired
    private StolenCardService stolenCardService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionService transactionService;

    private static final Set<String> VALID_REGIONS = Set.of("EAP", "ECA", "HIC", "LAC", "MENA", "SA", "SSA");

    private static long maxAllowedAmount = 200L;
    private static long maxManualProcessingAmount = 1500L;

    @PostMapping("/api/antifraud/transaction")
    public ResponseEntity<Map<String, String>> checkTransaction(@RequestBody TransactionRequestDto request) {
        // Validate inputs
        if (request.getAmount() == null || request.getAmount() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Amount must be greater than 0"));
        }

        if (!isValidIp(request.getIp())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid IP format"));
        }

        if (!isValidCardNumber(request.getNumber())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid card number format"));
        }

        if (!VALID_REGIONS.contains(request.getRegion())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid region"));
        }

        if (request.getDate() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid date"));
        }

        // Check if the IP or card number is suspicious or stolen
        boolean isIpSuspicious = suspiciousIpService.findAll().stream()
                .anyMatch(ip -> ip.getIp().equals(request.getIp()));
        boolean isCardStolen = stolenCardService.findAll().stream()
                .anyMatch(card -> card.getNumber().equals(request.getNumber()));

        // Check correlation rules
        LocalDateTime oneHourBefore = request.getDate().minusHours(1);
        List<Transaction> recentTransactions = transactionService.getTransactionsByNumberAndDate(
                request.getNumber(), oneHourBefore, request.getDate()
        );

        long uniqueRegions = recentTransactions.stream()
                .map(Transaction::getRegion)
                .filter(region -> !region.equals(request.getRegion()))
                .distinct()
                .count();

        long uniqueIps = recentTransactions.stream()
                .map(Transaction::getIp)
                .filter(ip -> !ip.equals(request.getIp()))
                .distinct()
                .count();

        // Determine the result based on the checks
        String result = null;
        List<String> reasons = new ArrayList<>();

        if (isIpSuspicious) {
            result = "PROHIBITED";
            reasons.add("ip");
        }

        if (isCardStolen) {
            if (result == null) {
                result = "PROHIBITED";
            }
            reasons.add("card-number");
        }

        if (request.getAmount() > maxManualProcessingAmount) {
            if (result == null) {
                result = "PROHIBITED";
            }
            reasons.add("amount");
        } else if (request.getAmount() > maxAllowedAmount) {
            if (result == null) {
                result = "MANUAL_PROCESSING";
                reasons.add("amount");
            } else if (result.equals("ALLOWED")) {
                result = "MANUAL_PROCESSING";
                reasons.clear();
                reasons.add("amount");
            }
        }

        if (uniqueRegions > 2) {
            if (result == null || result.equals("ALLOWED")) {
                result = "PROHIBITED";
            }
            reasons.add("region-correlation");
        } else if (uniqueRegions == 2) {
            if (result == null || result.equals("ALLOWED")) {
                result = "MANUAL_PROCESSING";
            }
            reasons.add("region-correlation");
        }

        if (uniqueIps > 2) {
            if (result == null || result.equals("ALLOWED")) {
                result = "PROHIBITED";
            }
            reasons.add("ip-correlation");
        } else if (uniqueIps == 2) {
            if (result == null || result.equals("ALLOWED")) {
                result = "MANUAL_PROCESSING";
            }
            reasons.add("ip-correlation");
        }

        if (result == null) {
            result = "ALLOWED";
            reasons.add("none");
        }

        // Sort the reasons alphabetically
        Collections.sort(reasons);

        // Save the transaction
        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setIp(request.getIp());
        transaction.setNumber(request.getNumber());
        transaction.setRegion(request.getRegion());
        transaction.setDate(request.getDate());
        transaction.setResult(result);
        transactionRepository.save(transaction);

        // Create and return response with result and info
        Map<String, String> response = new HashMap<>();
        response.put("result", result);
        response.put("info", String.join(", ", reasons));

        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/antifraud/transaction")
    public ResponseEntity<?> provideFeedback(@RequestBody Map<String, String> request) {
        Long transactionId = Long.parseLong(request.get("transactionId"));
        String feedback = request.get("feedback");

        // Validate feedback
        if (!Set.of("ALLOWED", "MANUAL_PROCESSING", "PROHIBITED").contains(feedback)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid feedback"));
        }

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        if (!transaction.getFeedback().isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Feedback already provided"));
        }

        String result = transaction.getResult();

        if (feedback.equals(result)) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("error", "Invalid feedback transition"));
        }

        transaction.setFeedback(feedback);
        transactionRepository.save(transaction);

        updateLimits(transaction.getAmount(), result, feedback);

        return ResponseEntity.ok(Map.of("transactionId", transaction.getId(), "amount", transaction.getAmount(), "ip",
                transaction.getIp(), "number", transaction.getNumber(), "region", transaction.getRegion(), "date", transaction.getDate(),
                "result", transaction.getResult(), "feedback", transaction.getFeedback()));
    }

    private void updateLimits(Long amount, String result, String feedback) {
        if ("ALLOWED".equals(feedback)) {
            if ("MANUAL_PROCESSING".equals(result)) {
                maxAllowedAmount = (long) Math.ceil(0.8 * maxAllowedAmount + 0.2 * amount);
            } else if ("PROHIBITED".equals(result)) {
                maxAllowedAmount = (long) Math.ceil(0.8 * maxAllowedAmount + 0.2 * amount);
                maxManualProcessingAmount = (long) Math.ceil(0.8 * maxManualProcessingAmount + 0.2 * amount);
            }
        } else if ("MANUAL_PROCESSING".equals(feedback)) {
            if ("ALLOWED".equals(result)) {
                maxAllowedAmount = (long) Math.ceil(0.8 * maxAllowedAmount - 0.2 * amount);
            } else if ("PROHIBITED".equals(result)) {
                maxManualProcessingAmount = (long) Math.ceil(0.8 * maxManualProcessingAmount + 0.2 * amount);
            }
        } else if ("PROHIBITED".equals(feedback)) {
            if ("ALLOWED".equals(result)) {
                maxAllowedAmount = (long) Math.ceil(0.8 * maxAllowedAmount - 0.2 * amount);
                maxManualProcessingAmount = (long) Math.ceil(0.8 * maxManualProcessingAmount - 0.2 * amount);
            } else if ("MANUAL_PROCESSING".equals(result)) {
                maxManualProcessingAmount = (long) Math.ceil(0.8 * maxManualProcessingAmount - 0.2 * amount);
            }
        }
    }

    private boolean isValidIp(String ip) {
        String ipRegex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ip.matches(ipRegex);
    }

    private boolean isValidCardNumber(String number) {
        // Implement Luhn algorithm here

        int nDigits = number.length();
        int nSum = 0;
        boolean isSecond = false;

        for (int i = nDigits - 1; i >= 0; i--) {
            int d = number.charAt(i) - '0';

            if (isSecond) d = d * 2;

            nSum += d / 10;
            nSum += d % 10;

            isSecond = !isSecond;
        }
        return (nSum % 10 == 0);
    }

    @GetMapping("/api/antifraud/history")
    public ResponseEntity<?> getTransactionHistory() {
        List<Transaction> transactions = transactionRepository.findAll();
        transactions.sort(Comparator.comparing(Transaction::getId));

        List<Map<String, Object>> response = new ArrayList<>();
        for (Transaction transaction : transactions) {
            response.add(Map.of(
                    "transactionId", transaction.getId(),
                    "amount", transaction.getAmount(),
                    "number", transaction.getNumber(),
                    "ip", transaction.getIp(),
                    "region", transaction.getRegion(),
                    "date", transaction.getDate(),
                    "result", transaction.getResult(),
                    "feedback", transaction.getFeedback()
            ));
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/antifraud/history/{number}")
    public ResponseEntity<?> getTransactionHistoryByNumber(@PathVariable String number) {
        if (!isValidCardNumber(number)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid card number format"));
        }

        List<Transaction> transactions = transactionRepository.findByNumber(number);
        if (transactions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No transactions found for this card number"));
        }

        transactions.sort(Comparator.comparing(Transaction::getId));

        List<Map<String, Object>> response = new ArrayList<>();
        for (Transaction transaction : transactions) {
            response.add(Map.of(
                    "transactionId", transaction.getId(),
                    "amount", transaction.getAmount(),
                    "number", transaction.getNumber(),
                    "ip", transaction.getIp(),
                    "region", transaction.getRegion(),
                    "date", transaction.getDate(),
                    "result", transaction.getResult(),
                    "feedback", transaction.getFeedback()
            ));
        }
        return ResponseEntity.ok(response);
    }
}
