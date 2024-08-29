package antifraud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/antifraud/stolencard")
public class StolenCardController {

    @Autowired
    private StolenCardService stolenCardService;

    @PostMapping
    public ResponseEntity<?> addStolenCard(@RequestBody Map<String, String> request) {
        String number = request.get("number");

        if (!isValidCardNumber(number)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid card number format"));
        }

        try {
            StolenCard savedCard = stolenCardService.saveStolenCard(number);
            // Returning status 200 OK instead of 201 CREATED
            return ResponseEntity.ok(savedCard);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{number}")
    public ResponseEntity<?> deleteStolenCard(@PathVariable String number) {
        if (!isValidCardNumber(number)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid card number format"));
        }

        try {
            stolenCardService.deleteStolenCard(number);
            return ResponseEntity.ok(Map.of("status", "Card " + number + " successfully removed!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<StolenCard>> getAllStolenCards() {
        List<StolenCard> cards = stolenCardService.findAll();
        return ResponseEntity.ok(cards);
    }

    private boolean isValidCardNumber(String number) {
        // Implement Luhn algorithm here
        int nDigits = number.length();
        int nSum = 0;
        boolean isSecond = false;

        for (int i = nDigits - 1; i >= 0; i--) {
            int d = number.charAt(i) - '0';
            if (isSecond) {
                d *= 2;
            }
            nSum += d / 10;
            nSum += d % 10;
            isSecond = !isSecond;
        }
        return (nSum % 10 == 0);
    }
}
