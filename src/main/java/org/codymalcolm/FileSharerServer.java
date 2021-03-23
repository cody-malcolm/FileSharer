package org.codymalcolm;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileSharerServer {
    final private static int PORT = 9001;
    final private static int MAX_CLIENTS = 5;

    private ServerSocket serverSocket = null;
    private Thread[] threads = null;
    private int numClients = 0;

    public FileSharerServer(File directory) {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.print("Server is running. ");
            System.out.println("Listening to port: " + PORT);
            threads = new Thread[MAX_CLIENTS];
            // create a Thread to handle each of the clients
            while (true) {
                if (numClients == MAX_CLIENTS) {
                    numClients = 0;
                } // TODO: Does this cause unintended behaviour? don't think it will, but check at end
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connecting " + numClients);
                FileSharerServerHandler handler = new FileSharerServerHandler(clientSocket, directory);

                threads[numClients] = new Thread(handler);
                threads[numClients++].start();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        String sharedFolderName = "./shared/";
        // gradle command - 'gradle startServer --args="<name>"'
        if (args.length > 0) {
            sharedFolderName = args[0];
        }
        File directory = new File(sharedFolderName);
        if (!directory.exists()) {
            System.out.println("Shared folder not found. Creating new shared folder...");
            directory.mkdir();
        }
        new FileSharerServer(directory);
    }
}
