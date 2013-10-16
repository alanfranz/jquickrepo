package eu.franzoni.jquickrepo.concurrency;

import eu.franzoni.jquickrepo.repository.ByteArrayRepo;
import eu.franzoni.jquickrepo.repository.DoWhileLocking;
import eu.franzoni.jquickrepo.repository.MarshallingRepository;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ConcurrentAccessByteArrayRepoTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void runModifyWhileLocking() throws Exception {
        ByteArrayRepo byteArrayRepo = new ByteArrayRepo(tempFolder.getRoot());
        test(1000, byteArrayRepo);
    }

    private void test(final int threadCount, final ByteArrayRepo repo) throws Exception {

        Callable<Void> task = new Callable<Void>() {
            @Override
            public Void call() {
                repo.modifyWhileLocking("asd", new DoWhileLocking<byte[]>() {
                    @Override
                    public byte[] execute(byte[] data) {
                        byte[] copy = Arrays.copyOf(data, data.length + 1);
                        copy[data.length] = 'a';
                        return copy;
                    }
                }, new byte[0]);
                return null;
            }
        };


        List<Callable<Void>> tasks = Collections.nCopies(threadCount, task);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<Future<Void>> futures = executorService.invokeAll(tasks);
        // Check for exceptions
        int goodCount = 0;
        for (Future<Void> future : futures) {
            // Throws an exception if an exception was thrown by the task.
            try {
            future.get();
                goodCount += 1;
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

        }
        // Validate the IDs
        Assert.assertEquals(goodCount, threadCount);
        Assert.assertEquals(threadCount, repo.load("asd").length);
    }
}
