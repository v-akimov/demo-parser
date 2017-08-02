package ru.lartech.demo.parser;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by z003cptz on 24.03.2016.
 */
@Slf4j
public class DoubleSpaceCounterTest {

    @Test
    public void testGeneralCases() {
        DoubleSpaceCounter doubleSpaceCounter = DoubleSpaceCounter.INSTANCE;

        assertThat(doubleSpaceCounter.apply(null)).isEqualTo(0);
        assertThat(doubleSpaceCounter.apply("")).isEqualTo(0);

        assertThat(doubleSpaceCounter.apply("helloworld!")).isEqualTo(0);
        assertThat(doubleSpaceCounter.apply("hello world !")).isEqualTo(0);
        assertThat(doubleSpaceCounter.apply("hello  world  !")).isEqualTo(2);
        assertThat(doubleSpaceCounter.apply("  hello  world  !  ")).isEqualTo(2);
        assertThat(doubleSpaceCounter.apply("hello    world  !    ")).isEqualTo(1);
    }
}
