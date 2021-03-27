package org.codymalcolm;

import java.io.File;
import java.util.Scanner;

public class FileSharerServer {
    private static FileSharerServer server = new FileSharerServer();
    private static File directory;
    private static int port = 9001;
    private boolean running = false;
    private ListenerThread listenerThread;

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

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            // get command
            String command = scanner.nextLine();
            if (command.regionMatches(true, 0, "start", 0, 5)) {
                startServer();
            } else if (command.regionMatches(true, 0, "stop", 0, 4)) {
                stopServer();
            } else if (command.regionMatches(true, 0, "status", 0, 6)) {
                System.out.println("The server is " + (server.running ? "" : "not") + " running.");
            } else if (command.regionMatches(true, 0, "help", 0, 4)) {
                printCommands();
            } else if (command.regionMatches(true, 0, "quit", 0, 4)) {
                stopServer();
                exit = true;
            } else {
                System.out.println("That command was not recognized.");
                printCommands();
            }
        }

        scanner.close();
        System.out.println("Closing the application. Goodbye.");
        System.exit(0);
    }

    private static void stopServer() {
        if (server.running) {
            server.listenerThread.closeSocket();
            server.running = false;
        }
    }

    private static void startServer() {
        if (!server.running) {
            server.listenerThread = new ListenerThread("Listener", directory, port);
            server.listenerThread.start();
            server.running = true;
        } else {
            System.out.println("The server is already running.");
        }
    }

    private static void printCommands() {
        System.out.println("          ------------\r\n          | Commands |\r\n          ------------");
        System.out.println("start   - starts the server");
        System.out.println("stop    - stops the server");
        System.out.println("status  - prints the status of the server");
        System.out.println("help    - prints a list of commands");
        System.out.println("quit    - shuts down the application\r\n");
    }

}
