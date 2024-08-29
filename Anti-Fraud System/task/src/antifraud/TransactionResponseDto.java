package antifraud;

public class TransactionResponseDto {
    private Long id;
    private String result;
    private String info;

    public TransactionResponseDto(Long id, String result, String info) {
        this.id = id;
        this.result = result;
        this.info = info;
    }

    public Long getId() {
        return id;
    }

    public String getResult() {
        return result;
    }

    public String getInfo() {
        return info;
    }
}
