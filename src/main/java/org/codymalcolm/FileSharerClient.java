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
    private String hostname = "localhost"; // my IP "104.158.13.126"
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
            processDirectoryResponse();
        } else {
            controller.giveFeedback("A connection was not established.", false);
        }
    }

    public void requestDownload(String filename, String localDirectory, String targetName) {
        boolean connected = establishConnection();
        if (connected) {
            sendRequest("DOWNLOAD", filename, "");
            processDownload(("".equals(targetName) ? filename : targetName), localDirectory);
        } else {
            controller.giveFeedback("A connection was not established.", false);
        }
    }

    public String requestPreview(String filename) {
        boolean connected = establishConnection();
        if (connected) {
            sendRequest("DOWNLOAD", filename, "");
            return processPreview(filename);
        } else {
            return "";
        }
    }

    public void requestDirectory() {
        boolean connected = establishConnection();
        if (connected) {
            sendRequest("DIR");
            processDirectoryResponse();
        } else {
            controller.giveFeedback("A connection was not established.", false);
        }
    }

    public void requestDelete(String filename) {
        boolean connected = establishConnection();
        if (connected) {
            sendRequest("DELETE", filename, "");
            processDirectoryResponse();
            controller.highlightServerFile("");
        } else {
            controller.giveFeedback("A connection was not established.", false);
        }
    }

    private String processPreview(String filename) {
        String response = "";
        String line;

        try {
            if ((line = in.readLine()).equals("203")) {
                int numLines = Integer.parseInt(in.readLine());
                int i = 0;
                while((i++ < numLines) && in.ready() && (null != (line = in.readLine()))) {
                    response += line + "\r\n";
                }
                processDirectoryResponse();
                controller.highlightServerFile(filename);
                return response;
            } else if ("404".equals(line)) {
                line = in.readLine(); // discard "1"
                controller.giveFeedback(in.readLine() , false);
                processDirectoryResponse();
                controller.highlightServerFile("");
                return response;
            } else {
                return response;
            }
        } catch(IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void processDownload(String filename, String localDirectory) {
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
                processDirectoryResponse();

                controller.highlightServerFile(new File(filename).getName());
            } else if ("404".equals(line)) {
                line = in.readLine(); // discard "1"
                controller.giveFeedback(in.readLine() , false);
                processDirectoryResponse();
                controller.highlightServerFile("");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void processDirectoryResponse() {
        String line;
        try {
            if ("201".equals(line = in.readLine())) {
                controller.clearServerTree();
                String sharedDirectoryName = (line = in.readLine());
                controller.updateServerDirectory(sharedDirectoryName);
                while (!"".equals(line = in.readLine())) {
                    controller.addServerFileListing(line);
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }


    private void sendRequest(String type) {
        out.println(alias);
        out.println(type);
        out.flush();
    }

    private void sendRequest(String type, String filename, String targetName) {
        // TODO add a code to indicate successful file read
        out.println(alias);
        out.println(type);

        File file = new File(filename);
        out.println(("".equals(targetName) ? file.getName() : targetName));

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
            return true;

        } catch(IOException e) {
            return false;
        }
    }

}
