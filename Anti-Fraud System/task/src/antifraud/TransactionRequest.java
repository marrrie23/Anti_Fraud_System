package antifraud;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;


/* The TransactionRequest class is used to capture and pass information
   about a transaction to your application.
*/

public class TransactionRequest {

    private Long amount;
    private String ip;
    private String number;
    private String region;
    private LocalDateTime date;

    // Getters and Setters
    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}



/* The TransactionRequest class is used to capture and pass information
   about a transaction to your application.
*/
