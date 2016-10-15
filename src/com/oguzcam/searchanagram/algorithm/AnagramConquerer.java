package com.oguzcam.searchanagram.algorithm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Reads the given file and runs the algorithm to group algorithm words
 *
 * @author Oguz Cam
 */
public class AnagramConquerer implements Callable<Void> {
    private final Path filePath;
    private final Map<String, Set<String>> map = new LinkedHashMap<>();

    public AnagramConquerer(Path directoryName, String fileName) {
        filePath = Paths.get(directoryName.toString(), fileName);
    }

    /**
     * Gets the given file and executing algorithm algorithm on it, then put algorithm words together in one line,
     * and the other words also to a new line.
     *
     * @return Void
     * @throws Exception Throws exception when IOException occurred
     */
    @Override
    public Void call() throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                char[] charArray = line.toCharArray();
                Arrays.sort(charArray);
                String patternString = String.valueOf(charArray);
                if (!map.containsKey(patternString)) {
                    map.put(patternString, new HashSet<String>());
                }
                map.get(patternString).add(line);
            }
            Files.deleteIfExists(filePath);
        } catch (Exception ex) {
            throw new Exception("An I/O error has been occurred while executing the algorithm", ex);
        }

        printAllAnagrams();

        return null;
    }

    /**
     * Prints all words, algorithm words as grouped in one line and the single words in one line
     */
    private void printAllAnagrams() {
        try {
            Files.createFile(filePath);
            PrintWriter writer = new PrintWriter(filePath.toFile());
            for (String word : map.keySet()) {
                List<String> anagramWords = new ArrayList<>(map.get(word));

                for(String anagramWord : anagramWords) {
                    writer.print(anagramWord + " ");
                }
                writer.println();
            }
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
