package org.codymalcolm;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.NoSuchElementException;

public class FileSharerServerHandler implements Runnable {
    // TODO don't forget the synchronized stuff

    private Socket socket = null;
    private File directory = null;
    private BufferedReader requestInput = null;
    private PrintWriter responseOutput = null;

    public FileSharerServerHandler(Socket socket, File directory) throws IOException {
        this.socket = socket;
        this.directory = directory;

        requestInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        responseOutput = new PrintWriter(socket.getOutputStream());
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
            if (request.regionMatches(true, 0, "dir", 0, 3)) {
                handleDir();
            } else if (request.regionMatches(true, 0, "upload", 0, 6)) {
                handleUpload();
            } else if (request.regionMatches(true, 0, "download", 0, 8)) {
                handleDownload();
            } else if (request.regionMatches(true, 0, "delete", 0, 6)) {
                handleDelete();
            }else {
                sendError("405", "'" + request.split("\\s")[0] + "' is not a valid " +
                        "command on this server.");
            }

        } catch(NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete() {
        // TODO All requests with filenames need to have the filename verified for existence
        String filename = directory.getName() + "/";
        try {
            filename += requestInput.readLine();
        } catch(IOException e) {
            e.printStackTrace();
        }
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
        handleDir();
    }

    private void handleDownload() {
        try {
            String filename = directory.getName() + "/" + requestInput.readLine();
            File temp = new File(filename);
            if (!temp.exists()) {
                sendResponse("404", "That file doesn't exist");
                return;
            }
            String content = "";
            // read and copy file
            try {
                BufferedReader input = new BufferedReader(new FileReader(temp));

                String line;

                while (null != (line = input.readLine())) {
                    content += line + "\r\n";
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
            sendResponse("201", content);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDir() {
        String responseCode = "201";
        String[] contents = directory.list();
        Arrays.sort(contents);
        String content = directory.getPath() + "\r\n";
        for (String c : contents) {
            content += c + "\r\n";
        }
        try {
            sendResponse(responseCode, content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleUpload() {
        String filename = directory.getName() + "/";
        try {
            String name = requestInput.readLine();
            if (name != "") {
                filename += name;
            } else {
                filename += "newfile.txt";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        filename = Utils.getFilename(filename);

        try {
            PrintWriter output = new PrintWriter(filename);
            String line = null;
            try {
                while (requestInput.ready() && null != (line = requestInput.readLine())) {
                    output.println(line);
                }
            } catch(IOException e) {
                e.printStackTrace();
            } finally {
                output.flush();
                output.close();
            }
        } catch(IOException e) {
            e.printStackTrace();
            System.out.println("An error occurred when creating file named " + filename + " on the server-side");
        }
        handleDir();
    }

    private void sendResponse(String responseCode, String content)
            throws IOException {
        responseOutput.println(responseCode);
        responseOutput.println(content);
        responseOutput.flush();
    }

    private void sendError(String errorCode, String description)
            throws IOException {
        sendResponse(errorCode, description);
    }
}
