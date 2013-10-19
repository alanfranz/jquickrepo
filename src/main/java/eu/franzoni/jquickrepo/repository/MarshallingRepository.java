package eu.franzoni.jquickrepo.repository;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;

public class MarshallingRepository<T> implements Repository<T> {
    

    private final ByteArrayRepo diskRepo;
    private final XStreamByteArrayMarshaller<T> marshaller;

    public MarshallingRepository(File persistenceDir) {
        this.diskRepo = new ByteArrayRepo(persistenceDir);
        this.marshaller = new XStreamByteArrayMarshaller<T>(Charset.forName("UTF-8"), new XStream(new StaxDriver()));
    }

    @Override
    public Collection<Entry<T>> loadAll() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    

    @Override
    public void save(String id, T obj) {
        this.diskRepo.save(id, marshaller.marshal(obj));

    }

    @Override
    public void saveOrUpdate(String id, T obj) {
        this.diskRepo.saveOrUpdate(id, marshaller.marshal(obj));

    }

    @Override
    public void update(String id, T obj) {
        this.diskRepo.update(id, marshaller.marshal(obj));

    }

    @Override
    public void delete(String id) {
        this.diskRepo.delete(id);

    }

    @Override
    public T load(String id) {
        return (T) this.marshaller.unmarshal(this.diskRepo.load(id));
    }

    @Override
    public void modifyWhileLocking(final String id, final DoWhileLocking<T> whileLocking) throws UnknownResourceIdException {
        this.diskRepo.modifyWhileLocking(id, new DoWhileLocking<byte[]>() {
            @Override
            public byte[] execute(byte[] data) {
                return marshaller.marshal(
                        whileLocking.execute(marshaller.unmarshal(data)                        )
                        
                        );

            }
        });
    }

    @Override
    public void modifyWhileLocking(final String id, final DoWhileLocking<T> whileLocking, final T missing) throws UnknownResourceIdException {
        this.diskRepo.modifyWhileLocking(id, new DoWhileLocking<byte[]>() {
            @Override
            public byte[] execute(byte[] data) {
                return marshaller.marshal(whileLocking.execute(
                        marshaller.unmarshal(data)));

            }
        }, marshaller.marshal(missing));

    }

    private static class XStreamByteArrayMarshaller<T> {

        final private Charset charset;
        final private XStream marshaller;

        public XStreamByteArrayMarshaller(Charset charset, XStream marshaller) {
            this.charset = charset;
            this.marshaller = marshaller;
        }

        public byte[] marshal(T obj) {
            return this.marshaller.toXML(obj).getBytes(this.charset);

        }

        public T unmarshal(byte[] bytes) {
            return (T) this.marshaller.fromXML(new String(bytes, this.charset));

        }
    }
}
