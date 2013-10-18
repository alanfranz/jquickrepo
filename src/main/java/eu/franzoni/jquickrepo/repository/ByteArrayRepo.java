package eu.franzoni.jquickrepo.repository;

import com.google.common.io.Files;
import eu.franzoni.jquickrepo.concurrency.ScopedReadWriteLock;
import eu.franzoni.jquickrepo.concurrency.MultipleResourceLock;
import eu.franzoni.jquickrepo.concurrency.WhileLocked;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ByteArrayRepo {

    private byte[] doLoad(final String id) {
        // not required but suggested, as it prevents traversals.
        validateId(id);

        ScopedReadWriteLock<byte[]> scopedLock = new ScopedReadWriteLock<byte[]>(this.lockProvider.provideLock(id));

        return scopedLock.executeWithReadLock(new WhileLocked<byte[]>() {
            @Override
            public byte[] execute() {
                return getContents(id);
            }
        });
    }

    public static class Item {

        private final String id;

        public String getId() {
            return id;
        }

        public byte[] getData() {
            return data;
        }
        private final byte[] data;

        public Item(String id, byte[] data) {
            this.id = id;
            this.data = data;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 29 * hash + (this.id != null ? this.id.hashCode() : 0);
            hash = 29 * hash + Arrays.hashCode(this.data);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Item other = (Item) obj;
            if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
                return false;
            }
            if (!Arrays.equals(this.data, other.data)) {
                return false;
            }
            return true;
        }
        
        
    }

    public static class ItemIterator implements Iterator<Item> {

        private final Iterator<File> filesIterator;
        private final ByteArrayRepo filesRepo;

        public ItemIterator(Iterator<File> filesIterator, ByteArrayRepo repo) {
            this.filesIterator = filesIterator;
            this.filesRepo = repo;
        }

        @Override
        public boolean hasNext() {
            return this.filesIterator.hasNext();
        }

        @Override
        public Item next() {
            final String id = this.filesIterator.next().getName();
            return new Item(id, this.filesRepo.doLoad(id));
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Removal is unsupported.");
        }
    }
    final private File persistenceDir;
    final private MultipleResourceLock lockProvider = new MultipleResourceLock();

    public ByteArrayRepo(File persistenceDir) {
        validatePersistenceDir(persistenceDir);
        this.persistenceDir = persistenceDir;
    }

    private void validatePersistenceDir(File persistenceDir) {
        if (!persistenceDir.isDirectory()) {
            throw new IllegalArgumentException("persistence dir must be a directory");
        }

        if (!persistenceDir.canWrite()) {
            throw new IllegalArgumentException("persistence dir must be writeable");

        }

        if (!persistenceDir.canRead()) {
            throw new IllegalArgumentException("persistence dir must be readable");
        }
    }

    public void save(final String id, final byte[] data) {
        validateId(id);

        ScopedReadWriteLock<Void> scopedLock = new ScopedReadWriteLock<Void>(lockProvider.provideLock(id));
        scopedLock.executeWithWriteLock(new WhileLocked<Void>() {
            @Override
            public Void execute() {
                verifyResourceDoesNotExist(id);
                persistData(id, data);
                return null;
            }
        });


    }

    private void verifyResourceDoesNotExist(String id) {
        if (new File(this.persistenceDir, id).exists()) {
            throw new DuplicateResourceIdException(id);
        }
    }

    private void verifyResourceExists(String id) {
        if (!new File(this.persistenceDir, id).exists()) {
            throw new MissingIdException(id);
        }
    }

    public void update(final String id, final byte[] data) {
        validateId(id);

        ScopedReadWriteLock<Void> scopedLock = new ScopedReadWriteLock<Void>(lockProvider.provideLock(id));
        scopedLock.executeWithWriteLock(new WhileLocked<Void>() {
            @Override
            public Void execute() {
                verifyResourceExists(id);
                persistData(id, data);
                return null;
            }
        });

    }

    public void saveOrUpdate(final String id, final byte[] data) {
        validateId(id);

        ScopedReadWriteLock<Void> scopedLock = new ScopedReadWriteLock<Void>(lockProvider.provideLock(id));
        scopedLock.executeWithWriteLock(new WhileLocked<Void>() {
            @Override
            public Void execute() {
                persistData(id, data);
                return null;
            }
        });
    }

    private void persistData(String id, byte[] data) {
        File datafile = createTemporaryDataFile(id);
        writeDataToTemporaryDataFile(data, datafile);
        renameDataFileToFinalName(id, datafile);
    }

    private void validateId(String id) {
        if (!id.matches("^[a-zA-Z0-9_\\-]+$")) {
            throw new BadIdException(id, "contains unsupported characters");
        }

    }

    private void renameDataFileToFinalName(String id, File datafile) {
        final File finalFile = new File(this.persistenceDir, id);
        boolean succeeded = datafile.renameTo(finalFile);

        if (!succeeded) {
            boolean existingOld = datafile.exists();
            boolean existingNew = finalFile.exists();

            throw new RuntimeException(String.format("could not rename (exists: %s) %s to (exists: %s) %s", existingOld, datafile.getAbsolutePath(), existingNew, finalFile.getAbsolutePath()));
        }
    }

    private void writeDataToTemporaryDataFile(byte[] data, File datafile) {
        try {
            FileOutputStream fos = new FileOutputStream(datafile);
            fos.write(data);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File createTemporaryDataFile(String id) {
        File datafile = new File(this.persistenceDir, id + ".part");

        if (datafile.exists()) {
            throw new RuntimeException(String.format("Part file exists already: %s", datafile.getAbsolutePath()));
        }

        // this is required only in order to check whether the name is too long.
        try {
            boolean created = datafile.createNewFile();
            if (datafile.exists() && !created) {
                throw new RuntimeException(String.format("Part file exists already but was not created: %s", datafile.getAbsolutePath()));
            }
            if (!created) {
                throw new RuntimeException(String.format("Could not create %s", datafile.getAbsolutePath()));
            }
        } catch (IOException e) {
            throw new BadIdException(id, "too long for the underlying filesystem", e);
        }
        return datafile;
    }

    // TODO: make this method less complex.
    public byte[] load(final String id) throws UnknownResourceIdException {
        return doLoad(id);

    }

    private byte[] getContents(String id) throws UnknownResourceIdException {
        try {
            return Files.toByteArray(new File(persistenceDir, id));
        } catch (IOException e) {
            throw new UnknownResourceIdException(id, e);
        }
    }

    public void delete(final String id) throws UnknownResourceIdException {
        validateId(id);

        ScopedReadWriteLock<Void> scopedLock = new ScopedReadWriteLock<Void>(lockProvider.provideLock(id));
        scopedLock.executeWithWriteLock(new WhileLocked<Void>() {
            @Override
            public Void execute() {
                File file = new File(persistenceDir, id);
                boolean wasDeleted = file.delete();
                if (!wasDeleted) {
                    throw new UnknownResourceIdException(id);
                }
                return null;
            }
        });

    }

    /**
     * Behaviour if items are deleted while iterating is undefined; it items are added, they won't appear.
     * @return 
     */
    public Iterator<Item> searchAll() {
        File[] allFiles = this.persistenceDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (!name.endsWith(".part") && !name.startsWith("."));
            }
        });
        
        return new ItemIterator(Arrays.asList(allFiles).iterator(), this);
    }

    public void modifyWhileLocking(final String id, final DoWhileLocking<byte[]> doWhile) throws UnknownResourceIdException {
        validateId(id);

        ScopedReadWriteLock<Void> scopedLock = new ScopedReadWriteLock<Void>(lockProvider.provideLock(id));
        scopedLock.executeWithWriteLock(new WhileLocked<Void>() {
            @Override
            public Void execute() {
                byte[] newData = doWhile.execute(getContents(id));
                persistData(id, newData);
                return null;
            }
        });
    }

    public void modifyWhileLocking(final String id, final DoWhileLocking<byte[]> doWhile, final byte[] missing) throws UnknownResourceIdException {
        validateId(id);

        ScopedReadWriteLock<Void> scopedLock = new ScopedReadWriteLock<Void>(lockProvider.provideLock(id));
        scopedLock.executeWithWriteLock(new WhileLocked<Void>() {
            @Override
            public Void execute() {
                byte[] data;
                try {
                    data = getContents(id);
                } catch (UnknownResourceIdException e) {
                    data = missing;

                }

                byte[] newData = doWhile.execute(data);
                persistData(id, newData);
                return null;
            }
        });

    }
}
