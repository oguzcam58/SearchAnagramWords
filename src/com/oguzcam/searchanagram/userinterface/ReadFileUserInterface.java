package com.oguzcam.searchanagram.userinterface;

import com.oguzcam.searchanagram.algorithm.AnagramAlgorithm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Presents a swing panel to get Input File by User's selection.
 *
 * @author Oguz Cam
 */
public class ReadFileUserInterface {
    private final JFrame frame;
    private final JTextField filePath;

    public ReadFileUserInterface() {
        frame = new JFrame("Input File Selection For Anagram Processing");
        filePath = new JTextField();
    }

    /**
     * Creates UI for getting input file
     */
    public void buildUI() {
        // Configure JFrame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.setSize(500, 250);

        // Add components
        JPanel mainPanel = buildMainPanel();
        frame.getContentPane().add(mainPanel);

        // Show it
        frame.setVisible(true);
    }

    /**
     * Creates main panel to show elements in the frame
     *
     * @return JPanel with elements
     */
    private JPanel buildMainPanel() {
        // Create TopPanel and add relative components
        JPanel topPanel = new JPanel(new GridLayout(2,4));
        topPanel.add(new JLabel("Selected File"));
        topPanel.add(filePath);
        topPanel.add(new JLabel(""));
        JButton fileChooseButton = new JButton("Select File");
        fileChooseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.getName().endsWith(".txt");
                    }

                    @Override
                    public String getDescription() {
                        return "txt";
                    }
                });
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    filePath.setText(selectedFile.getAbsolutePath());
                }
            }
        });
        topPanel.add(fileChooseButton);

        // Create bottomPanel and add relative components
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        JButton processButton = new JButton("Process");
        processButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Path selectedFile = Paths.get(filePath.getText());
                if (validateFile(selectedFile)) {
                    try {
                        Path result = new AnagramAlgorithm(selectedFile.toString()).process();
                        if (result == null) {
                            JOptionPane.showMessageDialog(frame, "Please try again. An error occurred.");
                        } else {
                            JOptionPane.showMessageDialog(frame,
                                    "The process has been completed successfully. \r\n"
                                            + "You can see the output file in \r\n"
                                            + result.toAbsolutePath().normalize().toString());
                        }
                    } catch (Exception ex) {
                        showError(ex.getMessage());
                    }
                } else {
                    showError("The file is not valid, make sure it exists and a txt file.");
                }
            }

            private void showError(String message) {
                JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
            }

            private boolean validateFile(Path selectedFile) {
                return Files.exists(selectedFile) && Files.isRegularFile(selectedFile) && selectedFile.toString().endsWith(".txt");
            }
        });
        bottomPanel.add(processButton);

        // Keep panels structured in mainPanel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(BorderLayout.NORTH, topPanel);
        mainPanel.add(BorderLayout.SOUTH, bottomPanel);
        return mainPanel;
    }

}
