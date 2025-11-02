package com.classes;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class FileReaderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void getLinesReturnsAllLinesFromExistingFile() throws IOException {
        File textFile = temporaryFolder.newFile("notes.txt");
        try (FileWriter writer = new FileWriter(textFile)) {
            writer.write("first line\n");
            writer.write("second line\n");
            writer.write("\n");
            writer.write("final line");
        }

        List<String> lines = FileReader.getLines(textFile.getAbsolutePath());

        assertEquals(4, lines.size());
        assertEquals("first line", lines.get(0));
        assertEquals("second line", lines.get(1));
        assertEquals("", lines.get(2));
        assertEquals("final line", lines.get(3));
    }

    @Test
    public void getLinesReturnsEmptyListWhenFileNotFound() {
        List<String> lines = FileReader.getLines("missing-file-" + System.nanoTime());

        assertNotNull(lines);
        assertTrue(lines.isEmpty());
    }

    @Test
    public void getLinesReturnsEmptyListForEmptyFile() throws IOException {
        File textFile = temporaryFolder.newFile("empty.txt");

        List<String> lines = FileReader.getLines(textFile.getAbsolutePath());

        assertNotNull(lines);
        assertTrue(lines.isEmpty());
    }
}

