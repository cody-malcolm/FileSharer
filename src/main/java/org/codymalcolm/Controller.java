package org.codymalcolm;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;

import java.io.File;


public class Controller {
    @FXML
    private Label localDirectory;
    @FXML
    private TreeItem<String> localDirectoryName;
    @FXML
    private TreeItem<String> serverDirectoryName;

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
    }

    public void refreshLocal() {
        String directory = localDirectory.getText();
        localDirectoryName.setValue(directory);
        File dir = new File(directory);
        for (String filename : dir.list()) {
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
        serverFile = true; // TODO this needs to be implemented eventually
        if (null != selectedFilename && serverFile) {
            client.requestDownload(selectedFilename, localDirectory.getText());
        }
    }

    public void dir(ActionEvent actionEvent) {
        System.out.println(this);
        client.requestDirectory();
    }

    public void delete(ActionEvent actionEvent) {
        serverFile = true; // TODO this needs to be implemented eventually
        if (null != selectedFilename && serverFile) {
            client.requestDelete(selectedFilename);
        }
    }

    public void setClient(FileSharerClient client) {
        this.client = client;
    }

    public void handleLocalTreeClick(MouseEvent mouseEvent) {
        String temp = mouseEvent.getTarget().toString();
        int start = temp.indexOf("text=") + 6; // will be 5 if "text=" was not found
        int end;
        // if you click near the edge of the TreeItem, temp will be of the form "...]'<name>'", so "text=" is not found
        if (start == 5) {
            start = temp.indexOf("]'") + 2;
            end = temp.indexOf('\'', start);
        } else {// if you click the TreeItem directly, temp will be of the form: "Text[text="<name>", ...]"
            end = temp.indexOf('"', start);
        }
        String filename = localDirectory.getText() + temp.substring(start, end);
        File testing = new File(filename);
        if (testing.exists()) {
            selectedFilename = filename;
            localFile = true;
            serverFile = false;
        } else {
            selectedFilename = null;
            localFile = false;
            serverFile = false;
        }
    }

    public void handleServerTreeClick(MouseEvent mouseEvent) {
        String temp = mouseEvent.getTarget().toString();
        int start = temp.indexOf("text=") + 6; // will be 5 if "text=" was not found
        int end;
        // if you click near the edge of the TreeItem, temp will be of the form "...]'<name>'", so "text=" is not found
        if (start == 5) {
            start = temp.indexOf("]'") + 2;
            end = temp.indexOf('\'', start);
        } else {// if you click the TreeItem directly, temp will be of the form: "Text[text="<name>", ...]"
            end = temp.indexOf('"', start);
        }
        String filename = localDirectory.getText() + temp.substring(start, end);
        File testing = new File(filename);
        if (testing.exists()) {
            selectedFilename = filename;
            localFile = true;
            serverFile = false;
        } else {
            selectedFilename = null;
            localFile = false;
            serverFile = false;
        }
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
