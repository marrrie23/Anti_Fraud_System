// StolenCardService.java
package antifraud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StolenCardService {

    @Autowired
    private StolenCardRepository stolenCardRepository;

    @Transactional
    public StolenCard saveStolenCard(String number) {
        if (stolenCardRepository.existsByNumber(number)) {
            throw new IllegalArgumentException("Card number already exists");
        }
        StolenCard card = new StolenCard();
        card.setNumber(number);
        return stolenCardRepository.save(card);
    }

    @Transactional
    public void deleteStolenCard(String number) {
        StolenCard card = stolenCardRepository.findByNumber(number)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));
        stolenCardRepository.delete(card);
    }

    public List<StolenCard> findAll() {
        return stolenCardRepository.findAll();
    }
}