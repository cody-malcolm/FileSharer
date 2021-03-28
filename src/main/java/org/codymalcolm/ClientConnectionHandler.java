// Cody Malcolm 100753739
// March 27th, 2021
// CSCI 2020u - Assignment #2 - File Sharing System

package org.codymalcolm;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;

/**
 * A ClientConnectionHandler handles a connection to client. Each ClientConnectionHandler processes a single request,
 * sends an appropriate response, then terminates the connection.
 */
public class ClientConnectionHandler implements Runnable {
    /** The Socket this connection uses */
    final private Socket socket;
    /** The directory the shared directory the server is using */
    final private File directory;
    /** The Reader for the client input */
    final private BufferedReader requestInput;
    /** The Writer for the server output */
    final private PrintWriter responseOutput;
    /** The client's IP address */
    final private String clientIP;
    /** The client's self-identified alias (appended with their IP) */
    final private String alias;

    /**
     * Constructor for ClientConnectionHandler. Stores the given paramters, establishes the input and output streams
     * for the connection, stores the client's IP, and reads in the first line of the request (which is the alias)
     *
     * @param socket The clientSocket that has been accepted by the server
     * @param directory The shared directory the server is using
     * @throws IOException when there is a failure to get the InputStream or OutputStream for the socket
     */
    public ClientConnectionHandler(Socket socket, File directory) throws IOException {
        // copy these parameters to instance fields
        this.socket = socket;
        this.directory = directory;

        // store the client's IP address for logging purposes
        this.clientIP = socket.getInetAddress().toString();

        // intialize the BufferedReader and PrintWriter that will be used to communicate with client
        requestInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        responseOutput = new PrintWriter(socket.getOutputStream());

        // The first line of any request is to be the alias for the client, store this appended with their IP
        this.alias = requestInput.readLine() + clientIP;

        // log the establishment of the connection
        log("Establishing connection with " + alias);
    }

    /**
     * Takes the given input message and copies it to both a 'log.txt' file in the root shared directory and the
     * console. The `log.txt` is prepended with a timestamp. If the file 'log.txt' is not found in the root shared
     * directory, it is created.
     *
     * @param message The message to log.
     */
    private void log(String message) {
        try {
            // create the File
            File file = new File("shared/log.txt");

            // initialize the PrintWriter variable
            PrintWriter output;
            synchronized (this) {
                // if there is already a log.txt file
                if (file.exists()) {
                    // initialize the PrintWriter to append to the existing file
                    output = new PrintWriter(new FileOutputStream(file, true));
                } else {
                    // initialise the PrintWriter to append to a new file
                    output = new PrintWriter(file);
                }

                // append the message to the file and close the PrintWriter
                output.append(String.valueOf(new Date())).append(": ").append(message).append("\r\n");
                output.close();
            }

        } catch(IOException e) {
            e.printStackTrace();
        }

        // print the message to the console
        System.out.println(message);
    }

