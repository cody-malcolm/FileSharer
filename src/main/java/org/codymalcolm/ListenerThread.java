package org.codymalcolm;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * A ListenerThread listens for incoming connection requests on the specified port. When a request comes in, it creates
 * a handler to process and respond to the request.
 */
public class ListenerThread extends Thread {
    private ServerSocket serverSocket = null;
    private File directory;
    private int port;
    private boolean stopped;

    public ListenerThread(String name, File directory, int port) {
        super(name);
        this.directory = directory;
        this.port = port;
    }

    public void run() {
        try {
            stopped = false;
            serverSocket = new ServerSocket(port);
            System.out.print("The server is running. ");
            System.out.println("Listening to port: " + port);
            while (!stopped) {
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
                    // happens when thread is interrupted.
                    System.out.println("Stopping the server.");
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void closeSocket() {
        if (null != serverSocket) {
            stopped = true;
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
