package eu.franzoni.jquickrepo.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ConcurrentAccessMarshallingRepositoryTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void runModifyWhileLocking() throws Exception {
        MarshallingRepository<List<String>> repo = new MarshallingRepository<List<String>>(tempFolder.getRoot());
        test(1000, repo);
    }

    private void test(final int threadCount, final MarshallingRepository<List<String>> repo) throws Exception {

        Callable<Void> task = new Callable<Void>() {
            @Override
            public Void call() {
                repo.modifyWhileLocking("asd", new DoWhileLocking<List<String>>() {
                    @Override
                    public List<String> execute(List<String> data) {
                        List<String> copy = new ArrayList<String>(data);
                        copy.add("a");
                        return copy;
                    }
                }, Collections.<String>emptyList());
                return null;
            }
        };


        List<Callable<Void>> tasks = Collections.nCopies(threadCount, task);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<Future<Void>> futures = executorService.invokeAll(tasks);
        // Check for exceptions
        int count = 0;
        for (Future<Void> future : futures) {
            // Throws an exception if an exception was thrown by the task.
            future.get();
            count += 1;
        }
        // Validate the IDs
        Assert.assertEquals(count, threadCount);
        Assert.assertEquals(threadCount, repo.load("asd").size());
    }
}