    /**
     * This method is automatically invoked when the server Thread associated with "this" is started. It calls a handler
     * for the request, then closes the input and output Streams and the Socket.
     */
    public void run() {
        // call the helper to sort the request
        handleRequest();

        try {
            // close the InputStream, OutputStream, and socket connection
            requestInput.close();
            responseOutput.close();
            log("Terminating connection with " + alias);
            socket.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Responsible for sorting the request type and calling the appropriate handler(s). The handler or handlers
     * process the request and return the response. Concatenates the response and sends the complete response to the
     * client.
     */
    private void handleRequest() {
        // initialize the response
        String response = "";
        try {
            // read in the type of request - accepted are "DIR", "UPLOAD", "DOWNLOAD", and "DELETE"
            String request = requestInput.readLine();

            // log the type of request and client making the request
            log(request + " request from " + alias);

            // check for each of the accepted requests, and call appropriate handler
            if (request.regionMatches(true, 0, "dir", 0, 3)) {
                // do nothing (every request is always appended with a dir response at the end)
                // included for semantic and modularity reasons
            } else if (request.regionMatches(true, 0, "upload", 0, 6)) {
                response += handleUpload();
            } else if (request.regionMatches(true, 0, "download", 0, 8)) {
                response += handleDownload();
            } else if (request.regionMatches(true, 0, "delete", 0, 6)) {
                response += handleDelete();
            } else {
                response += handleUnknown(request);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        // append the response with a directory listing
        response += handleDir();

        // send the response to the client
        responseOutput.println(response);
        responseOutput.flush();
    }

    /**
     * This method returns a String of the following format:
     *
     * 201
     * <number of files listed, including the shared directory>
     * <the name of the shared directory>
     * <zero or more lines, each listing one filename in the shared directory>
     *
     * Note: All needed information from the client has already been parsed by the time this method is invoked
     *
     * @return A String as described above
     */
    private String handleDir() {
        // store a listing of the filenames
        String[] filenames = directory.list();

        // sort the list of filenames
        Arrays.sort(filenames);

        // initialize the response
        StringBuilder response = new StringBuilder("201\r\n");

        // append the number of filenames in the response (including shared directory)
        response.append(filenames.length + 1).append("\r\n");

        // append the shared directory name (ignoring the root folder part of the path)
        response.append(directory.getPath().substring(7)).append("/\r\n");

        // for each filename, append it to the response
        for (String filename : filenames) {
            response.append(filename).append("\r\n");
        }

        // log an appropriate message
        log("A listing of files in the shared directory was sent to " + alias);

        // return the response
        return response.toString();
    }

    /**
     * Reads in the client input and copies it to a new file on the server, using the client-provided target name and
     * updating the name if there is a filename conflict. Returns a String of the following format for successful
     * requests:
     *
     * 202
     * 0
     *
     * For unsuccessful requests, this method returns a String of the following format:
     *
     * 402
     * There was an error uploading <filename>
     *
     * Note: An upload request is of the following format:
     *
     * <alias> (already parsed)
     * UPLOAD (already parsed)
     * <target filename>
     * <zero or more lines of file content, text format>
     *
     * @return a String as described above
     */
    private String handleUpload() {
        // initialize the response
        String response = "";

        // the client-provided filename is going to be appended to the shared folder path, initialized here
        String filename = "shared/" + directory.getName() + "/";

        // initialize String to hold targetName, which may be needed for error reporting
        String targetName;

        // read the target filename
        try {
            targetName = requestInput.readLine();
        } catch (IOException e) {
            e.printStackTrace();

            // use default filename
            targetName = "newfile.txt";
        }

        // append the target filename to the path
        filename += targetName;
        // call the Utility to verify filename doesn't already exist, update if needed
        filename = Utils.getFilename(filename);

        // copy the remaining request to the new file
        try {
            // initialize output file
            PrintWriter output = new PrintWriter(filename);

            // initialize line variable
            String line;
            try {
                // only read input while there is still input to read
                while (requestInput.ready() && null != (line = requestInput.readLine())) {
                    // copy the read line to the file
                    output.println(line);
                }

                // copy successful
                response += "202\r\n0\r\n";

                // log the result of the request
                log(alias + " uploaded " + filename.substring(7) + ".");
            } catch(IOException e) {
                // copy unsuccessful
                response += "402\r\nThere was an error uploading '" + targetName + "' to the server.\r\n";
                log("There was an error uploading " + targetName  + " from " + alias + ".");
            } finally {
                // close the output file
                output.flush();
                output.close();
            }
        } catch(IOException e) {
            // copy unsuccessful
            response += "402\r\nThere was an error uploading '" + targetName + "' to the server.\r\n";

            // log the result of the request
            log("An error occurred when creating file named " + filename + ".");
        }

        // return the response
        return response;
    }

    /**
     * Copies the requested file to the client line-by-line. Returns a String of the following format for successful
     * requests:
     *
     * 203
     * <number of lines in the file>
     * <zero or more lines, each listing one line from the file>
     *
     * For unsuccessful requests, this method returns a String of the following format:
     *
     * 403
     * <A message describing the error>
     *
     * Note: A download request is of the following format:
     *
     * <alias> (already parsed)
     * DOWNLOAD (already parsed)
     * <target filename>
     *
     * @return a String as described above
     */
    private String handleDownload() {
        // initialize the response
        StringBuilder response = new StringBuilder();

        // initialize the variable to accept the filename from the request
        String filename;
        try {
            // read the filename from the client request
            filename = requestInput.readLine();
        } catch(IOException e) {
            // if we can't read the filename, we can't do the download
            response.append("403\r\nThere was an error parsing the download request.\r\n");

            // log an appropriate message recording the error
            log("There was an error parsing the download request from " + alias + ".");

            return response.toString();
        }

        // initialize the File that we will copy to the response
        String path = "shared/" + directory.getName() + "/" + filename;
        File file = new File(path);

        if (!file.exists()) {
            // the file doesn't exist, so there is nothing to download
            response.append("403\r\n'").append(filename).append("' was not found in the shared directory.\r\n");

            // log an appropriate message recording the error
            log("The requested file was not found in the shared directory.");
        } else {
            // read and copy file
            try {
                // initialize the file reader
                BufferedReader input = new BufferedReader(new FileReader(file));

                // initialize a counter for the number of lines of the file
                int numLines = 0;

                // initialize the variable to hold a single line of the file
                String line;

                // copy all lines of the file to the response and count each line
                while (null != (line = input.readLine())) {
                    response.append(line).append("\r\n");
                    numLines++;
                }
                // close the reader
                input.close();

                // prepend the metadata (response code, number of lines) to the response
                response.insert(0, "203\r\n" + numLines + "\r\n");

                // log the successful download
                log(directory.getName() + "/" + filename + " was sent to " + alias);
            } catch(IOException e) {
                // there was some error, prepend the metadata (response code, error message) to the response
                response.insert(0, "403\r\nThere was an error copying the file contents\r\n");
            }
        }
        // return the response
        return response.toString();
    }

    /**
     * Deletes the file indicated by the client. Returns a String of the following format for successful requests:
     *
     * 204
     * 0
     *
     * For unsuccessful requests, this method returns a String of the following format:
     *
     * 404
     * <A message describing the error>
     *
     * Note: A delete request is of the following format:
     *
     * <alias> (already parsed)
     * DOWNLOAD (already parsed)
     * <target filename>
     *
     * @return a String as described above
     */
    private String handleDelete() {
        // initialize the response
        String response = "";

        // initialize the variable to accept the filename from the request
        String filename;
        try {
            // read the filename from the client request
            filename = requestInput.readLine();
        } catch(IOException e) {
            // if we can't read the filename, we can't do the deletion
            response += "404\r\nThere was an error parsing the delete request.\r\n";

            // log an appropriate error message
            log("There was an error parsing the delete request from " + alias + ".");

            // return the response
            return response;
        }

        // initialize the File that we will delete
        String path = "shared/" + directory.getName() + "/" + filename;
        File file = new File(path);

        // if the file exists
        if (file.exists()) {
            // delete it
            file.delete();

            // log the successful deletion
            log(directory.getName() + "/" + filename + " was deleted.");

            // record the response
            response += ("204\r\n0\r\n");
        } else {
            // the file doesn't exist, log the appropriate messages
            log("The requested file was not found in the shared directory.");
            response += ("404\r\nThat file was already deleted from the server.\r\n");
        }

        // return the response
        return response;
    }

    /**
     * This method returns a String of the following format:
     *
     * 405
     * <request> is not a valid request type.
     *
     * @param request the unknown request from the client
     * @return a String as described above
     */
    private String handleUnknown(String request) {
        return "405\r\n'" + request + "' is not a valid request type.\r\n";
    }
}
