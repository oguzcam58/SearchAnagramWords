package com.oguzcam.searchanagram.algorithm;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.Callable;

/**
 * Gets two input files, finds anagrams between them and puts them to output file.
 * Old input files will be removed
 *
 * @author Oguz Cam
 */
public class AnagramMerger implements Callable<Void> {
    private final Path inputPath1;
    private final Path inputPath2;
    private final Path outputPath;

    /**
     * Gets input and output paths to do the operation,
     * Given input files will be removed and merged into output file
     *
     * @param inputPath1 First conquered input path
     * @param inputPath2 Second conquered input path
     * @param outputPath Output path to write into merged result
     */
    public AnagramMerger(Path inputPath1, Path inputPath2, Path outputPath) {
        this.inputPath1 = inputPath1;
        this.inputPath2 = inputPath2;
        this.outputPath = outputPath;
    }

    @Override
    public Void call() throws Exception {
        // Create output file
        Files.createFile(outputPath);

        writeToFile(inputPath1, inputPath2, true, false);
        writeToFile(inputPath2, inputPath1, false, true);

        deleteInputFiles();

        return null;
    }

    /**
     * Compare words from path1 with path2 and add words to outputPath
     *
     * @param path1 Base path
     * @param path2 Path for comparison
     * @param addMatches If there is a match, add them together and write to the file
     * @param append Append to output path
     * @throws Exception May throw exception during I/O operation
     */
    private void writeToFile(Path path1, Path path2, boolean addMatches, boolean append) throws Exception {
        // Read from input files and write to output file
        try (BufferedReader reader1 = new BufferedReader(new FileReader(path1.toFile()));
             PrintWriter writer = new PrintWriter(new FileOutputStream(outputPath.toFile(), append))) {

            // To go to the beginning of the file, inputStream is necessary
            FileInputStream inputStream = new FileInputStream(path2.toFile());
            BufferedReader reader2 = null;
            String lineFromReader1, lineFromReader2;
            HashSet<String> anagramWords;

            while ((lineFromReader1 = reader1.readLine()) != null) {
                anagramWords = new HashSet<>();
                anagramWords.addAll(Arrays.asList(lineFromReader1.split(" ")));
                String searchPattern = getPattern(lineFromReader1);
                if (searchPattern != null) {
                    inputStream.getChannel().position(0);
                    reader2 = new BufferedReader(new InputStreamReader(inputStream));
                    while ((lineFromReader2 = reader2.readLine()) != null) {
                        String foundPattern = getPattern(lineFromReader2);
                        if (searchPattern.equals(foundPattern)) {
                            if (addMatches) {
                                anagramWords.addAll(Arrays.asList(lineFromReader2.split(" ")));
                            } else {
                                anagramWords = null;
                            }
                            break;
                        }
                    }

                    if (anagramWords != null) {
                        for (String word : anagramWords) {
                            writer.print(word + " ");
                        }
                        writer.println();
                    }
                }
            }
            // Flush and close the output file
            writer.flush();
            writer.close();

            // Close reading resources
            if (reader2 != null) {
                reader2.close();
            }
            inputStream.close();
        } catch (Exception ex) {
            throw new Exception("An I/O error occurred while merging process", ex);
        }
    }

    /**
     * Gets first word and gives pattern as String which consists of sorted chars of the word.
     *
     * @param line Line of the file
     * @return Returns sorted charArray as String
     */
    private String getPattern(String line) {
        if (!line.trim().isEmpty()) {
            char[] charArray = line.split(" ", 2)[0].toCharArray();
            Arrays.sort(charArray);
            return String.valueOf(charArray);
        }
        return null;
    }

    /**
     * Delete input files after merge process
     *
     * @throws IOException May throw IOException while trying to remove the files
     */
    private void deleteInputFiles() throws IOException {
        Files.deleteIfExists(inputPath1);
        Files.deleteIfExists(inputPath2);
    }
}
