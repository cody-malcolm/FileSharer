package org.codymalcolm;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class FileSharerServerHandler implements Runnable {

    private Socket socket = null;
    private BufferedReader requestInput = null;
    private DataOutputStream responseOutput = null;

    public FileSharerServerHandler(Socket socket) throws IOException {
        this.socket = socket;

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
            StringTokenizer tokenizer = new StringTokenizer(request);
            String command = tokenizer.nextToken(); // GET or POST
            String arg = null;

            if (tokenizer.hasMoreTokens()) {
                arg = tokenizer.nextToken();
            }

            if (command.equalsIgnoreCase("dir")) {
                sendResponse("200", command, "Request to get dir".getBytes());
            } else if (command.equalsIgnoreCase("upload")) {
                sendResponse("200", command, ("Request to upload " + arg).getBytes());
            } else if (command.equalsIgnoreCase("download")) {
                sendResponse("200", command, ("Request to download " + arg).getBytes());
            } else {
                sendError(405, "Method Not Allowed", "'" + command + "' is not a valid " +
                        "command on this server.");
            }

        } catch(NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    private void sendFile(File baseDir, String uri) throws IOException {

    }

    private void sendResponse(String responseCode, String contentType, byte[] content)
            throws IOException {
        responseOutput.writeBytes(responseCode + "\r\n");

        responseOutput.writeBytes("Content-Type: " + contentType + "\r\n");
        responseOutput.writeBytes("Date: " + (new Date()) + "\r\n");
        responseOutput.writeBytes("Server: Example-http-Server v1.0.0\r\n");
        responseOutput.writeBytes("Content-Length: " + content.length + "\r\n");
        responseOutput.writeBytes("Connection: Close\r\n\r\n");

        responseOutput.write(content);
        responseOutput.flush();
    }

    private void sendError(int errorCode, String errorMessage, String description)
            throws IOException {
        sendResponse("405", errorMessage, description.getBytes());
    }
}
