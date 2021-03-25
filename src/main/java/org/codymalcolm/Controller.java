package org.codymalcolm;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

// TODO Implement the rest of the optional arguments
// TODO Implement full refresh on both upload and download
// TODO Implement manual filename selection
// TODO Implement measures to protect against race condition
public class Controller {
    /** the label where the directory is displayed/updated */
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
    private VBox previewInstructionsContainer;
    @FXML
    private VBox previewContainer;
    @FXML
    private Label feedback;
    @FXML
    private TextField customFilename;

    private String initialLocalDirectory = null;
    private FileSharerClient client = null;
    private String selectedFilename = null;
    private boolean localFile = false;
    private boolean serverFile = false;
    private Stage primaryStage;

    public void setPrimaryStage(Stage s) {
        primaryStage = s;
    }

    public void initialize() {
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
            if (!new File(filename).isDirectory()) {
                TreeItem<String> entry = new TreeItem<>();
                entry.setValue(filename);
                localDirectoryName.getChildren().add(entry);
            }
        }
    }

    public void upload(ActionEvent actionEvent) {
        if (null != selectedFilename && localFile) {
            client.requestUpload(selectedFilename, customFilename.getText());
        } else {
            if (null == selectedFilename) {
                giveFeedback("No file has been selected!", false);
            } else {
                giveFeedback("To upload, you must select a local file.", false);
            }
        }
    }

    protected void giveFeedback(String s, boolean key) {
        feedback.setText(s);
        feedback.getStyleClass().clear();
        feedback.getStyleClass().add((key ? "successMessage" : "failureMessage"));
    }

    public void download(ActionEvent actionEvent) {
        if (null != selectedFilename && serverFile) {
            client.requestDownload(selectedFilename, localDirectory.getText(), customFilename.getText());
        } else {
            if (null == selectedFilename) {
                giveFeedback("No file has been selected!", false);
            } else {
                giveFeedback("To download, you must select a server file.", false);
            }
        }
    }

    public void delete(ActionEvent actionEvent) {
        if (null != selectedFilename && serverFile) {
            client.requestDelete(selectedFilename);
        } else {
            if (null == selectedFilename) {
                giveFeedback("No file has been selected!", false);
            } else {
                giveFeedback("To delete a local file, use your OS file manager.", false);
            }
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
//        for (TreeItem<String> i : serverDirectoryName.getChildren()) {
//            // TODO remove selected pseudoclass from all server files
//        }
    }

    public void handleServerTreeClick(MouseEvent mouseEvent) {
        String filename = parseTreeSelection(mouseEvent.getTarget().toString());
        selectedFilename = filename;
        localFile = false;
        serverFile = true;

        previewContainer.getChildren().remove(1);

        String filePreview = client.requestPreview(filename);
        if (!"".equals(filePreview)) {
            preview.setText(filePreview);
            previewContainer.getChildren().add(previewPane);
        } else {
            previewContainer.getChildren().add(previewInstructionsContainer);
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

    /**
     * Allows the user to select a directory to get testing/training data from. Updates the directory Label text.
     *
     * @param mouseEvent not used
     */
    public void chooseDirectory(MouseEvent mouseEvent) {
        // initialize a DirectoryChooser
        DirectoryChooser directoryChooser = new DirectoryChooser();

        // set the initial directory to show the current selection
        directoryChooser.setInitialDirectory(new File(new File(localDirectory.getText()).getParent()));

        // update the text of the directory Label to the new chosen directory
        File chosenDirectory = directoryChooser.showDialog(primaryStage);
        if (null != chosenDirectory) {
            localDirectory.setText(chosenDirectory.getPath() + "/");
        }
        refreshLocal();
    }
}
