package eu.franzoni.jquickrepo.repository;


import com.google.common.collect.Sets;
import com.google.common.io.Files;
import eu.franzoni.jquickrepo.repository.ByteArrayRepo.Item;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class ByteArrayRepoTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    private File myTempDir;
    private ByteArrayRepo repo;

    @Before
    public void setUp() throws Exception {
        myTempDir = tempFolder.newFolder("temp");
        repo = new ByteArrayRepo(myTempDir);

    }

    @Test
    public void testSaveCreatesFileWithNameId() {
        byte[] data = new byte[]{0xa, 0xb};
        this.repo.save("something", data);
        Assert.assertTrue(new File(this.myTempDir, "something").exists());


    }

    @Test(expected = UnknownResourceIdException.class)
    public void testDeleteRemovesFileWithId() {
        byte[] data = new byte[]{0xa, 0xb};
        this.repo.save("something", data);
        this.repo.delete("something");
        this.repo.load("something");
    }

    @Test(expected = UnknownResourceIdException.class)
    public void testDeleteThrowsUnknownResourceExceptionIfNoResWithIdExists() {
        this.repo.delete("something");
    }

    @Test
    public void testSavedFileContainsGivenData() throws Exception {
        byte[] test_data = new byte[]{0xa, 0xb};
        this.repo.save("something", test_data);
        byte[] actual_content = Files.toByteArray(new File(this.myTempDir, "something"));
        Assert.assertTrue(Arrays.equals(test_data, actual_content));
    }

    @Test
    public void testLoadSucceedsFetchingNamedFileFromFilesystem() throws Exception {
        byte[] ignored_data = new byte[]{0xe, 0xd};
        FileOutputStream fos = new FileOutputStream(new File(myTempDir, "somethingElse"));
        fos.write(ignored_data);

        byte[] data = this.repo.load("somethingElse");
    }

    @Test
    public void testLoadSucceedsFetchingContentFromFilesystem() throws Exception {
        byte[] test_data = new byte[]{0xc, 0xd};
        FileOutputStream fos = new FileOutputStream(new File(myTempDir, "somethingElse"));
        fos.write(test_data);

        byte[] data = this.repo.load("somethingElse");
        Assert.assertTrue(Arrays.equals(test_data, data));

    }

    @Test(expected = UnknownResourceIdException.class)
    public void testLoadFailsIfUnknownId() throws Exception {
        this.repo.load("unknown");
    }

    @Test
    public void testValidIdCanContainLettersNumbersHyphensUnderscores() throws Exception {
        this.repo.save("AXZ-axz0_9", new byte[]{0xa});
    }

    @Test(expected = BadIdException.class)
    public void testTooLongIdThrowsBadIdException() throws Exception {
        byte[] test_data = new byte[]{0xc, 0xd};
        this.repo.save("asdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasd",
                test_data);
    }

    @Test(expected = BadIdException.class)
    public void testIdWithInvalidCharsThrowsBadIdExceptionOnSave() throws Exception {
        byte[] test_data = new byte[]{0xc, 0x1};
        this.repo.save(".xxxxxx", test_data);
    }

    @Test(expected = BadIdException.class)
    public void testEmptyStringIdThrowsBadIdExceptionOnSave() throws Exception {
        byte[] test_data = new byte[]{0xc, 0x1};
        this.repo.save("", test_data);
    }

    @Test(expected = BadIdException.class)
    public void testEmptyStringIdThrowsBadIdExceptionOnLoad() throws Exception {
        this.repo.load("");
    }

    @Test(expected = BadIdException.class)
    public void testIdWithInvalidCharsThrowsBadIdExceptionOnLoad() throws Exception {
        this.repo.load("../xxxxxx");
    }

    @Test(expected = DuplicateResourceIdException.class)
    public void testSaveWithAlreadyExistingIdThrowsDuplicateResourceIdException() throws Exception {
        byte[] test_data = new byte[]{0xc, 0xf};
        this.repo.save("some", test_data);
        this.repo.save("some", test_data);
    }

    @Test(expected = MissingIdException.class)
    public void testUpdateWithoutAlreadyExistingIdThrowsDuplicateResourceIdException() throws Exception {
        byte[] test_data = new byte[]{0xc, 0xf};
        this.repo.update("some", test_data);
    }

    @Test
    public void testUpdateWithAlreadyExistingIdUpdatesData() throws Exception {
        this.repo.save("some", new byte[]{0xc, 0xf});
        this.repo.update("some", new byte[]{0xa, 0xa});
        Assert.assertTrue(Arrays.equals(this.repo.load("some"), new byte[]{0xa, 0xa}));
    }

    @Test
    public void testSaveOrUpdateDoesntCareAboutExistingOrNonExistingData() throws Exception {
        this.repo.saveOrUpdate("some", new byte[]{0xc, 0xf});
        Assert.assertTrue(Arrays.equals(this.repo.load("some"), new byte[]{0xc, 0xf}));
        this.repo.saveOrUpdate("some", new byte[]{0xa, 0xa});
        Assert.assertTrue(Arrays.equals(this.repo.load("some"), new byte[]{0xa, 0xa}));

    }

    @Test
    public void testModifyWhileLocking() throws Exception {
        this.repo.saveOrUpdate("some", new byte[]{0xc, 0xf});

        this.repo.modifyWhileLocking("some", new DoWhileLocking<byte[]>() {
            @Override
            public byte[] execute(byte[] data) {
                return new byte[]{0xa, 0xb};
            }
        });
        
        Assert.assertTrue(Arrays.equals(new byte[]{0xa, 0xb}, this.repo.load("some")));


    }

    @Test(expected = UnknownResourceIdException.class)
    public void testModifyWhileLockingFailsIfMissingId() throws Exception {
        this.repo.modifyWhileLocking("some", new DoWhileLocking<byte[]>() {
            @Override
            public byte[] execute(byte[] data) {
                return new byte[]{0xa, 0xb};
            }
        });

    }



    @Test
    public void testModifyWhileLockingDoestnUseMissingIfAlreadyInRepo() throws Exception {
        this.repo.modifyWhileLocking("some", new DoWhileLocking<byte[]>() {
            @Override
            public byte[] execute(byte[] data) {
                return new byte[]{0xa, 0xb};
            }
        }, new byte[]{0xc, 0xf});

        Assert.assertTrue(Arrays.equals(new byte[]{0xa, 0xb}, this.repo.load("some")));


    }
    
    @Test
    public void searchAllEnumeratesAllSavedDAta() throws Exception {
        this.repo.saveOrUpdate("some", new byte[]{0xc, 0xf});
        this.repo.saveOrUpdate("else", new byte[]{0xc, 0xe});
        this.repo.saveOrUpdate("third", new byte[]{0xc, 0xd});
        
        Iterator<ByteArrayRepo.Item> all = this.repo.searchAll();
        
        Set<Item> expectedItems = new HashSet<Item>();
        expectedItems.add(new Item("some", new byte[]{0xc, 0xf}));
        expectedItems.add(new Item("else", new byte[]{0xc, 0xe}));
        expectedItems.add(new Item("third", new byte[]{0xc, 0xd}));
        
        Assert.assertEquals(expectedItems, Sets.newHashSet(all));
       
    }
    
    
    


}
