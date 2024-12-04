package cz.trinera.dkt;

public class ToolAvailabilityError extends Exception {
    public ToolAvailabilityError(String message) {
        super(message);
    }

    public ToolAvailabilityError(String message, Throwable cause) {
        super(message, cause);
    }
}
