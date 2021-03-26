package org.codymalcolm;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileSharerServer {
    private static int port = 9001;
    final private static int MAX_CLIENTS = 5;

    private ServerSocket serverSocket = null;
    private Thread[] threads = null;
    private int clientNo = 0;

    public FileSharerServer(File directory) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.print("Server is running. ");
            System.out.println("Listening to port: " + port);
            threads = new Thread[MAX_CLIENTS];
            // create a Thread to handle each of the clients
            while (true) {
                if (clientNo == MAX_CLIENTS) {
                    clientNo = 0;
                }
                Socket clientSocket = serverSocket.accept();
                ClientConnectionHandler handler = new ClientConnectionHandler(clientSocket, directory);

                threads[clientNo] = new Thread(handler);
                threads[clientNo++].start();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }


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
        File directory = new File(sharedFolderName);
        if (!directory.exists()) {
            System.out.println("Shared folder not found. Creating new shared folder...");
            directory.mkdir();
        }
        new FileSharerServer(directory);
    }
}
