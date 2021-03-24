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
//    private String uri;
    private int port = 9001;
    private Controller controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        List<String> parameters = getParameters().getRaw();
        if (parameters.isEmpty()) {
            System.out.println("No local directory provided");
            System.exit(0);
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
            controller.setClient(this);
            controller.setInitialLocalDirectory(parameters.get(0));
            controller.setup();
            controller.refreshLocal();
            requestDirectory();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void requestUpload(String filename) {
        boolean connected = establishConnection();
        if (connected) {
            sendRequest("UPLOAD", filename);
            processDirectoryResponse();
        } else {
            System.out.println("A connection was not established."); // TODO output these to UI
        }
    }

    public void requestDownload(String filename, String localDirectory) {
        boolean connected = establishConnection();
        if (connected) {
            sendRequest("DOWNLOAD", filename);
            processDownload(filename, localDirectory);
        } else {
            System.out.println("A connection was not established.");
        }
    }


    public void requestDirectory() {
        boolean connected = establishConnection();
        if (connected) {
            sendRequest("DIR");
            processDirectoryResponse();
        } else {
            System.out.println("A connection was not established.");
        }
    }


    public void requestDelete(String filename) {
        boolean connected = establishConnection();
        if (connected) {
            sendRequest("DELETE", filename);
            processResponse();
        } else {
            System.out.println("A connection was not established.");
        }
    }

    private void processDownload(String filename, String localDirectory) {
        String line;
        try {
            if ((line = in.readLine()).equals("201")) {
                filename = Utils.getFilename(filename);
                PrintWriter writer = new PrintWriter(new File(filename));
                while(in.ready() && (null != (line = in.readLine()))) {
                    writer.println(line);
                }
                writer.flush();
                writer.close();
            } else {
                System.out.println("Did not receive the file");
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

    private void processResponse() {
        String line;
        try {
            while (null != (line = in.readLine())) {
                System.out.println(line);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRequest(String type) {
        out.println(type);
        out.flush();
    }

    private void sendRequest(String type, String filename) {
        // TODO add a code to indicate successful file read
        // TODO don't forget about host machine alias
        out.println(type);

        File file = new File(filename);
        out.println(file.getName());

        if (type == "UPLOAD") {
            // read and copy file
            try {
                BufferedReader input = new BufferedReader(new FileReader(file));

                String line;

                while (null != (line = input.readLine())) {
                    out.println(line);
                }
            } catch(IOException e) {
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
