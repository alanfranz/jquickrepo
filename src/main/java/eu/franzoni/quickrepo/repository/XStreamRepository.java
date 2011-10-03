package eu.franzoni.quickrepo.repository;

import com.google.common.io.Files;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import java.io.File;

public class XStreamRepository<T> {
    private XStream xStream = new XStream(new StaxDriver());
    private ByteArrayRepo diskRepo;

    public XStreamRepository(File persistenceDir) {
        this.diskRepo = new ByteArrayRepo(persistenceDir);
    }

    public void save(String id, T obj) {
        this.diskRepo.save(id, this.xStream.toXML(obj).getBytes());

    }
    
    public void saveOrUpdate(String id, T obj) {
        this.diskRepo.saveOrUpdate(id, this.xStream.toXML(obj).getBytes());

    }
    
    public void update(String id, T obj) {
        this.diskRepo.update(id, this.xStream.toXML(obj).getBytes());
        
    }
    
    public void delete(String id) {
        this.diskRepo.delete(id);
        
    }
    
    public T load(String id) {
        return (T) this.xStream.fromXML(new String(this.diskRepo.load(id)));
    }
    

    public void modifyWhileLocking(final String id, final DoWhileLocking<T> whileLocking) throws UnknownResourceIdException {
        this.diskRepo.modifyWhileLocking(id, new DoWhileLocking<byte[]>() {
            @Override
            public byte[] execute(byte[] data) {
                return xStream.toXML(
                        whileLocking.execute(
                                (T) xStream.fromXML(new String(data)))).getBytes();

            }
        });
    }

    public void modifyWhileLocking(final String id, final DoWhileLocking<T> whileLocking, final T missing) throws UnknownResourceIdException {
        this.diskRepo.modifyWhileLocking(id, new DoWhileLocking<byte[]>() {
            @Override
            public byte[] execute(byte[] data) {
                return xStream.toXML(
                        whileLocking.execute(
                                (T) xStream.fromXML(new String(data)))).getBytes();

            }
        }, xStream.toXML(missing).getBytes());

    }
    

}
