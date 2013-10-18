package eu.franzoni.jquickrepo.repository;

import com.google.common.io.Files;
import eu.franzoni.jquickrepo.concurrency.ScopedLock;
import eu.franzoni.jquickrepo.concurrency.MultipleResourceLock;
import eu.franzoni.jquickrepo.concurrency.WhileLocked;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ByteArrayRepo {

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

        ScopedLock<Void> writeLock = new ScopedLock<Void>(lockProvider.provideLock(id).writeLock());
        writeLock.executeWhileLocking(new WhileLocked<Void>() {
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

        ScopedLock<Void> writeLock = new ScopedLock<Void>(lockProvider.provideLock(id).writeLock());
        writeLock.executeWhileLocking(new WhileLocked<Void>() {
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

        ScopedLock<Void> writeLock = new ScopedLock<Void>(lockProvider.provideLock(id).writeLock());
        writeLock.executeWhileLocking(new WhileLocked<Void>() {
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
        // not required but suggested, as it prevents traversals.
        validateId(id);

        ScopedLock<byte[]> readLock = new ScopedLock<byte[]>(this.lockProvider.provideLock(id).readLock());

        return readLock.executeWhileLocking(new WhileLocked<byte[]>() {
            @Override
            public byte[] execute() {
                return getContents(id);
            }
        });

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

        ScopedLock<Void> writeLock = new ScopedLock<Void>(lockProvider.provideLock(id).writeLock());
        writeLock.executeWhileLocking(new WhileLocked<Void>() {
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

    public void modifyWhileLocking(final String id, final DoWhileLocking<byte[]> doWhile) throws UnknownResourceIdException {
        validateId(id);

        ScopedLock<Void> writeLock = new ScopedLock<Void>(lockProvider.provideLock(id).writeLock());
        writeLock.executeWhileLocking(new WhileLocked<Void>() {
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

        ScopedLock<Void> writeLock = new ScopedLock<Void>(lockProvider.provideLock(id).writeLock());
        writeLock.executeWhileLocking(new WhileLocked<Void>() {
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
