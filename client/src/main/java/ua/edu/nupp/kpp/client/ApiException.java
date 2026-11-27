package ua.edu.nupp.kpp.client;

/** Помилка взаємодії із сервером: status = 0 означає, що сервер недоступний. */
public class ApiException extends RuntimeException {
    private final int status;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
