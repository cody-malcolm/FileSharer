package org.codymalcolm;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;

import java.io.File;


public class Controller {
    @FXML
    private Label localDirectory;
    @FXML
    private TreeItem<String> localDirectoryName;

    private String initialLocalDirectory = null;
    private FileSharerClient client = null;
    private String selectedFilename = null;
    private boolean localFile = false;
    private boolean serverFile = false;

    public void initialize() {

    }

    public void setInitialLocalDirectory(String d) {
        initialLocalDirectory = d;
    }

    public void setup() {
        localDirectory.setText(initialLocalDirectory);
    }

    public void refreshLocal() {
        String directory = localDirectory.getText();
        selectedFilename = directory + "test.txt";
        localDirectoryName.setValue(directory);
        File dir = new File(directory);
        for (String filename : dir.list()) {
            TreeItem<String> entry = new TreeItem<>();
            entry.setValue(filename);
            localDirectoryName.getChildren().add(entry);
        }
    }

    public void upload(ActionEvent actionEvent) {
        localFile = true; // TODO this needs to be implemented eventually
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
}
