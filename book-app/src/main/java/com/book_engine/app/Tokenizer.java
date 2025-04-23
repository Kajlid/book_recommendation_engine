package com.book_engine.app;

import java.util.Arrays;
import java.util.List;

public class Tokenizer {

    public static List<String> tokenize(String text) {
        return Arrays.asList(text.toLowerCase().split("\\W+"));   // split on non-word characters
    }


}