package eu.franzoni.jquickrepo.repository;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MarshallingRepositoryTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void savePersistsIfItemDoesNotExist() {
        MarshallingRepository<List<String>> repo = new MarshallingRepository<List<String>>(tempFolder.getRoot());
        List<String> data = new ArrayList<String>();
        data.add("ciao");
        data.add("mamma");
        repo.save("something", data);
        List<String> load = repo.load("something");
        Assert.assertEquals(load, data);
    }

    @Test(expected = DuplicateResourceIdException.class)
    public void saveFailsIfItemExists() {
        MarshallingRepository<List<String>> repo = new MarshallingRepository<List<String>>(tempFolder.getRoot());
        List<String> data = new ArrayList<String>();
        data.add("ciao");
        data.add("mamma");
        repo.save("something", data);
        repo.save("something", data);
    }

    @Test
    public void saveOrUpdateAlwaysPersists() {
        MarshallingRepository<List<String>> repo = new MarshallingRepository<List<String>>(tempFolder.getRoot());
        List<String> data = new ArrayList<String>();
        data.add("ciao");
        data.add("mamma");
        repo.saveOrUpdate("something", data);
        data.add("forever");
        repo.saveOrUpdate("something", data);
        List<String> load = repo.load("something");
        Assert.assertEquals(load, data);
    }

    @Test(expected = MissingIdException.class)
    public void updateFailsIfItemDoesNotExist() {
        MarshallingRepository<List<String>> repo = new MarshallingRepository<List<String>>(tempFolder.getRoot());
        List<String> data = new ArrayList<String>();
        data.add("ciao");
        data.add("mamma");
        repo.update("something", data);
    }

    @Test
    public void updatePersistsIfItemExists() {
        MarshallingRepository<List<String>> repo = new MarshallingRepository<List<String>>(tempFolder.getRoot());
        List<String> data = new ArrayList<String>();
        data.add("ciao");
        data.add("mamma");
        repo.save("something", data);
        data.add("forever");
        repo.update("something", data);
        List<String> load = repo.load("something");
        Assert.assertEquals(load, data);
    }
   

    @Test(expected = UnknownResourceIdException.class)
    public void loadFailsIfElementDoesNotExist() {
        MarshallingRepository<List<String>> repo = new MarshallingRepository<List<String>>(tempFolder.getRoot());
        repo.load("something");
    }

    public void loadAllLoadsAllEntriesInRepo() {
        MarshallingRepository<String> repo = new MarshallingRepository<String>(tempFolder.getRoot());
        repo.save("1", "a");
        repo.save("2", "b");
        repo.save("3", "c");
        final HashSet<Entry> expected = Sets.newHashSet(new Entry("1", "a"), new Entry("2", "b"), new Entry("3", "c"));
        final Set<Entry<String>> loaded = new HashSet<Entry<String>>(repo.loadAll());
        assertEquals(expected, loaded);

    }
}
