package org.codymalcolm;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.io.*;
import java.util.Arrays;

public class Controller {
    @FXML
    private Label localDirectory;
    @FXML
    private TreeItem<String> localDirectoryName;
    @FXML
    private TreeItem<String> serverDirectoryName;
    @FXML
    private Label preview;
    @FXML
    private ScrollPane previewPane;
    @FXML
    private Label previewInstructions;
    @FXML
    private VBox previewInstructionsContainer;
    @FXML
    private VBox previewContainer;

    private String initialLocalDirectory = null;
    private FileSharerClient client = null;
    private String selectedFilename = null;
    private boolean localFile = false;
    private boolean serverFile = false;

    public void initialize() {
        System.out.println("setting up controller");
    }

    public void setInitialLocalDirectory(String d) {
        initialLocalDirectory = d;
    }

    public void setup() {
        localDirectory.setText(initialLocalDirectory);
        previewContainer.getChildren().remove(1);
    }

    public void refreshLocal() {
        localDirectoryName.getChildren().clear();
        String directory = localDirectory.getText();
        localDirectoryName.setValue(directory);
        File dir = new File(directory);
        String[] filenames = dir.list();
        Arrays.sort(filenames);
        for (String filename : filenames) {
            TreeItem<String> entry = new TreeItem<>();
            entry.setValue(filename);
            localDirectoryName.getChildren().add(entry);
        }
    }

    public void upload(ActionEvent actionEvent) {
        System.out.println(selectedFilename);
        System.out.println(localFile);
        if (null != selectedFilename && localFile) {
            client.requestUpload(selectedFilename);
        }
    }

    public void download(ActionEvent actionEvent) {
        if (null != selectedFilename && serverFile) {
            client.requestDownload(selectedFilename, localDirectory.getText());
        }
    }

    public void dir(ActionEvent actionEvent) {
        System.out.println(this);
        client.requestDirectory();
    }

    public void delete(ActionEvent actionEvent) {
        if (null != selectedFilename && serverFile) {
            client.requestDelete(selectedFilename);
        }
    }

    public void setClient(FileSharerClient client) {
        this.client = client;
    }

    private String parseTreeSelection(String response) {
        int start = response.indexOf("text=") + 6; // will be 5 if "text=" was not found
        int end;
        // if you click near the edge of the TreeItem, temp will be of the form "...]'<name>'", so "text=" is not found
        if (start == 5) {
            start = response.indexOf("]'") + 2;
            end = response.indexOf('\'', start);
        } else {// if you click the TreeItem directly, temp will be of the form: "Text[text="<name>", ...]"
            end = response.indexOf('"', start);
        }
        return response.substring(start, end);
    }

    public void handleLocalTreeClick(MouseEvent mouseEvent) {
        String filename = localDirectory.getText() + parseTreeSelection(mouseEvent.getTarget().toString());

        File temp = new File(filename);
        if (temp.exists()) {
            selectedFilename = filename;
            localFile = true;
            serverFile = false;

            String previewText = "";
            try {
                BufferedReader reader = new BufferedReader(new FileReader(temp));

                String line;
                while (null != (line = reader.readLine())) {
                    previewText += line + "\r\n";
                }
                reader.close();
            } catch(IOException e) {
                e.printStackTrace();
            }

            preview.setText(previewText);
            previewContainer.getChildren().remove(1);
            previewContainer.getChildren().add(previewPane);
        } else {
            selectedFilename = null;
            localFile = false;
            serverFile = false;
            previewContainer.getChildren().remove(1);
            previewContainer.getChildren().add(previewInstructionsContainer);
        }
    }

    public void handleServerTreeClick(MouseEvent mouseEvent) {
        String filename = parseTreeSelection(mouseEvent.getTarget().toString());
        selectedFilename = filename;
        localFile = false;
        serverFile = true;
    }

    public void updateServerDirectory(String sharedDirectoryName) {
        serverDirectoryName.setValue(sharedDirectoryName);
    }

    public void addServerFileListing(String filename) {
        TreeItem<String> entry = new TreeItem<>();
        entry.setValue(filename);
        serverDirectoryName.getChildren().add(entry);
    }

    public void clearServerTree() {
        serverDirectoryName.getChildren().clear();
    }
}
