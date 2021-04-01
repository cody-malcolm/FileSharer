// Cody Malcolm 100753739
// March 27th, 2021
// CSCI 2020u - Assignment #2 - File Sharing System

package org.codymalcolm;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * A ListenerThread listens for incoming connection requests on the specified port. When a request comes in, it creates
 * a handler to process and respond to the request. Also handles closure of the associated socket.
 */
public class ListenerThread extends Thread {
    /** The Socket used to facilitate the connection */
    private ServerSocket serverSocket = null;
    /** The directory to use as the shared directory for this instance */
    final private File directory;
    /** The port to listen to */
    final private int port;
    /** A flag to indicate if a stop has been requested */
    private boolean stopped;

    /**
     * Constructor for ListenerThread. Just sets fields based on the parameters.
     *
     * @param directory The directory that will be used to facilitate client requests
     * @param port The port that will be listened to
     */
    public ListenerThread(File directory, int port) {
        super("Listener");
        this.directory = directory;
        this.port = port;
    }

    /**
     * This is executed when the FileSharerServer starts the Thread. After initializing some variables and printing
     * status messages, it will listen to and process incoming client requests until the FileSharerServer invokes the
     * closeSocket() method which closes the Socket - this interrupts the Socket's accept() method, the SocketException
     * is caught, and the loop ends (via closeSocket() updating the stopped flag).
     */
    @Override
    public void run() {
        try {
            // initialize the flag and Socket
            stopped = false;
            serverSocket = new ServerSocket(port);

            // print a message indicating that the server is listening for requests
            System.out.println("The server is listening to port: " + port);

            while (!stopped) {
                try {
                    // wait for a connection
                    Socket clientSocket = serverSocket.accept();

                    // create a handler for the new connection
                    ClientConnectionHandler handler = new ClientConnectionHandler(clientSocket, directory);

                    // create a thread to process the request
                    Thread thread = new Thread(handler);

                    // start the thread
                    thread.start();
                } catch(SocketException e) {
                    // Happens when thread is interrupted. Do nothing.
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the Socket (and updates the stopped flag accordingly)
     */
    public void closeSocket() {
        // guard against incorrect usage (ex. being invoked before the Thread has been started and Socket initialized)
        if (null != serverSocket) {
            // update stopped flag
            stopped = true;

            // close the Socket
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
