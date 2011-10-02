package eu.franzoni.quickrepo.repository;


import com.google.common.io.Files;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;


public class ByteArrayRepoTest {

    private File myTempDir;
    private ByteArrayRepo repo;

    @Before
    public void setUp() throws Exception {
        myTempDir = Files.createTempDir();
        repo = new ByteArrayRepo(myTempDir);

    }

    @After
    public void tearDown() throws Exception {
        Files.deleteRecursively(this.myTempDir);
    }

    @Test
    public void testSaveCreatesFileWithNameId() {
        byte[] data = new byte[]{0xa, 0xb};
        this.repo.save("something", data);
        Assert.assertTrue(new File(this.myTempDir, "something").exists());


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


}
