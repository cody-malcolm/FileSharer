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
        System.out.println(1);
        localFile = true;
        if (null != selectedFilename && localFile) {
            System.out.println(2);
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
        client.requestDelete();
    }

    public void setClient(FileSharerClient client) {
        this.client = client;
    }
}
