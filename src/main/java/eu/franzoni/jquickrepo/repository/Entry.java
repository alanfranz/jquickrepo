package eu.franzoni.jquickrepo.repository;

public class Entry<T> {
    private final T content;
    private final String id;

    public Entry(String id, T content) {
        this.content = content;
        this.id = id;
    }

    public T getContent() {
        return content;
    }

    public String getId() {
        return id;
    }
    
    
    

}
