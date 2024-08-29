package antifraud;

public class SuspiciousIpRequest {

    private String ip;

    public SuspiciousIpRequest() {
    }

    public SuspiciousIpRequest(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
