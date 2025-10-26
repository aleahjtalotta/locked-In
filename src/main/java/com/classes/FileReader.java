package com.classes;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Utility class for loading line-based text files.
 */
public class FileReader {
    /**
     * Reads all lines from a text file into a list.
     *
     * @param fileName path to the source file
     * @return list of lines in file order; empty if the file cannot be read
     */
    public static ArrayList<String> getLines(String fileName) {
        ArrayList<String> lines = new ArrayList<>();
        try {
            File file = new File(fileName);
            Scanner reader = new Scanner(file);
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                lines.add(line);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error reading file.");
            e.printStackTrace();  
        }
        return lines;
    }
}
