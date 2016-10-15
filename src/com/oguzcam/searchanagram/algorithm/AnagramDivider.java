package com.oguzcam.searchanagram.algorithm;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.oguzcam.searchanagram.algorithm.AnagramAlgorithmConfiguration.FILE_EXTENSION;
import static com.oguzcam.searchanagram.algorithm.AnagramAlgorithmConfiguration.THRESHOLD;

/**
 * Create small files by dividing big file into pieces
 *
 * @author Oguz Cam
 */
public class AnagramDivider {
    private final Path tempDirectory;
    private final String fileToProcess;

    public AnagramDivider(Path tempDirectory, String fileToProcess) {
        this.tempDirectory = tempDirectory;
        this.fileToProcess = fileToProcess;
    }

    /**
     * Create small files, divide big file into pieces
     *
     * @throws Exception Throws an exception with message if any exception is thrown.
     * Throws FileNotFoundException, if the given file cannot be opened.
     * Throws IOException, if any I/O problem occurs.
     */
    public int divide() throws Exception {
        int nextFileNumber = 1;

        // Read file and create smaller files
        try (BufferedReader reader = new BufferedReader(new FileReader(fileToProcess))) {
            String line;
            int i = 0;
            PrintWriter writer = new PrintWriter(
                    Paths.get(tempDirectory.toString(),
                            FileSystems.getDefault().getSeparator() + nextFileNumber++ + FILE_EXTENSION)
                            .toFile());

            while ((line = reader.readLine()) != null) {
                if (++i > THRESHOLD) {
                    writer.flush();
                    writer.close();
                    i = 1;

                    writer = new PrintWriter(
                            new File(tempDirectory + FileSystems.getDefault().getSeparator() + nextFileNumber++ + FILE_EXTENSION));
                }
                writer.println(line);
            }
            writer.flush();
            writer.close();

        } catch (FileNotFoundException ex) {
            throw new Exception(fileToProcess + " does not exist, choose another file", ex);
        } catch (IOException ex) {
            throw new Exception("An I/O error occurred, please try again", ex);
        }

        return nextFileNumber;
    }
}
