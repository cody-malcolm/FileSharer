package org.codymalcolm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class FileSharerClient extends Application {
    private BufferedReader in;
    private PrintWriter out;
    private String hostname = "localhost";
    private int port = 9001;
    private Controller controller;
    private String alias;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        List<String> parameters = getParameters().getRaw();
        int numParams = parameters.size();
        if (numParams < 2) {
            System.out.println("Note: Usage is 'gradle run --args=\"<alias> <local-directory>\"'. Aborting startup.");
            System.exit(0);
        }

        if (numParams >= 3) {
            hostname = parameters.get(2);
        }

        if (numParams > 3) {
            try {
                port = Integer.parseInt(parameters.get(3));
            } catch(NumberFormatException e) {
                System.out.println("Note: Please not, correct usage is gradle run --args=\"<alias> <local-directory> <hostname> <port>\"");
                System.out.println("The port was not understood, using default.");
            }
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent root;
        try {
            root = loader.load();
            controller = loader.getController();
            // set up and show the Stage
            primaryStage.setTitle("File Sharer v1.0");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
            controller.setPrimaryStage(primaryStage);
            controller.setClient(this);
            controller.setInitialLocalDirectory(parameters.get(1));
            controller.setup();
            alias = parameters.get(0);
            requestDirectory();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void requestUpload(String filename, String targetName) {
        boolean connected = establishConnection();
        if (connected) {
            sendRequest("UPLOAD", filename, targetName);
            processUploadResponse(filename);
            processDirectoryResponse();
        }
    }

    private void processUploadResponse(String filename) {
        try {
            if (in.readLine().equals("202")) {
                controller.giveFeedback("'" + new File(filename).getName() + "' was uploaded successfully.", true);
                in.readLine(); // discard next line
            } else {
                controller.giveFeedback(in.readLine(), false);
            }
        } catch(IOException e) {
            controller.giveFeedback("There was an error parsing the response from the server", false);
            e.printStackTrace();
        }
    }

    public void requestDownload(String filename, String localDirectory, String targetName) {
        boolean connected = establishConnection();
        if (connected) {
            sendRequest("DOWNLOAD", filename, "");
            processDownloadResponse(("".equals(targetName) ? filename : targetName), localDirectory);
            processDirectoryResponse(filename);
        }
    }

    public String requestPreview(String filename) {
        boolean connected = establishConnection();
        if (connected) {
            sendRequest("DOWNLOAD", filename, "");
            String response = processPreviewResponse();
            processDirectoryResponse(filename);
            return response;
        } else {
            return "";
        }
    }

    public void requestDirectory() {
        boolean connected = establishConnection();
        if (connected) {
            sendRequest("DIR");
            processDirectoryResponse();
        }
    }

    public void requestDelete(String filename) {
        boolean connected = establishConnection();
        if (connected) {
            sendRequest("DELETE", filename, "");
            processDeleteResponse(filename);
            processDirectoryResponse();
        }
    }

    private void processDeleteResponse(String filename) {
        try {
            if (in.readLine().equals("204")) {
                controller.giveFeedback("'" + filename + "' was deleted successfully.", true);
                in.readLine(); // discard next line
            } else {
                controller.giveFeedback(in.readLine(), false);
            }
        } catch(IOException e) {
            controller.giveFeedback("There was an error parsing the response from the server", false);
            e.printStackTrace();
        }
    }

    // s/b good
    private String processPreviewResponse() {
        String response = "";
        String line;

        try {
            if ((line = in.readLine()).equals("203")) {
                int numLines = Integer.parseInt(in.readLine());
                int i = 0;
                while((i++ < numLines) && in.ready() && (null != (line = in.readLine()))) {
                    response += line + "\r\n";
                }
                return response;
            } else if ("403".equals(line)) {
                controller.giveFeedback(in.readLine(), false);
                return response;
            }
        } catch(IOException e) {
            e.printStackTrace();
            return response;
        }
        return response;
    }

    // s/b good
    private void processDownloadResponse(String filename, String localDirectory) {
        String line;
        try {
            if ((line = in.readLine()).equals("203")) {
                filename = Utils.getFilename(localDirectory + filename);
                int numLines = Integer.parseInt(in.readLine());
                int i = 0;
                PrintWriter writer = new PrintWriter(new File(filename));
                while((i++ < numLines)  && in.ready() && (null != (line = in.readLine()))) {
                    writer.println(line);
                }
                writer.flush();
                writer.close();
                controller.giveFeedback("'" + new File(filename).getName() + "' was successfully downloaded.", true);
            } else if ("403".equals(line)) {
                controller.giveFeedback(in.readLine() , false);
                controller.giveFeedback("There was an error trying to download " + filename, false);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    // dir responses s/b good
    private void processDirectoryResponse() {
        processDirectoryResponse("");
    }

    private void processDirectoryResponse(String selectedFilename) {
        try {
            if ("201".equals(in.readLine())) {
                String line;
                in.readLine(); // discard number of files, not used in current implementation (maybe later?)
                controller.clearServerTree();
                String sharedDirectoryName = (in.readLine());
                controller.updateServerDirectory(sharedDirectoryName);
                while (!"".equals(line = in.readLine())) {
                    controller.addServerFileListing(line);
                }
                if (!"".equals(selectedFilename)) {
                    controller.highlightServerFile(selectedFilename);
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    // ONLY USED FOR DIR
    private void sendRequest(String type) {
        out.println(alias);
        out.println(type);
        out.flush();
    }

    // Used for UPLOAD, DOWNLOAD, DELETE
    private void sendRequest(String type, String filename, String targetName) {
        out.println(alias);
        out.println(type);

        File file = new File(filename);
        if ("".equals(targetName)) {
            out.println(file.getName());
        } else {
            out.println(targetName);
        }

        if (type == "UPLOAD") {
            // read and copy file
            try {
                BufferedReader input = new BufferedReader(new FileReader(file));

                String line;

                while (null != (line = input.readLine())) {
                    out.println(line);
                }
                controller.giveFeedback("Uploaded '" + filename + "' successfully!", true);
            } catch(IOException e) {
                controller.giveFeedback("Something went wrong reading the file.", false);
                e.printStackTrace();
            }
        }
        out.flush();
    }

    private boolean establishConnection() {
        try {
            Socket socket = new Socket(hostname, port);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
            controller.giveFeedback("", true);
            return true;

        } catch(IOException e) {
            controller.clearServerTree();
            controller.giveFeedback("A connection was not established.", false);
            return false;
        }
    }

}
