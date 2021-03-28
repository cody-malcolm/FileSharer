// Cody Malcolm 100753739
// March 27th, 2021
// CSCI 2020u - Assignment #2 - File Sharing System

package org.codymalcolm;

import javafx.collections.ObservableList;
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

/**
 * Handles updates to the UI based on data provided by the FileSharerClient. Facilitates interfacing with main.fxml.
 */
public class Controller {
    /** the label where the local directory is displayed/updated (top right of UI) */
    @FXML
    private Label localDirectory;
    /** the TreeView that displays the local files (left pane) */
    @FXML
    private TreeView<String> localTreeView;
    /** the TreeView that displays the server files (center pane) */
    @FXML
    private TreeView<String> serverTreeView;
    /** the "root" entry of the local directory TreeView */
    @FXML
    private TreeItem<String> localDirectoryName;
    /** the "root" entry of the server directory TreeView */
    @FXML
    private TreeItem<String> serverDirectoryName;
    /** the Label that contains the preview of the selected File (right pane, when preview present) */
    @FXML
    private Label preview;
    /** the container for the preview Label, to facilitate scrolling for larger files */
    @FXML
    private ScrollPane previewPane;
    /** the container for the preview instructions (right pane, when no preview available */
    @FXML
    private VBox previewInstructionsContainer;
    /** the container for the preview header and the previewPane or previewInstructionsContainer (as appropriate) */
    @FXML
    private VBox previewContainer;
    /** the Label used to display feedback to the user */
    @FXML
    private Label feedback;
    /** the TextField where the user can request custom filenames for uploads/downloads */
    @FXML
    private TextField customFilename;

    /** the filename of the initial local directory to display during start-up */
    private String initialLocalDirectory;
    /** the FileSharerClient associated with "this" */
    private FileSharerClient client;
    /** the currently selected filename */
    private String selectedFilename;
    /** a flag to indicate if the current selected filename belongs to the local directory */
    private boolean localFile = false;
    /** a flag to indicate if the current selected filename belongs to the shared directory */
    private boolean serverFile = false;
    /** the Stage to display content on (used with the DirectoryChooser) */
    private Stage primaryStage;
    /** a SelectionModel to facilitate selection of the correct local TreeItems */
    private SelectionModel<TreeItem<String>> localSelectionModel;
    /** a SelectionModel to facilitate selection of the correct server TreeItems */
    private SelectionModel<TreeItem<String>> serverSelectionModel;

    /**
     * A setter for the primary stage.
     *
     * @param s the Stage
     */
    public void setPrimaryStage(Stage s) {
        primaryStage = s;
    }

    /**
     * A setter for the initial local directory filename. Handles case where user entered an invalid path.
     *
     * @param d the filename
     */
    public void setInitialLocalDirectory(String d) {
        // check for existence
        if (new File(d).exists()) {
            initialLocalDirectory = d;
        } else {
            // handle case where directory doesn't exist - makes use of fact src/main/resources is known to exist
            initialLocalDirectory = "src/main/resources/local";
            System.out.println("The provided directory doesn't exist. Using " + initialLocalDirectory + " instead.");
            File dir = new File(initialLocalDirectory);
            if (!dir.exists()) {
                dir.mkdir();
            }
        }
    }

    /**
     * A setter for the FileSharerClient associated with "this".
     *
     * @param client the client for "this" to communicate with
     */
    public void setClient(FileSharerClient client) {
        this.client = client;
    }

    /**
     * Some setup is required after the setters have been called (which happens after the initialize() function), so
     * this facilitates the true initialization of the controller. Invoked by FileSharerClient after it has set the
     * mentioned setters.
     */
    public void setup() {
        // set the text of the Label used to select the directory to the default
        localDirectory.setText(initialLocalDirectory);

        /* the preview container is initialized in main.fxml with both the previewPane and the
        previewInstructionsContainer, but we only want the previewInstructionsContainer for now, so remove the other */
        previewContainer.getChildren().remove(1);

        // initializes the SelectionModels
        localSelectionModel = localTreeView.getSelectionModel();
        serverSelectionModel = serverTreeView.getSelectionModel();

        // initialize the local TreeView
        refreshLocal();
    }

