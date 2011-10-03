package eu.franzoni.quickrepo.repository;

import com.google.common.io.Files;
import eu.franzoni.quickrepo.concurrency.ClosureLock;
import eu.franzoni.quickrepo.concurrency.MultipleResourceLock;
import eu.franzoni.quickrepo.concurrency.WhileLocked;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.Lock;


public class ByteArrayRepo {
    final private File persistenceDir;
    final private MultipleResourceLock lockProvider = new MultipleResourceLock();

    public ByteArrayRepo(File persistenceDir) {
        validatePersistenceDir(persistenceDir);
        this.persistenceDir = persistenceDir;
    }

    private void validatePersistenceDir(File persistenceDir) {
        if (!persistenceDir.isDirectory()) {
            throw new RuntimeException("persistence dir must be a directory");
        }

        if (!persistenceDir.canWrite()) {
            throw new RuntimeException("persistence dir must be writeable");

        }

        if (!persistenceDir.canRead()) {
            throw new RuntimeException("persistence dir must be readable");
        }
    }

    public void save(final String id, final byte[] data) {
        validateId(id);

        ClosureLock writeLock = new ClosureLock<Object>(lockProvider.provideLock(id).writeLock());
        writeLock.executeWhileLocking(new WhileLocked<Object>() {
            @Override
            public Object execute() {
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

        ClosureLock writeLock = new ClosureLock<Object>(lockProvider.provideLock(id).writeLock());
        writeLock.executeWhileLocking(new WhileLocked<Object>() {
            @Override
            public Object execute() {
                verifyResourceExists(id);
                persistData(id, data);
                return null;
            }
        });

    }

    public void saveOrUpdate(final String id, final byte[] data) {
        validateId(id);

        ClosureLock writeLock = new ClosureLock<Object>(lockProvider.provideLock(id).writeLock());
        writeLock.executeWhileLocking(new WhileLocked<Object>() {
            @Override
            public Object execute() {
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
        boolean succeeded = datafile.renameTo(new File(this.persistenceDir, id));

        if (!succeeded) {
            throw new RuntimeException("could not rename file to final name!");
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

        // this is required only in order to check whether the name is too long.
        try {
            datafile.createNewFile();
        } catch (IOException e) {
            throw new BadIdException(id, "too long for the underlying filesystem", e);
        }
        return datafile;
    }

    // TODO: make this method less complex.
    public byte[] load(String id) throws UnknownResourceIdException {
        // not required but suggested, as it prevents traversals.
        validateId(id);
        try {
            Lock readLock = lockProvider.provideLock(id).readLock();
            readLock.lock();
            try {
                return Files.toByteArray(new File(persistenceDir, id));
            } finally {
                readLock.unlock();
            }
        } catch (IOException e) {
            throw new UnknownResourceIdException(id, e);
        }
    }

    public void delete(String id) throws UnknownResourceIdException {
        validateId(id);

        Lock writeLock = lockProvider.provideLock(id).writeLock();
        writeLock.lock();
        try {
            File file = new File(persistenceDir, id);
            boolean wasDeleted = file.delete();
            if (!wasDeleted) {
                throw new UnknownResourceIdException(id);
            }
        } finally {
            writeLock.unlock();
        }

    }

    public void modifyWhileLocking(String id, DoWhileLocking<byte[]> doWhile) {
        validateId(id);
        Lock writeLock = lockProvider.provideLock(id).writeLock();
        writeLock.lock();
        try {
            byte[] newData = doWhile.execute(Files.toByteArray(new File(persistenceDir, id)));
            persistData(id, newData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            writeLock.unlock();
        }


    }
}



