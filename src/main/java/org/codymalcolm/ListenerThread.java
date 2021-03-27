package org.codymalcolm;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ListenerThread extends Thread {
    private ServerSocket serverSocket = null;
    private File directory;
    private int port;

    public ListenerThread(String name, File directory, int port) {
        super(name);
        this.directory = directory;
        this.port = port;
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.print("Server is running. ");
            System.out.println("Listening to port: " + port);
            while (true) {
                try {
                    // wait for a connection
                    Socket clientSocket = serverSocket.accept();

                    // create a handler for the new connection
                    ClientConnectionHandler handler = new ClientConnectionHandler(clientSocket, directory);

                    // create a thread to process the handler
                    Thread thread = new Thread(handler);

                    // start the thread
                    thread.start();
                } catch(SocketException e) {
                    System.out.println("Stopping the server.");
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void closeSocket() {
        if (null != serverSocket) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
