package org.codymalcolm;

import javafx.scene.input.MouseEvent;

public class Controller {
    FileSharerClient client;

    public void upload(MouseEvent mouseEvent) {
        client.requestUpload();
    }

    public void download(MouseEvent mouseEvent) {
        client.requestDownload();
    }

    public void dir(MouseEvent mouseEvent) {
        client.requestDirectory();
    }

    public void setClient(FileSharerClient client) {
        this.client = client;
    }
}
