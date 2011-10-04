package eu.franzoni.jquickrepo.repository;

public class DuplicateResourceIdException extends RuntimeException {
    public DuplicateResourceIdException(String id) {
        super("Resource with id '" + id + "' has already been saved.");
    }

    public DuplicateResourceIdException(String id, Throwable cause) {
        super("Resource with id '" + id + "' has already been saved.", cause);
    }
}
