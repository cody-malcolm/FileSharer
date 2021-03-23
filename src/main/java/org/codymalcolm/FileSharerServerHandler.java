package org.codymalcolm;

import java.io.*;
import java.net.Socket;
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

    private void handleDir() {
        String responseCode = "201";
        String[] contents = directory.list();
        String content = "";
        for (String c : contents) {
            content += c + "\r\n";
        }
        try {
            sendResponse(responseCode, content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleUpload(String request) {
        String filename = directory.getPath() + "/" + request.split("\\s")[1];
        File temp = new File(filename);
        int i = 1;
        while (temp.exists()) {
            if (i == 1) {
                filename = incrementIteration(filename);
            } else {
                filename = incrementIteration(filename, i);
            }
            temp = new File(filename);
            i++;
        }

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

    private String incrementIteration(String oldFilename) {
        int extensionStartsAt = oldFilename.lastIndexOf('.');
        String extension = oldFilename.substring(extensionStartsAt);
        return oldFilename.substring(0, extensionStartsAt) + "(1)" + extension;
    }

    private String incrementIteration(String oldFilename, int i) {
        int iterationStartsAt = oldFilename.lastIndexOf('(')+1;
        int iterationEndsAt = oldFilename.lastIndexOf(')');
        String filename = oldFilename.substring(0, iterationStartsAt);
        if (Integer.parseInt(oldFilename.substring(iterationStartsAt, iterationEndsAt))+1 == i) {
            filename += i;
        } else {
            filename += oldFilename.substring(iterationStartsAt, iterationEndsAt);
        }
        filename += oldFilename.substring(iterationEndsAt);
        return filename;
    }

    private void sendFile(File baseDir, String uri) throws IOException {

    }

    private void sendResponse(String responseCode, String content)
            throws IOException {
        responseOutput.println(responseCode);
        responseOutput.println(content);
        responseOutput.flush();
    }

    private void sendError(String errorCode, String description)
            throws IOException {
        sendResponse("405", description);
    }
}
