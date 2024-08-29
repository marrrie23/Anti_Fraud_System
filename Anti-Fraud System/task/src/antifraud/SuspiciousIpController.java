package antifraud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/antifraud/suspicious-ip")
public class SuspiciousIpController {

    @Autowired
    private SuspiciousIpService suspiciousIpService;

    @PostMapping
    public ResponseEntity<?> addSuspiciousIp(@RequestBody Map<String, String> request) {
        String ip = request.get("ip");

        if (!isValidIp(ip)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid IP format"));
        }

        try {
            SuspiciousIp savedIp = suspiciousIpService.saveSuspiciousIp(ip);
            // Returning status 200 OK with the saved IP object
            return ResponseEntity.ok(savedIp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{ip}")
    public ResponseEntity<?> deleteSuspiciousIp(@PathVariable String ip) {
        if (!isValidIp(ip)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid IP format"));
        }

        try {
            suspiciousIpService.deleteSuspiciousIp(ip);
            return ResponseEntity.ok(Map.of("status", "IP " + ip + " successfully removed!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<SuspiciousIp>> getAllSuspiciousIps() {
        List<SuspiciousIp> ips = suspiciousIpService.findAll();
        return ResponseEntity.ok(ips);
    }

    private boolean isValidIp(String ip) {
        String ipRegex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ip.matches(ipRegex);
    }
}