    /**
     * Handles all functionality with refreshing the local file tree.
     *
     * Side effect: clears all highlighting of the local file tree
     */
    private void refreshLocal() {
        // clear any selections in the local tree (else, when the nodes are removed the root node would get highlighted)
        localSelectionModel.clearSelection();

        // clear all children below the "root" TreeItem
        localDirectoryName.getChildren().clear();

        // read the local directory from the appropriate label (in the top right of UI)
        String directory = localDirectory.getText();

        // set the "root" TreeItem text to the selected directory
        localDirectoryName.setValue(directory);

        // initialize a File with the selected directory
        File dir = new File(directory);

        // get a list of the filenames contained in the selected directory
        String[] filenames = dir.list();

        // sort the list of filenames
        Arrays.sort(filenames);

        // store the path to the directory
        String path = localDirectory.getText();

        // if the "/" is missing (such as during startup), append it
        if (!"/".equals(path.substring(path.length()-1))) {
            path += "/";
        }

        // for each filename, check if it's a directory and if not, create and append TreeItem to represent it
        for (String filename : filenames) {
            if (!new File(path + filename).isDirectory()) {
                TreeItem<String> entry = new TreeItem<>();
                entry.setValue(filename);
                localDirectoryName.getChildren().add(entry);
            }
        }
    }

    /**
     * A wrapper for refreshLocal(), that also highlights the provided filename once the refresh has been completed
     *
     * @param filename the filename to highlight (could be null)
     */
    private void refreshLocal(String filename) {
        // invoke refreshLocal to refresh the local TreeView
        refreshLocal();

        // guard against null, which is valid input for this parameter
        if (null != filename) {
            // strip out the path information from the filename
            String selected = new File(filename).getName();

            // check each local TreeItem and if its text matches the filename, highlight it
            localDirectoryName.getChildren().forEach(stringTreeItem -> {
                if(stringTreeItem.getValue().equals(selected)) {
                    localSelectionModel.select(localTreeView.getRow(stringTreeItem));
                }
            });
        }
    }

    /**
     * Updates the feedback Label with messages for the user.
     *
     * @param s the message to display
     * @param key true if the message is a success message, false otherwise
     */
    protected void giveFeedback(String s, boolean key) {
        // set the text
        feedback.setText(s);

        // remove any previous styling and apply the new styling based on the key
        feedback.getStyleClass().clear();
        feedback.getStyleClass().add((key ? "successMessage" : "failureMessage"));
    }

    /**
     * A click handler for the upload button. Checks some guard conditions and invokes the client's requestUpload()
     * method if all pass, otherwise provide feedback to the user on why the upload was not requested.
     */
    public void upload() {
        // if a file is selected from the local file pane
        if (null != selectedFilename && localFile) {
            // request the upload
            client.requestUpload(selectedFilename, customFilename.getText());
        } else {
            // provide relevant feedback
            if (null == selectedFilename) {
                giveFeedback("No file has been selected!", false);
            } else {
                giveFeedback("To upload, you must select a local file.", false);
            }
        }

        // refresh the local directory in case of any file changes from other applications
        refreshLocal(selectedFilename);
    }

    /**
     * A click handler for the download button. Checks some guard conditions and invokes the client's requestDownload()
     * method if all pass, otherwise provide feedback to the user on why the download was not requested.
     */
    public void download() {
        // if a file is selected from the server file pane
        if (null != selectedFilename && serverFile) {
            // request the download
            client.requestDownload(selectedFilename, localDirectory.getText(), customFilename.getText());
        } else {
            // provide relevant feedback
            if (null == selectedFilename) {
                giveFeedback("No file has been selected!", false);
            } else {
                giveFeedback("To download, you must select a server file.", false);
            }
        }

        // refresh the local directory in case of any file changes from other applications
        refreshLocal();
    }

