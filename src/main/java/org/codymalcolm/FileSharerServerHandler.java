package org.codymalcolm;

import java.io.*;
import java.net.Socket;
import java.util.NoSuchElementException;

public class FileSharerServerHandler implements Runnable {

    private Socket socket = null;
    private File directory = null;
    private BufferedReader requestInput = null;
    private DataOutputStream responseOutput = null;

    public FileSharerServerHandler(Socket socket, File directory) throws IOException {
        this.socket = socket;
        this.directory = directory;

        requestInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        responseOutput = new DataOutputStream(socket.getOutputStream());
    }

    public void run() {
        String line = null;
        try {
            line = requestInput.readLine();
            handleRequest(line);
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                requestInput.close();
                responseOutput.close();
                socket.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleRequest(String request) throws IOException {
        try {
            if (request.regionMatches(true, 0, "dir ", 0, 4)) {
                sendResponse("200", "Request to get dir".getBytes());
            } else if (request.regionMatches(true, 0, "upload ", 0, 7)) {
                handleUpload(request);
            } else if (request.regionMatches(true, 0, "download  ", 0, 9)) {
            } else if (request.regionMatches(true, 0, "delete  ", 0, 7)) {
            }else {
                sendError("405", "'" + request.split("\\s")[0] + "' is not a valid " +
                        "command on this server.");
            }

        } catch(NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    private void handleUpload(String request) {
        String filename = directory.getPath() + "/" + request.split("\\s")[1];
        // TODO check for file name conflicts on server side
        try {
            PrintWriter output = new PrintWriter(filename);
            String line = null;
            try {
                while (null != (line = requestInput.readLine())) {
                    output.println(line);
                    output.flush();
                }
            } catch(IOException e) {
                e.printStackTrace();
            } finally {
                output.close();
            }
        } catch(IOException e) {
            e.printStackTrace();
            System.out.println("An error occurred when creating file named " + filename + " on the server-side");
        }
    }

    private void sendFile(File baseDir, String uri) throws IOException {

    }

    private void sendResponse(String responseCode, byte[] content)
            throws IOException {
        responseOutput.writeBytes(responseCode + "\r\n");
//        responseOutput.writeBytes("Date: " + (new Date()) + "\r\n");
//        responseOutput.writeBytes("Server: Example-http-Server v1.0.0\r\n");
//        responseOutput.writeBytes("Content-Length: " + content.length + "\r\n");
        responseOutput.writeBytes("Connection: Close\r\n\r\n");

        responseOutput.write(content);
        responseOutput.flush();
    }

    private void sendError(String errorCode, String description)
            throws IOException {
        sendResponse("405", description.getBytes());
    }
}
