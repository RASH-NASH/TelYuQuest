package arcana.model.exception;

public class NotEnoughGoldException extends Exception {
    public NotEnoughGoldException(String message) {
        super(message);
    }

    public NotEnoughGoldException() {
        super("Gold tidak cukup!");
    }
}
