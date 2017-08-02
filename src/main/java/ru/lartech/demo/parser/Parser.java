package ru.lartech.demo.parser;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Ordering;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by z003cptz on 23.03.2016.
 */
@Slf4j
public class Parser {
    private final File folder;
    private final Function<String, Integer> parseFunction;

    private final Lock lock = new ReentrantLock(true);
    private final ExecutorService executor;

    @VisibleForTesting
    protected ParseJob[] parseJobs;

    public Parser(File folder, Function<String, Integer> parseFunction, int threadNumber) {
        this.folder = folder;
        this.parseFunction = parseFunction;

        executor = Executors.newFixedThreadPool(threadNumber);
    }

    public List<ParseResult> execute() {
        File[] files = folder.listFiles(File::isFile);
        //apply natural order
        Arrays.sort(files, Ordering.natural());

        //prepare jobs
        parseJobs = new ParseJob[files.length];
        for (int i = 0; i < files.length; i++) {
            parseJobs[i] = new ParseJob(i, files[i]);
        }

        //submit jobs for execution

        List<Future> tasks = Stream.of(parseJobs).map(executor::submit).collect(Collectors.toList());
        try {
            for (Future task : tasks) {
                task.get();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to obtain result from concurrent task", e);
        }

        executor.shutdown();

        return Stream.of(parseJobs)
                .map(parseJob -> new ParseResult(parseJob.index, parseJob.tokens))
                .collect(Collectors.toList());
    }

    @VisibleForTesting
    protected void onJobComplete(ParseJob parseJob, int result) {
        try {
            lock.lock();
            int jobIndex = parseJob.index;

            //signal adjacent jobs to stop
            if (jobIndex > 1 && parseJobs[jobIndex - 2].stop) {
                parseJobs[jobIndex - 1].stop = true;
            }
            if (jobIndex < parseJobs.length - 2 && parseJobs[jobIndex + 2].stop) {
                parseJobs[jobIndex + 1].stop = true;
            }

            //pass tokens to neighbor job according to rules
            if (!parseJob.stop) {
                if (result % 2 == 0 && jobIndex > 0 && !parseJobs[jobIndex - 1].stop) {
                    parseJobs[jobIndex - 1].tokens.addAll(parseJob.tokens);
                    parseJob.tokens.clear();
                } else if (result % 2 == 1 && jobIndex < parseJobs.length - 1 && !parseJobs[jobIndex + 1].stop) {
                    parseJobs[jobIndex + 1].tokens.addAll(parseJob.tokens);
                    parseJob.tokens.clear();
                }
            }

            parseJob.stop = true;
        } finally {
            lock.unlock();
        }
    }

    @VisibleForTesting
    protected class ParseJob implements Runnable {
        protected final int index;
        protected final List<Integer> tokens;
        protected final File file;

        /**
         * Used to signal thread to stop working if jobs with token-1 and token+1 are finished
         */
        protected volatile boolean stop;

        protected ParseJob(int index, File file) {
            this.index = index;
            this.tokens = new CopyOnWriteArrayList<>(new Integer[]{index});
            this.file = file;
        }

        public void run() {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                int result = 0;
                String line;

                while (!stop && (line = br.readLine()) != null) {
                    result += parseFunction.apply(line);
                }

                onJobComplete(this, result);
            } catch (IOException e) {
                log.error("Failed to parse file " + file.getName(), e);
            }
        }
    }
}
