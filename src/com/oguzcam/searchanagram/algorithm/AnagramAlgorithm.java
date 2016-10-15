package com.oguzcam.searchanagram.algorithm;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static com.oguzcam.searchanagram.algorithm.AnagramAlgorithmConfiguration.FILE_EXTENSION;
import static com.oguzcam.searchanagram.algorithm.AnagramAlgorithmConfiguration.N_THREADS;

/**
 * Anagram divide-conquer algorithm
 * Create smaller files and conquer inside the files, then merges
 *
 * @author Oguz Cam
 */
public class AnagramAlgorithm {
    private static final Logger LOG = Logger.getLogger(AnagramAlgorithm.class.getName());
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(N_THREADS);

    private final String fileToProcess;

    private final Path outputDirectory;
    private Path tempDirectory;
    private int nextFileNumber;

    public AnagramAlgorithm(String selectedFile) {
        this.fileToProcess = selectedFile;
        this.tempDirectory = Paths.get("." + FileSystems.getDefault().getSeparator() + "tempfiles");
        this.outputDirectory = Paths.get("." + FileSystems.getDefault().getSeparator() + "output");
    }

    /**
     * Manages the algorithm process, calls divide, conquer and merge respectively
     *
     * @return A flag to warn calling method if the algorithm works successfully.
     * @throws Exception May throw Exception while running algorithm.
     */
    public Path process() throws Exception {
        // Create directories whether they are not created yet
        tempDirectory = Paths.get(tempDirectory.toString(), getFileName(fileToProcess) + System.currentTimeMillis());
        Files.createDirectories(tempDirectory);

        long startTime = System.currentTimeMillis();
        long diff;
        nextFileNumber = new AnagramDivider(tempDirectory, fileToProcess).divide();
        diff = System.currentTimeMillis() - startTime;
        LOG.info("Divide process has been completed in " + diff + " milliseconds");

        startTime = System.currentTimeMillis();
        conquer();
        diff = System.currentTimeMillis() - startTime;
        LOG.info("Conquer process has been completed in " + diff + " milliseconds");

        startTime = System.currentTimeMillis();
        int lastFileNumber = merge(1, nextFileNumber);
        diff = System.currentTimeMillis() - startTime;
        LOG.info("Merge process has been completed in " + diff + " milliseconds");

        Path pathToRead = Paths.get(tempDirectory.toString(), lastFileNumber + FILE_EXTENSION);
        Path pathForOutput = Paths.get(
                outputDirectory.toString(),
                getFileName(fileToProcess) + "_" + System.currentTimeMillis() + FILE_EXTENSION);

        output(pathToRead, pathForOutput);

        return pathForOutput;
    }

    /**
     * Starts an executorService to execute algorithm per file.
     *
     * @throws Exception May throw an InterruptedException while waiting termination of all tasks.
     * May throw IOException from tasks.
     */
    private void conquer() throws Exception {
        List<Callable<Void>> serviceList = new ArrayList<>(nextFileNumber - 1);
        for (int i = 1; i < nextFileNumber; i++) {
            serviceList.add(new AnagramConquerer(tempDirectory, i + FILE_EXTENSION));
        }

        try {
            EXECUTOR_SERVICE.invokeAll(serviceList);
        } catch (InterruptedException ex) {
            throw new Exception("Process has been interrupted while executing algorithm.", ex);
        }
    }

    /**
     * Merges the small files into one big file recursively.
     *
     * @throws Exception May throw an InterruptedException while waiting termination of all tasks.
     * May throw IOException from tasks.
     */
    private int merge(int firstFileNumber, int nextFileNumber) throws Exception {
        // If there is just one file, it is done
        if (nextFileNumber - 1 == firstFileNumber) {
            return firstFileNumber;
        }

        int newFirstFileNumber = nextFileNumber;
        List<Callable<Void>> serviceList = new ArrayList<>();
        Path inputPath1, inputPath2, outputPath;
        for (int i = firstFileNumber; i + 1 < nextFileNumber; i += 2) {
            inputPath1 = Paths.get(tempDirectory.toString(), i + FILE_EXTENSION);
            inputPath2 = Paths.get(tempDirectory.toString(), (i + 1) + FILE_EXTENSION);
            outputPath = Paths.get(tempDirectory.toString(), (nextFileNumber + serviceList.size()) + FILE_EXTENSION);
            serviceList.add(new AnagramMerger(inputPath1, inputPath2, outputPath));
        }

        // If last file is not processed, move it to the last again
        int existingFileCount = nextFileNumber - firstFileNumber;
        if (existingFileCount % 2 != 0) {
            inputPath1 = Paths.get(tempDirectory.toString(), (nextFileNumber - 1) + FILE_EXTENSION);
            outputPath = Paths.get(tempDirectory.toString(), (nextFileNumber + serviceList.size()) + FILE_EXTENSION);
            Files.move(inputPath1, outputPath);
            // Keep nextFileNumber sync to give it to new call
            nextFileNumber++;
        }
        // Keep nextFileNumber sync to give it to new call
        nextFileNumber += serviceList.size();

        try {
            EXECUTOR_SERVICE.invokeAll(serviceList);
        } catch (InterruptedException ex) {
            throw new Exception("Process has been interrupted while executing algorithm.", ex);
        }

        // Call recursively the same method till just one file remains
        return merge(newFirstFileNumber, nextFileNumber);
    }

    /**
     * Gives fileName without extension
     *
     * @param fileName Filename can be absolute path or relative path
     * @return Returns fileName without extension
     */
    private String getFileName(String fileName) {
        return new File(fileName).getName().split("\\.")[0];
    }

    /**
     * Reading the last merged file, eliminates non-algorithm words and transfer the data to output directory
     *
     * @param pathToRead Path to read data from
     * @param pathForOutput Path to write data to
     * @throws Exception May throw IOException
     */
    private void output(Path pathToRead, Path pathForOutput) throws Exception {
        Files.createDirectories(outputDirectory);

        try (BufferedReader reader = new BufferedReader(new FileReader(pathToRead.toFile()));
             PrintWriter writer = new PrintWriter(pathForOutput.toFile())) {
            String line;
            while((line = reader.readLine()) != null) {
                List<String> words = Arrays.asList(line.split(" "));
                if (words.size() > 1) {
                    Collections.sort(words);
                    for (String word : words) {
                        writer.print(word + " ");
                    }
                    writer.println();
                }
            }
            writer.flush();
        } catch (Exception ex) {
            throw new Exception("An error occurred while transfering the result to output.", ex);
        }

        Files.deleteIfExists(pathToRead);
        Files.deleteIfExists(tempDirectory);
    }
}
