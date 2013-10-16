package eu.franzoni.jquickrepo.repository;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XStreamRepositoryTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();


    private File myTempDir;
    private MarshallingRepository<List<String>> repo;

    @Test
    public void testAround() {
        repo = new MarshallingRepository<List<String>>(tempFolder.getRoot());
        List<String> data = new ArrayList<String>();
        data.add("ciao");
        data.add("mamma");
        this.repo.save("something", data);
        List<String> retrieved = this.repo.load("something");
        Assert.assertEquals(data, retrieved);
    }
}
