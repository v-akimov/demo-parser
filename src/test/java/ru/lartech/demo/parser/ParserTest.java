package ru.lartech.demo.parser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by z003cptz on 23.03.2016.
 */
public class ParserTest {
    private static final int paserThreads = 4;
    private static final File testFolder = new File("./target/test");

    private static final int testFilesAmount = RandomUtils.nextInt(10, 40);
    private static int[] testData = new int[testFilesAmount];

    @BeforeClass
    public static void beforeClass() throws IOException {
        if (!testFolder.exists()) {
            assertThat(testFolder.mkdir()).isTrue();
        }
        FileUtils.cleanDirectory(testFolder);
        for (int i = 0; i < testFilesAmount; i++) {
            File file = new File(testFolder, "test" + Integer.toString(10000 + i) + ".txt");
            List<String> fileContent = new LinkedList<>();
            for (int lines = 0; lines < RandomUtils.nextInt(500, 5000); lines++) {
                StringBuilder builder = new StringBuilder();
                int doubleSpacesAmount = RandomUtils.nextInt(1, 13);
                for (int m = 0; m < doubleSpacesAmount; m++) {
                    builder.append(RandomStringUtils.randomNumeric(RandomUtils.nextInt(1, 20)));
                    builder.append("  ");
                    builder.append(RandomStringUtils.randomNumeric(RandomUtils.nextInt(1, 20)));
                }
                testData[i] = doubleSpacesAmount;
                fileContent.add(builder.toString());
            }
            FileUtils.writeLines(file, fileContent);
        }
    }


    @Test
    public void testAllFilesShouldBeProcessed() {

        //given
        Parser parser = new Parser(testFolder, DoubleSpaceCounter.INSTANCE, paserThreads);

        //when
        List<ParseResult> result = parser.execute();

        //then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(testFilesAmount);

        //check that all tokens are in place
        Set<Integer> tokens = new HashSet<>();
        result.forEach(e -> tokens.addAll(e.getTokens()));
        assertThat(tokens).hasSize(testFilesAmount);
    }

    @Test
    public void testFirstJobShouldStopIfEvenSpacesFound() {
        //given
        Parser parser = new Parser(null, DoubleSpaceCounter.INSTANCE, 3);
        parser.parseJobs = new Parser.ParseJob[] {
                parser.new ParseJob(0, null),
                parser.new ParseJob(1, null),
                parser.new ParseJob(2, null)
        };

        //when
        parser.onJobComplete(parser.parseJobs[0], 2);

        //then
        assertThat(parser.parseJobs[0].tokens).isEqualTo(Collections.singletonList(0));
    }

    @Test
    public void testLasJobShouldStopIfOddSpacesFound() {
        //given
        Parser parser = new Parser(null, DoubleSpaceCounter.INSTANCE, 3);
        parser.parseJobs = new Parser.ParseJob[] {
                parser.new ParseJob(0, null),
                parser.new ParseJob(1, null),
                parser.new ParseJob(2, null)
        };

        //when
        parser.onJobComplete(parser.parseJobs[2], 1);

        //then
        assertThat(parser.parseJobs[2].tokens).isEqualTo(Collections.singletonList(2));
    }

    @Test
    public void testJobShouldStopIfAdjacentsAreStopped() {
        //given
        Parser parser = new Parser(null, DoubleSpaceCounter.INSTANCE, 3);
        parser.parseJobs = new Parser.ParseJob[] {
                parser.new ParseJob(0, null),
                parser.new ParseJob(1, null),
                parser.new ParseJob(2, null)
        };


        //when
        parser.onJobComplete(parser.parseJobs[0], 1);
        parser.onJobComplete(parser.parseJobs[2], 1);

        //then
        assertThat(parser.parseJobs[1].stop).isTrue();
    }

    @Test
    public void testEvenResultProcessing() {
        //given
        Parser parser = new Parser(null, DoubleSpaceCounter.INSTANCE, 3);
        parser.parseJobs = new Parser.ParseJob[] {
                parser.new ParseJob(0, null),
                parser.new ParseJob(1, null),
                parser.new ParseJob(2, null),
        };

        //when
        parser.onJobComplete(parser.parseJobs[2], 2);
        parser.onJobComplete(parser.parseJobs[1], 2);
        //passing 3 to check that this will not pass to job 1
        parser.onJobComplete(parser.parseJobs[0], 3);

        //given
        assertThat(parser.parseJobs[0].tokens).isEqualTo(Arrays.asList(0,1,2));
    }

    @Test
    public void testOddResultProcessing() {
        //given
        Parser parser = new Parser(null, DoubleSpaceCounter.INSTANCE, 3);
        parser.parseJobs = new Parser.ParseJob[] {
                parser.new ParseJob(0, null),
                parser.new ParseJob(1, null),
                parser.new ParseJob(2, null),
        };

        //when
        parser.onJobComplete(parser.parseJobs[0], 3);
        parser.onJobComplete(parser.parseJobs[1], 5);
        //passing 6 to check that this will not pass to job 1
        parser.onJobComplete(parser.parseJobs[2], 6);

        //given
        assertThat(parser.parseJobs[2].tokens).isEqualTo(Arrays.asList(2,1,0));
    }
}
