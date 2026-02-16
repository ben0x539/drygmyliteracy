package org._1d6.drygmyliteracy;

import java.util.Arrays;import static org.junit.jupiter.api.Assertions.assertEquals;

class BookWriterTest {
    @org.junit.jupiter.api.Test
    void getWrapPoint() {
        assertEquals(1, BookWriter.getWrapPoint("m"));
        assertEquals(19, BookWriter.getWrapPoint("mmmmmmmmmmmmmmmmmmmm"));
        assertEquals(57,
            BookWriter.getWrapPoint("'''''''''''''''''''''''''''''''''''''''''''''''''''''''''m"));
        assertEquals(Arrays.asList(
            "I'm just writing a",
            "bunch of nonsense",
            "into a book so I can",
            "copy it out and check",
            "that the line wrapping",
            "works out just like in",
            "the game! Surely me",
            "typing for ten",
            "seconds on a US",
            "keyboard will cover",
            "the breadth and width",
            "of the human",
            "experience."
        ), BookWriter.wrapLines(
            "I'm just writing a " +
            "bunch of nonsense " +
            "into a book so I can " +
            "copy it out and check " +
            "that the line wrapping " +
            "works out just like in " +
            "the game! Surely me " +
            "typing for ten " +
            "seconds on a US " +
            "keyboard will cover " +
            "the breadth and width " +
            "of the human " +
            "experience."));
        assertEquals(Arrays.asList("From Zombified Piglin:", "4x Rotten Flesh"),
            BookWriter.wrapLines( "From Zombified Piglin: 4x Rotten Flesh"));
    }
}