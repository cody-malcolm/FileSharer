package org.codymalcolm;

import java.io.File;
import java.util.Scanner;

public class FileSharerServer {
    private static File directory;
    private static int port = 9001;
    private static boolean running = false;
    private static ListenerThread listenerThread;

    public static void main(String[] args) {
        String sharedFolderName = "shared/";
        // gradle command - 'gradle startServer --args="<directory-name> <port>"' - both args optional
        if (args.length > 0) {
            sharedFolderName += args[0];
        } else {
            sharedFolderName += "shared";
        }

        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch(NumberFormatException e) {
                System.out.println("Please note, correct usage is 'gradle startServer --args=\"<directory-name> <port>\"'.");
                System.out.println("The port was not understood, using default.");
            }
        }
        directory = new File(sharedFolderName);
        if (!directory.exists()) {
            System.out.println("Shared folder not found. Creating new shared folder...");
            directory.mkdir();
        }

        printCommands();
        processCommands();

        System.out.println("Closing the application. Goodbye.");
        System.exit(0);
    }

    private static void processCommands() {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            // get command
            String command = scanner.nextLine();
            if (command.regionMatches(true, 0, "start", 0, 5)) {
                startServer();
            } else if (command.regionMatches(true, 0, "stop", 0, 4)) {
                stopServer(true);
            } else if (command.regionMatches(true, 0, "status", 0, 6)) {
                printStatus();
            } else if (command.regionMatches(true, 0, "config", 0, 6)) {
                printConfig();
            } else if (command.regionMatches(true, 0, "help", 0, 4)) {
                printCommands();
            } else if (command.regionMatches(true, 0, "quit", 0, 4)) {
                stopServer(false);
                exit = true;
            } else {
                System.out.println("That command was not recognized.");
                printCommands();
            }
        }

        scanner.close();
    }

    private static void printConfig() {
        System.out.println("The server is using the directory '" + directory.getPath() + "' and port " + port + ".");
    }

    private static void printStatus() {
        System.out.println("The server is" + (running ? "" : " not") + " running.");
    }

    private static void stopServer(boolean notify) {
        if (running) {
            listenerThread.closeSocket();
            running = false;
        } else {
            if (notify) {
                printStatus();
            }
        }
    }

    private static void startServer() {
        if (!running) {
            listenerThread = new ListenerThread("Listener", directory, port);
            listenerThread.start();
            running = true;
        } else {
            System.out.println("The server is already running.");
        }
    }

    private static void printCommands() {
        System.out.println("          ------------\r\n          | Commands |\r\n          ------------");
        System.out.println(
                "start   - starts the server\r\n" +
                "stop    - stops the server\r\n" +
                "status  - prints the status of the server\r\n" +
                "config  - prints the configuration of the server\r\n" +
                "help    - prints a list of commands\r\n" +
                "quit    - shuts down the application");
    }

}
