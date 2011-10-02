package eu.franzoni.quickrepo.repository;

import com.google.common.io.Files;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XStreamRepositoryTest extends TestCase {
    private File myTempDir;
    private XStreamRepository<List<String>> repo;

    @Before
    public void setUp() throws Exception {
        myTempDir = Files.createTempDir();
        repo = new XStreamRepository<List<String>>(myTempDir);
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
