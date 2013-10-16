package eu.franzoni.jquickrepo.repository;

import com.google.common.io.Files;
import junit.framework.TestCase;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XStreamRepositoryTest extends TestCase {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();


    private File myTempDir;
    private MarshallingRepository<List<String>> repo;

    @Before
    public void setUp() throws Exception {
        myTempDir = tempFolder.newFolder("mytemp");
        repo = new MarshallingRepository<List<String>>(myTempDir);
    }

    @After
    public void tearDown() throws Exception {
        Files.deleteRecursively(this.myTempDir);
    }

    @Test
    public void testAround() {
        List<String> data = new ArrayList<String>();
        data.add("ciao");
        data.add("mamma");
        this.repo.save("something", data);
        List<String> retrieved = this.repo.load("something");
        Assert.assertEquals(data, retrieved);
    }
}
