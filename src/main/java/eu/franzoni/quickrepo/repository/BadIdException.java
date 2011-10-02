package eu.franzoni.quickrepo.repository;

public class BadIdException extends RuntimeException {
    public BadIdException(String id, String message) {
        super("Bad id '" + id + "' (" + message + ")");
    }

    public BadIdException(String id, String message, Throwable cause) {
        super("Bad id '" + id + "' (" + message + ")", cause);
    }
}