    /**
     * A click handler for the delete button. Checks some guard conditions and invokes the client's requestDelete()
     * method if all pass, otherwise provide feedback to the user on why the deletion was not requested.
     */
    public void delete() {
        // if a file is selected from the server file pane
        if (null != selectedFilename && serverFile) {
            // request the deletion
            client.requestDelete(selectedFilename);
        } else {
            // provide relevant feedback
            if (null == selectedFilename) {
                giveFeedback("No file has been selected!", false);
            } else {
                giveFeedback("To delete a local file, use your OS file manager.", false);
            }
        }

        // refresh the local directory in case of any file changes from other applications
        refreshLocal();

        // ensure no server files remain selected
        serverSelectionModel.clearSelection();
    }

    /**
     * A helper method to parse the text from the Target of a TreeView mouse click into the filename we want.
     *
     * There are two forms the text may take, details inline.
     *
     * @param response the raw text from the mouseEvent.getTarget().toString()
     * @return the filename from the reponse
     */
    private String parseTreeSelection(String response) {
        // assume the response contains "text=" as it does with direct clicks, initialize the start index of the filename
        int start = response.indexOf("text=") + 6; // will be 5 if "text=" was not found (since indexOf would be -1)

        // initialize a variable to hold the end index
        int end;

        // if you click near the edge of the TreeItem, temp will be of the form "...]'<name>'", so "text=" is not found
        if (start == 5) {
            // then update the start index to the index indicated by the other format
            start = response.indexOf("]'") + 2;

            // get the end index based on this format
            end = response.indexOf('\'', start);
        } else {// if you click the TreeItem directly, temp will be of the form: "Text[text="<name>", ...]"
            // get the end index based on this format
            end = response.indexOf('"', start);
        }

        // return just the filename based on the start and end indexes found
        return response.substring(start, end);
    }

