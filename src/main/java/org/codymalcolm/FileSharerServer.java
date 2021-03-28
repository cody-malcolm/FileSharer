// Cody Malcolm 100753739
// March 27th, 2021
// CSCI 2020u - Assignment #2 - File Sharing System

package org.codymalcolm;

import java.io.File;
import java.util.Scanner;

/**
 * As implemented, the FileSharerServer class is an entirely static class that handles the main flow of the Server-side
 * of the FileSharer Application. It is primarily focused on providing server management functionality to the admin and
 * executing the requested instructions. As it is a fully static class, there is only one ListenerThread at a time.
 * However, it is implemented in such a way that it would be quite easy to refactor to use one or more FileSharerServer
 * instances, if such functionality is needed (for multiple concurrent ports, directories, varying degrees of password
 * protection, etc).
 */
public class FileSharerServer {
    /** The shared directory the server uses for servicing client requests */
    private static File directory;
    /** The port the server will listen to */
    private static int port = 9001;
    /** A flag to track whether the server is listening for requests or not */
    private static boolean listening = false;
    /** A Thread to handle the listening for client requests (so the server listen for admin input) */
    private static ListenerThread listenerThread;

    /**
     * Main method. Initializes the port (if provided), prints instructions for admin, and then processes admin input.
     *
     * @param args If any, first should be an integer to be saved as the port to listen to
     */
    public static void main(String[] args) {
        // optional argument for port
        if (args.length > 0) {
            try {
                // set port to argument, if it can be parsed as an int
                port = Integer.parseInt(args[1]);
            } catch(NumberFormatException e) {
                // otherwise stick with default
                System.out.println("Please note, correct usage is 'gradle startServer --args=\"<port>\"'.");
                System.out.println("The port was not understood, using default.");
            }
        }

        printCommands();
        processCommands();

        System.out.println("Closing the application. Goodbye.");
        System.exit(0);
    }

    /**
     * A helper method to read in admin instructions. Calls the appropriate handler function for each valid instruction.
     */
    private static void processCommands() {
        // initialize the Scanner
        Scanner scanner = new Scanner(System.in);

        // initialize the exit flag
        boolean exit = false;

        while (!exit) {
            // wait for a command (note: requires gradle exec task to have 'standardInput = System.in')
            String command = scanner.nextLine();

             if (command.regionMatches(true, 0, "start", 0, 5)) {
                // parse the optional argument and pass the folder name (or default) to the handler
                String sharedFolderName = "shared/";
                String[] arguments = command.split("\\s");
                if (arguments.length > 1) {
                    sharedFolderName += arguments[1];
                } else {
                    sharedFolderName += "shared";
                }
                startListening(sharedFolderName);
            } else if (command.regionMatches(true, 0, "stop", 0, 4)) {
                 // all remaining else-ifs just call appropriate handler except "quit"
                 // true - print message if instruction is redundant
                stopListening(true);
            } else if (command.regionMatches(true, 0, "status", 0, 6)) {
                printStatus();
            } else if (command.regionMatches(true, 0, "config", 0, 6)) {
                printConfig();
            } else if (command.regionMatches(true, 0, "help", 0, 4)) {
                printCommands();
            } else if (command.regionMatches(true, 0, "quit", 0, 4)) {
                 // false - do not print message if stop instruction is redundant
                stopListening(false);
                // update exit flag
                exit = true;
            } else {
                 // display an appropriate message and reprint the commands
                System.out.println("That command was not recognized.");
                printCommands();
            }
        }

        // close the Scanner
        scanner.close();
    }

    /**
     * Prints an appropriate message notifying the admin of the directory (if listening) and port being used.
     */
    private static void printConfig() {
        if (listening) {
            System.out.println("The server is using the directory '" + directory.getPath() + "' and port " + port + ".");
        } else {
            System.out.println("The server is configured to use port " + port + ".");
        }
    }

    /**
     * Prints a message notifying the admin if the server is listening for requests or not.
     */
    private static void printStatus() {
        System.out.println("The server is" + (listening ? "" : " not") + " listening for requests.");
    }

    /**
     * Handles a stop listening instruction.
     *
     * @param notify Whether or not to print a message when redundant stop instruction is received.
     */
    private static void stopListening(boolean notify) {
        // if server is listening,
        if (listening) {
            // close the socket
            listenerThread.closeSocket();

            // update the listening flag
            listening = false;

            // clear the directory
            directory = null;

            // print an appropriate message
            System.out.println("The server has stopped listening for requests.");
        } else {
            // code style note: don't like else-if here for semantic reasons
            // server was already stopped - print status message if notify flag is true
            if (notify) {
                printStatus();
            }
        }
    }

    /**
     * Handles a start listening instruction.
     *
     * @param name The directory to use to facilitate client requests
     */
    private static void startListening(String name) {
        // if the server is not listening,
        if (!listening) {
            // set the directory
            directory = new File(name);

            // if the directory doesn't exist, create it and print an appropriate message
            if (!directory.exists()) {
                System.out.println("Shared folder not found. Creating new shared folder...");
                directory.mkdir();
            }

            // create a new Thread to listen for client requests
            listenerThread = new ListenerThread(directory, port);

            // start the thread
            listenerThread.start();

            // update the listening flag
            listening = true;
        } else {
            // print an appropriate message
            System.out.println("The server is already listening for requests");
        }
    }

    /**
     * Prints a list of valid instructions to the console.
     */
    private static void printCommands() {
        System.out.println("          ------------\r\n          | Commands |\r\n          ------------");
        System.out.println(
                "start [name] - starts listening for requests (name = shared folder to use, default='shared')\r\n" +
                "stop         - stops listening for requests\r\n" +
                "status       - prints the status of the server\r\n" +
                "config       - prints the configuration of the server\r\n" +
                "help         - prints a list of commands\r\n" +
                "quit         - shuts down the application");
    }

}
