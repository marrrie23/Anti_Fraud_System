// SuspiciousIpService.java
package antifraud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SuspiciousIpService {

    @Autowired
    private SuspiciousIpRepository suspiciousIpRepository;

    @Transactional
    public SuspiciousIp saveSuspiciousIp(String ip) {
        if (suspiciousIpRepository.existsByIp(ip)) {
            throw new IllegalArgumentException("IP already exists");
        }
        SuspiciousIp ipEntity = new SuspiciousIp();
        ipEntity.setIp(ip);
        return suspiciousIpRepository.save(ipEntity);
    }

    @Transactional
    public void deleteSuspiciousIp(String ip) {
        SuspiciousIp ipEntity = suspiciousIpRepository.findByIp(ip)
                .orElseThrow(() -> new IllegalArgumentException("IP not found"));
        suspiciousIpRepository.delete(ipEntity);
    }

    public List<SuspiciousIp> findAll() {
        return suspiciousIpRepository.findAll();
    }
}