    /**
     * Handles all the events related to the user clicking on the local file pane, including:
     * - deselecting any selected server files
     * - updating the localFile and serverFile flags
     * - displaying a file preview
     * - refreshing the local and server file TreeItems
     *
     * @param mouseEvent the click event
     */
    public void handleLocalTreeClick(MouseEvent mouseEvent) {
        // clear any selected server files
        serverSelectionModel.clearSelection();

        // get the path of the file that was clicked on
        String path = localDirectory.getText() + parseTreeSelection(mouseEvent.getTarget().toString());

        // create a File associated with the path
        File temp = new File(path);

        // initialize a String to store the filename, if the file exists
        String filename = null;

        // if the file exists
        if (temp.exists()) {
            // update the selected filename field to the new file
            selectedFilename = path;

            // update the flags associated with which pane is "active"
            localFile = true;
            serverFile = false;

            // store the name of the selected file
            filename = temp.getName();

            // initialize a StringBuilder for the file preview
            StringBuilder previewText = new StringBuilder();
            try {
                // initialize a Reader for the file
                BufferedReader reader = new BufferedReader(new FileReader(temp));

                // initialze the String to store one line of text from the file
                String line;

                // read in each line of text
                while (null != (line = reader.readLine())) {
                    // and append it to the StringBuilder
                    previewText.append(line).append("\r\n");
                }

                // close the Reader
                reader.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
            // set the preview Label to the file contents that were read
            preview.setText(previewText.toString());

            // remove the second child of the preview container and replace it with the preview pane
            previewContainer.getChildren().remove(1);
            previewContainer.getChildren().add(previewPane);
        } else {
            // the file must have been deleted by another application, so update fields accordingly
            selectedFilename = null;
            localFile = false;
            serverFile = false;

            // remove the second child of the preview container and replace it with the instructions container
            previewContainer.getChildren().remove(1);
            previewContainer.getChildren().add(previewInstructionsContainer);
        }

        // refresh both lists of TreeItems
        client.requestDirectory();
        refreshLocal(filename);
    }

    /**
     * Handles all the events related to the user clicking on the server file pane, including:
     * - updating the selected filename
     * - updating the localFile and serverFile flags
     * - requesting and displaying a file preview
     *
     * Note: This is structured quite differently than the local tree click, because there are many "side effects"
     * from processing the response of the directory request that comes with the File preview, so functions like
     * refreshing the local and server panes happen within those functions.
     *
     * @param mouseEvent the click event
     */
    public void handleServerTreeClick(MouseEvent mouseEvent) {
        // parse the file name that was clicked on
        String filename = parseTreeSelection(mouseEvent.getTarget().toString());

        // update the filename field accordingly
        selectedFilename = filename;

        // set the flags
        localFile = false;
        serverFile = true;

        // guard is to deal with an edge case
        ObservableList<Node> previewNodes = previewContainer.getChildren();
        if (previewNodes.size() > 1) {
            // remove the second child of the preview container's children
            previewNodes.remove(1);
        }

        // request a preview of the selected file from the server
        String filePreview = client.requestPreview(filename);

        // if received,
        if (!"".equals(filePreview)) {
            // set the preview Label to the received text and display the preview pane
            preview.setText(filePreview);
            previewNodes.add(previewPane);
        } else {
            // otherwise set the preview instructions
            previewNodes.add(previewInstructionsContainer);
        }
    }

    /**
     * This is essentially a setter for the value of the "root" TreeItem of the server pane
     *
     * @param sharedDirectoryName the new value for the server pane "root" TreeItem
     */
    public void updateServerDirectory(String sharedDirectoryName) {
        serverDirectoryName.setValue(sharedDirectoryName);
    }

    /**
     * Appends a single TreeItem to the "root" server TreeItem.
     *
     * @param filename the value for the new TreeItem
     */
    public void addServerFileListing(String filename) {
        // create the TreeItem
        TreeItem<String> entry = new TreeItem<>();

        // set the value
        entry.setValue(filename);

        // append it to the "root" TreeItem
        serverDirectoryName.getChildren().add(entry);
    }

    /**
     * Removes all children of the "root" TreeItem in the server pane, and resets the value of the "root" node to ""
     */
    public void clearServerTree() {
        serverDirectoryName.getChildren().clear();
        serverDirectoryName.setValue("");
    }

    /**
     * Allows the user to select a local directory, and updates the Label text accordingly.
     *
     * Acknowledgment: This code is reused from Assignment 1, with the guard and refreshLocal() call new additions.
     */
    public void chooseDirectory() {
        // initialize a DirectoryChooser
        DirectoryChooser directoryChooser = new DirectoryChooser();

        // set the initial directory to show the current selection
        directoryChooser.setInitialDirectory(new File(new File(localDirectory.getText()).getParent()));

        // update the text of the directory Label to the new chosen directory
        File chosenDirectory = directoryChooser.showDialog(primaryStage);

        // guard against "cancel"
        if (null != chosenDirectory) {
            localDirectory.setText(chosenDirectory.getPath() + "/");
        }

        // update the pane to show the contents of the new directory
        refreshLocal();
    }

    /**
     * Highlights the TreeItem with a value that matches the provided filename.
     *
     * @param filename the filename we want to highlight
     */
    public void highlightServerFile(String filename) {
        // clear an existing selections from both panes
        localSelectionModel.clearSelection();
        serverSelectionModel.clearSelection();

        // check each child of the "root" server TreeItem and select it if its value matches the filename
        serverDirectoryName.getChildren().forEach(stringTreeItem -> {
            if(stringTreeItem.getValue().equals(filename)) {
                serverSelectionModel.select(serverTreeView.getRow(stringTreeItem));
            }
        });
    }
}
