package org.codymalcolm;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;


public class Controller {
    @FXML
    private Label localDirectory;

    private FileSharerClient client = null;
    private String selectedFilename = null;
    private boolean localFile = false;
    private boolean serverFile = false;

    public void initialize() {
        selectedFilename = localDirectory.getText() + "test.txt";
    }

    public void upload(ActionEvent actionEvent) {
        localFile = true; // TODO this needs to be implemented eventually
        if (null != selectedFilename && localFile) {
            client.requestUpload(selectedFilename);
        }
    }

    public void download(ActionEvent actionEvent) {
        client.requestDownload();
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
