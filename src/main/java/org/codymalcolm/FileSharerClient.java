package org.codymalcolm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class FileSharerClient extends Application {
    private BufferedReader in;
    private PrintWriter out;
    private String hostname = "localhost";
//    private String uri;
    private int port = 9001;
    private Controller controller;
    private FileSharerClient client;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent root;
        try {
            root = loader.load();
            controller = loader.getController();
            // set up and show the Stage
            primaryStage.setTitle("File Sharer v1.0");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();

            client = new FileSharerClient();
            controller.setClient(client);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void requestUpload(String filename) {
        establishConnection();
        sendRequest("UPLOAD", filename);
        processResponse();
    }

    public void requestDownload() {
    }

    public void requestDirectory() {
        establishConnection();
        sendRequest("DIR");
        processResponse();
    }


    public void requestDelete(String filename) {
        establishConnection();
        sendRequest("DELETE", filename);
        processResponse();
    }

    private void processResponse() {
        System.out.println("Response");
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

    private void establishConnection() {
        try {
            Socket socket = new Socket(hostname, port);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());

        } catch(IOException e) {
            e.printStackTrace();
        }
    }


}
