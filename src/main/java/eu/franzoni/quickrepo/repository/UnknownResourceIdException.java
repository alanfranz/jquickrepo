package eu.franzoni.quickrepo.repository;

public class UnknownResourceIdException extends RuntimeException{
    public UnknownResourceIdException(String id) {
        super("Could not locate resource with id: " + id);
    }

    public UnknownResourceIdException(String id, Throwable cause) {
        super("Could not locate resource with id: " + id, cause);
    }

}
