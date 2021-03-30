// Cody Malcolm 100753739
// March 27th, 2021
// CSCI 2020u - Assignment #2 - File Sharing System

package org.codymalcolm;

import java.io.File;
import java.io.IOException;

/**
 * Fully static class to provide utility functions to multiple other classes in the Application.
 */
class Utils {
    /**
     * Takes a filename and tests to see if a File with that name already exists, if so, it modifies the filename until
     * a unique name is found, then returns the found unique filename.
     *
     * @param filename the requested filename
     * @return the unique filename
     */
    public static String getFilename(String filename) {
        // initialize the File
        File temp = new File(filename);

        // if there is already a file with that filename
        if (temp.exists()) {
            // rename the file with a (1) just before the extension
            filename = addIteration(filename);

            // update the File with the new filename
            temp = new File(filename);

            // initialize the iterator
            int i = 2;

            try {
                // while a File with the current filename still exists, increment the iteration
                while (!temp.createNewFile()) {
                    filename = incrementIteration(filename, i);
                    temp = new File(filename);
                    i++;
                }
            } catch(IOException e) {
                filename = "newfile.txt";
            }
        }

        // return the unique filename
        return filename;
    }

    /**
     * Takes a filename and returns the same filename with (1) appended just before the extension (if present, or at
     * the end of the filename otherwise).
     * @param filename the filename to append the (1) to
     * @return the updated filename
     */
    private static String addIteration(String filename) {
        // get the index of the '.' that precedes the extension
        int extensionStartsAt = filename.lastIndexOf('.');

        // if there is an extension,
        if (extensionStartsAt != -1) {
            // copy the extension
            String extension = filename.substring(extensionStartsAt);

            // return the updated filename
            return filename.substring(0, extensionStartsAt) + "(1)" + extension;
        } else {
            // return the updated filename
            return filename + "(1)";
        }
    }

    /**
     * Takes a filename that has already had an iterator appended and increments the iterator by 1
     *
     * @param oldFilename the previous filename
     * @param i the new index to use
     * @return the updated filename
     */
    private static String incrementIteration(String oldFilename, int i) {
        // note the first index of the iterator
        int iterationStartsAt = oldFilename.lastIndexOf('(')+1;

        // note the index after the last index of the iterator
        int iterationEndsAt = oldFilename.lastIndexOf(')');

        // initialize the new filename to the string up to the '('
        String filename = oldFilename.substring(0, iterationStartsAt);

        // if the iterator provided matches the next index of the parsed iterator
        if (Integer.parseInt(oldFilename.substring(iterationStartsAt, iterationEndsAt))+1 == i) {
            // append the iterator
            filename += i;
        } else {
            // something is wrong, keep filename as-is
            filename += oldFilename.substring(iterationStartsAt, iterationEndsAt);
        }

        // append the rest of the filename (for eg. ").txt")
        filename += oldFilename.substring(iterationEndsAt);

        // return the new filename
        return filename;
    }

    /**
     * Takes a String input (argument to the client or server) and parses it, returning it if valid or a hardcoded
     * default if invalid.
     *
     * @param s the String to parse
     * @return a port number
     */
    public static int parsePort(String s) {
        try {
            // set port to argument, if it can be parsed as an int and is in the valid range
            int port = Integer.parseInt(s);
            if (port < 65536 && port >= 0) {
                return port;
            } else {
                // otherwise print an appropriate error message and stick with default
                System.out.println("Please note, a valid port must be between 0 and 65535, inclusive. Using default.");
            }
        } catch(NumberFormatException e) {
            System.out.println("Please note, correct usage is 'gradle startServer --args=\"<port>\"'.");
            System.out.println("The port was not understood, using default.");
        }
        return 9001;
    }

    /**
     * Parses the extension of a filename and returns true if it matches know binary-format files that would cause
     * the client to crash if parsing was attempted.
     *
     * Note: This method does not actually determine if the extension matches the file type. If a png format file is
     * saved as "file.txt", this will return false, and the program will still crash. This is essentially just to
     * prevent careless clicks or mis-clicks from causing program crashes.
     *
     * @param filename the filename to check
     * @return true if the file has an extension that matches known binary file types
     */
    public static boolean detectBinary(String filename) {
        int extensionStartsAt = filename.lastIndexOf('.');
        if (extensionStartsAt == -1) {
            return false;
        }
        String extension = filename.substring(extensionStartsAt);
        int length = extension.length();

        if (length < 2) {
            return false;
        }

        extension = extension.substring(1);

        String[] commonBinaryTypes = { "7a", "avi", "bin", "bmp", "deb", "doc", "docx", "exe", "gif", "gz", "ico",
                "iso", "jar", "jpeg", "jpg", "mkv", "mp3", "mp4", "mpa", "mpg", "mpeg", "msi", "ods", "odt", "ogg",
                "otf", "pdf", "pkg", "png", "ppt", "rar", "svg", "tif", "tiff", "ttf", "wmv", "xls", "zip" };

        for (String ext : commonBinaryTypes) {
            if (ext.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
}
