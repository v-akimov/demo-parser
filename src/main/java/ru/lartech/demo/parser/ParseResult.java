package ru.lartech.demo.parser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Created by Slava on 26.03.2016.
 */
@Getter
@Setter
@RequiredArgsConstructor
public class ParseResult {

    private final int index;
    private final List<Integer> tokens;
}
