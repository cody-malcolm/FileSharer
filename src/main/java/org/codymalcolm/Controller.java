package org.codymalcolm;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

// TODO Implement measures to protect against race condition
// TODO Upload should stay highlighted
// TODO Download should stay highlighted, download needs to give feedback on client
// TODO Delete needs to give feedback on client
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
    @FXML
    private TreeView<String> localTreeView;
    @FXML
    private TreeView<String> serverTreeView;

    private String initialLocalDirectory = null;
    private FileSharerClient client = null;
    private String selectedFilename = null;
    private boolean localFile = false;
    private boolean serverFile = false;
    private Stage primaryStage;
    private SelectionModel localSelectionModel;
    private SelectionModel serverSelectionModel;

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
        localSelectionModel = localTreeView.getSelectionModel();
        serverSelectionModel = serverTreeView.getSelectionModel();
        refreshLocal();
    }

    private void refreshLocal(String filename) {
        refreshLocal();
        if (null != filename) {
            String selected = new File(filename).getName();
            localDirectoryName.getChildren().forEach(new Consumer<TreeItem<String>>() {
                @Override
                public void accept(TreeItem<String> stringTreeItem) {
                    if(stringTreeItem.getValue().equals(selected)) {
                        localSelectionModel.select(localTreeView.getRow(stringTreeItem));
                    };
                }
            });
        }
    }

    private void refreshLocal() {
        localSelectionModel.clearSelection();

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
        refreshLocal(selectedFilename);
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
        refreshLocal();
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
        refreshLocal();
        serverSelectionModel.clearSelection();
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
        serverSelectionModel.clearSelection();
        String path = localDirectory.getText() + parseTreeSelection(mouseEvent.getTarget().toString());
        File temp = new File(path);
        String filename = null;
        if (temp.exists()) {
            selectedFilename = path;
            localFile = true;
            serverFile = false;
            filename = temp.getName();

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
        client.requestDirectory();
        refreshLocal(filename);
    }

    public void handleServerTreeClick(MouseEvent mouseEvent) {
        String filename = parseTreeSelection(mouseEvent.getTarget().toString());
        selectedFilename = filename;
        localFile = false;
        serverFile = true;

        ObservableList<Node> previewNodes = previewContainer.getChildren();
        if (previewNodes.size() > 1) {
            previewNodes.remove(1);
        }

        String filePreview = client.requestPreview(filename);
        if (!"".equals(filePreview)) {
            preview.setText(filePreview);
            previewNodes.add(previewPane);
        } else {
            previewNodes.add(previewInstructionsContainer);
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

    public void highlightServerFile(String filename) {
        localSelectionModel.clearSelection();
        serverSelectionModel.clearSelection();
        System.out.println(filename);

        /* This is backup code in case a certain bug crops up again */
//        ObservableList<TreeItem<String>> serverItems = serverDirectoryName.getChildren();
//        for (int i = 0; i < serverItems.size(); i++) {
//            TreeItem<String> item = serverItems.get(i);
//            if (item.getValue().equals(filename)) {
//                serverSelectionModel.select(serverTreeView.getRow(item));
//            }
//        }
        /* the following lines of code do the same thing, but seem more likely to throw an error if the bug comes back */
        serverDirectoryName.getChildren().forEach(new Consumer<TreeItem<String>>() {
            @Override
            public void accept(TreeItem<String> stringTreeItem) {
                System.out.println(stringTreeItem.getValue() + " " + filename + " " + stringTreeItem.getValue().equals(filename));
                if(stringTreeItem.getValue().equals(filename)) {
                    serverSelectionModel.select(serverTreeView.getRow(stringTreeItem));
                };
            }
        });
    }

}
