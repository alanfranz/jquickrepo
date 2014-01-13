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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entry)) return false;

        Entry entry = (Entry) o;

        if (!content.equals(entry.content)) return false;
        if (!id.equals(entry.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = content.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }
}
