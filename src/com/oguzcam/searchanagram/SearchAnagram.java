package com.oguzcam.searchanagram;

import com.oguzcam.searchanagram.userinterface.ReadFileUserInterface;

/**
 * Start point of the application, just triggers an event to show the UI
 *
 * @author Oguz Cam
 */
public class SearchAnagram {

    public static void main(String[] args) {
        // Show the UI
        new ReadFileUserInterface().buildUI();
    }
}
