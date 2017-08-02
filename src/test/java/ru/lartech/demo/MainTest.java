package ru.lartech.demo;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Slava on 27.03.2016.
 */
public class MainTest {
    private static final File testFolder = new File("./target/test");

    @BeforeClass
    public static void beforeClass() throws IOException {
        if (!testFolder.exists()) {
            assertThat(testFolder.mkdir()).isTrue();
        }
    }

    @Test
    public void testOnlyThreeArgumentsMustBeAccepted() {
        Assertions.assertThat(Main.checkArguments()).isFalse();
        Assertions.assertThat(Main.checkArguments("one")).isFalse();
        Assertions.assertThat(Main.checkArguments("one", "two")).isFalse();
        Assertions.assertThat(Main.checkArguments("one", "two", "three", "four")).isFalse();
    }

    @Test
    public void testThreadNumberShouldBeMoreThan2() {
        Assertions.assertThat(Main.checkArguments(testFolder.getAbsolutePath(), getResultFilePath(), "4")).isTrue();
        Assertions.assertThat(Main.checkArguments(testFolder.getAbsolutePath(), getResultFilePath(), "3")).isTrue();
        Assertions.assertThat(Main.checkArguments(testFolder.getAbsolutePath(), getResultFilePath(), "2")).isFalse();
        Assertions.assertThat(Main.checkArguments(testFolder.getAbsolutePath(), getResultFilePath(), "not a number")).isFalse();
    }

    @Test
    public void testScanFolderMustBeAccessible() {
        Assertions.assertThat(Main.checkArguments(testFolder.getAbsolutePath(), getResultFilePath(), "4")).isTrue();
        Assertions.assertThat(Main.checkArguments("fsdfsdfsdsdf", getResultFilePath(), "4")).isFalse();
    }

    @Test
    public void testResultFileMustBeAccessible() {
        Assertions.assertThat(Main.checkArguments(testFolder.getAbsolutePath(), getResultFilePath(), "4")).isTrue();
        Assertions.assertThat(Main.checkArguments(testFolder.getAbsolutePath(), "not/a/real/path/result.txt", "4")).isFalse();
    }

    private String getResultFilePath() {
        return new File(testFolder, "results.txt").getAbsolutePath();
    }
}
