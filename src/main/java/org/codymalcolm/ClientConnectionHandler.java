package org.codymalcolm;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.NoSuchElementException;

public class ClientConnectionHandler implements Runnable {
    // TODO don't forget the synchronized stuff

    private Socket socket = null;
    private File directory = null;
    private BufferedReader requestInput = null;
    private PrintWriter responseOutput = null;
    private String clientIP = null;
    private String alias = null;

    public ClientConnectionHandler(Socket socket, File directory) throws IOException {
        this.socket = socket;
        this.directory = directory;
        this.clientIP = socket.getInetAddress().toString();

        log("Establishing connection with " + clientIP.substring(1));

        requestInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        responseOutput = new PrintWriter(socket.getOutputStream());
    }

    private void log(String message) {
        try {
            File file = new File("shared/log.txt");
            PrintWriter output = null;
            if (file.exists()) {
                output = new PrintWriter(new FileOutputStream(file, true));
            } else {
                output = new PrintWriter(file);
            }
            output.append(new Date() + ": " + message + "\r\n");
            output.close();

            System.out.println(message);

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        String line = null;
        try {
            this.alias = requestInput.readLine() + clientIP;
            handleRequest(requestInput.readLine());
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                requestInput.close();
                responseOutput.close();
                log("Terminating connection with " + alias);
                socket.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleRequest(String request) throws IOException {
        try {
            log(request + " request from " + alias);
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
        String filename = "shared/" + directory.getName() + "/";
        try {
            filename += requestInput.readLine();
        } catch(IOException e) {
            e.printStackTrace();
        }
        File file = new File(filename);
        if (file.exists()) {
            log(filename + " was deleted.");
            file.delete();
        } else {
            log("The requested file was not found in the shared directory.");
        }
        handleDir();
    }

    private void handleDownload() {
        try {
            String filename = requestInput.readLine();
            String path = "shared/" + directory.getName() + "/" + filename;
            File file = new File(path);
            if (!file.exists()) {
                sendResponse("404", "That file doesn't exist");
                log("The requested file was not found in the shared directory.");
                return;
            }
            String content = "";
            // read and copy file
            try {
                BufferedReader input = new BufferedReader(new FileReader(file));
                int numLines = 0;
                String line;

                while (null != (line = input.readLine())) {
                    content += line + "\r\n";
                    numLines++;
                }
                content = numLines + "\r\n" + content;
            } catch(IOException e) {
                e.printStackTrace();
            }
            content += ("201\r\n" + getDirectoryContents());
            sendResponse("203", content);
            log(filename + " was sent to " + alias);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private String getDirectoryContents() {
        String[] contents = directory.list();
        Arrays.sort(contents);
        String content = directory.getPath().substring(7) + "/\r\n";
        for (String c : contents) {
            content += c + "\r\n";
        }
        return content;
    }

    private void handleDir() {
        String responseCode = "201";
        String content = getDirectoryContents();
        try {
            sendResponse(responseCode, content);
            log("A listing of files in the shared directory was sent to " + alias);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleUpload() {
        String filename = "shared/" + directory.getName() + "/";
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
                log(alias + " uploaded " + new File(filename).getName() + ".");
            }
        } catch(IOException e) {
            e.printStackTrace();
            log("An error occurred when creating file named " + filename + ".");
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
