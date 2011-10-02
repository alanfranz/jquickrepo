package eu.franzoni.quickrepo.repository;


public class MissingIdException extends RuntimeException {
    public MissingIdException(String id) {
        super("Resource with id '" + id + "' does not exist in repository.");
    }

    public MissingIdException(String id, Throwable cause) {
        super("Resource with id '" + id + "' does not exist in repository.", cause);
    }
}
