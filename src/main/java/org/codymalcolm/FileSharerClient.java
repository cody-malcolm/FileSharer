// Cody Malcolm 100753739
// March 27th, 2021
// CSCI 2020u - Assignment #2 - File Sharing System

package org.codymalcolm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

/**
 * The FileSharerClient initializes the Client Application, sends requests to the server, and processes response from
 * the server.
 */
public class FileSharerClient extends Application {
    /** The Reader used to get input from the server */
    private BufferedReader in;
    /** The Writer used to send output to the server */
    private PrintWriter out;
    /** The hostname of the server, default the local machine */
    private String hostname = "localhost";
    /** The port to send requests to, default 9001 */
    private int port = 9001;
    /** The Controller associated with this client */
    private Controller controller;
    /** The user-provided alias to use as ID for the server */
    private String alias;

    /**
     * JavaFX main function
     *
     * @param args any command line arguments (see start() method for arg requirements)
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initializes the Application and requests a listing of server contents.
     *
     * @param primaryStage the Stage used for the JavaFX Application
     */
    @Override
    public void start(Stage primaryStage) {
        // get the arguments the user provided
        List<String> parameters = getParameters().getRaw();

        // check how many arguments were provided
        int numParams = parameters.size();

        // if less than 2, print an error message to console and abort
        if (numParams < 2) {
            System.out.println("Note: Usage is 'gradle run --args=\"<alias> <local-directory>\"'. Aborting startup.");
            System.exit(0);
        }

        // if a 3rd argument is provided, update the hostname with the 3rd argument
        if (numParams >= 3) {
            hostname = parameters.get(2);
        }

        // if a fourth argument is provided, update the port with the 4 argument if it can be parsed as an integer
        if (numParams > 3) {
            port = Utils.parsePort(parameters.get(3));
        }

        // get the loader
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        try {
            // get the root from the loader
            Parent root = loader.load();

            // get the controller from the loader
            controller = loader.getController();

            // set up and show the Stage
            primaryStage.setTitle("File Sharer v1.0");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();

            // set the fields the controller needs
            controller.setPrimaryStage(primaryStage);
            controller.setClient(this);
            controller.setInitialLocalDirectory(parameters.get(1));

            // set up the controller
            controller.setup();

            // set the alias
            alias = parameters.get(0);

            // request the files in the shared folder from the server
            requestDirectory();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Establishes a connection with the server and invokes the methods to send the request and process the responses.
     *
     * @param filename the filename to upload
     * @param targetName the filename to request the server name the file (could be "" to indicate use filename)
     */
    public void requestUpload(String filename, String targetName) {
        // establish a connection
        boolean connected = establishConnection();
        if (connected) {
            // send the request
            sendRequest("UPLOAD", filename, targetName);

            // process the response
            processUploadResponse();
            processDirectoryResponse();
        }
    }

    /**
     * Establishes a connection with the server and invokes the methods to send the request and process the responses.
     *
     * @param filename the filename to download
     * @param localDirectory the filename of the local directory that the downloaded file will be saved to
     * @param targetName the filename to save the file as
     */
    public void requestDownload(String filename, String localDirectory, String targetName) {
        // establish the connection
        boolean connected = establishConnection();

        if (connected) {
            // send the request
            sendRequest("DOWNLOAD", filename, "");

            // process the response
            processDownloadResponse(("".equals(targetName) ? filename : targetName), localDirectory);
            processDirectoryResponse(filename);
        }
    }

    /**
     * Establishes a connection with the server and invokes the methods to send the request and process the responses.
     *
     * @param filename the filename to download
     * @return the preview text
     */
    public String requestPreview(String filename) {
        // establish the connection
        boolean connected = establishConnection();

        if (connected) {
            // send the request
            sendRequest("DOWNLOAD", filename, "");

            // store the preview text
            String response = processPreviewResponse();

            // update the directory
            processDirectoryResponse(filename);

            // return the response
            return response;
        } else {
            return "";
        }
    }

    /**
     * Sends a request of the following format to the server:
     *
     * <alias>
     * DIR
     */
    private void sendInitialRequest() {
        out.println(alias);
        out.println("DIR");
        out.flush();
    }

    /**
     * Establishes a connection with the server and invokes the methods to send the request and process the responses.
     */
    public void requestDirectory() {
        // send the request
        boolean connected = establishConnection();

        if (connected) {
            // send the request
            sendInitialRequest();

            // process the response
            processDirectoryResponse();
        }
    }

    /**
     * Establishes a connection with the server and invokes the methods to send the request and process the responses.
     *
     * @param filename the name of the file to be deleted
     */
    public void requestDelete(String filename) {
        // establish the connection
        boolean connected = establishConnection();

        if (connected) {
            // send the request
            sendRequest("DELETE", filename, "");

            // process the response
            processDeleteResponse(filename);
            processDirectoryResponse();
        }
    }

    /**
     * Parses the server response to an upload request, and provides appropriate feedback to the user in case of error.
     */
    private void processUploadResponse() {
        try {
            // if success code
            if (in.readLine().equals("202")) {
                in.readLine(); // discard next line (is 0, indicating no more lines of this portion of the response)
            } else {
                // send error feedback to the controller
                controller.giveFeedback(in.readLine(), false);
            }
        } catch(IOException e) {
            // send error feedback to the controller
            controller.giveFeedback("There was an error parsing the response from the server", false);
        }
    }

    /**
     * Parses the server response to a delete request, and provides appropriate feedback to the user.
     *
     * @param filename the name of the file that was deleted
     */
    private void processDeleteResponse(String filename) {
        try {
            // if success code
            if (in.readLine().equals("204")) {
                // send success feedback to the controller
                controller.giveFeedback("'" + filename + "' was deleted successfully.", true);

                in.readLine(); // discard next line (is 0, indicating no more lines of this portion of the response)
            } else {
                // send error feedback to the controller
                controller.giveFeedback(in.readLine(), false);
            }
        } catch(IOException e) {
            // send error feedback to the controller
            controller.giveFeedback("There was an error parsing the response from the server", false);
        }
    }

    /**
     * Parses the server response to a download request (for a preview), and provides appropriate feedback to the user
     * in cases where there is an error.
     *
     * @return a String with the text to preview
     */
    private String processPreviewResponse() {
        // initialize the StringBuilder
        StringBuilder response = new StringBuilder();

        // initialize a String to store the current line
        String line;

        try {
            // if success code
            if ((line = in.readLine()).equals("203")) {
                // get the number of lines of this portion of the response
                int numLines = Integer.parseInt(in.readLine());

                // initialize an iterator
                int i = 0;

                // while we have not read all of this portion of the response (+ guards)
                while((i++ < numLines) && in.ready() && (null != (line = in.readLine()))) {
                    // add this line to the response
                    response.append(line).append("\r\n");
                }

                // return the response to be previewed
                return response.toString();
            } else if ("403".equals(line)) {
                // send error feedback to the controller
                controller.giveFeedback(in.readLine(), false);

                // return the response (will be "" if nothing appended to the StringBuilder)
                return response.toString();
            }
        } catch(IOException e) {
            e.printStackTrace();
            // return the response (will be "" if nothing appended to the StringBuilder)
            return response.toString();
        }
        // return the response (will be "" if nothing appended to the StringBuilder)
        return response.toString();
    }

    /**
     * Parses the server response to a download request (for a download), provides appropriate feedback to the user,
     * and creates a new file with the contents of the response.
     *
     * @param filename the filename use for the new File
     * @param localDirectory the directory to save the new File in
     */
    private void processDownloadResponse(String filename, String localDirectory) {
        // initialize the String to hold a single line
        String line;
        try {
            // if success code
            if ((line = in.readLine()).equals("203")) {
                // verify filename uniqueness
                filename = Utils.getFilename(localDirectory + filename);

                // get the number of lines of this portion of the response
                int numLines = Integer.parseInt(in.readLine());

                // initialize an iterator
                int i = 0;

                // initialize the Writer
                PrintWriter writer = new PrintWriter(filename);

                // while we have not read all of this portion of the response (+ guards)
                while((i++ < numLines)  && in.ready() && (null != (line = in.readLine()))) {
                    // add this line of the response to the Writer
                    writer.println(line);
                }

                // write the contents to the file and close the Writer
                writer.flush();
                writer.close();

                // send a success message to the controller
                controller.giveFeedback("'" + new File(filename).getName() + "' was successfully downloaded.", true);
            } else if ("403".equals(line)) {
                // send the error message to the controller
                controller.giveFeedback(in.readLine() , false);
            } else {
                // send a generic error message to the controller
                controller.giveFeedback("There was an error trying to download " + filename, false);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Wrapper for processDirectoryResponse(String), when no server tree highlighting is required.
     */
    private void processDirectoryResponse() {
        processDirectoryResponse("");
    }

    /**
     * Parses the directory portion of a server response to a request and invokes the relevant methods to update the
     * server TreeItems
     *
     * @param selectedFilename the filename that should be highlighted after the tree is rebuilt (if not "")
     */
    private void processDirectoryResponse(String selectedFilename) {
        try {
            // if success code
            if ("201".equals(in.readLine())) {
                // initialize String to hold a single line
                String line;

                /* discard number of lines of this part of the response, not used in current implementation since this
                is processed last              */
                in.readLine();

                // read in the name of the shared directory
                String sharedDirectoryName = (in.readLine());

                // clear the server tree and update the name of the shared directory
                controller.clearServerTree();
                controller.updateServerDirectory(sharedDirectoryName);

                // the response always contains a '""' at the end, we do not want this appended
                while (!"".equals(line = in.readLine())) {
                    controller.addServerFileListing(line);
                }

                // highlight the selected filename, if not ""
                if (!"".equals(selectedFilename)) {
                    controller.highlightServerFile(selectedFilename);
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Sends a request to the server. Handles all requests after the initial directory request.
     *
     * All requests processed by this method send at least:
     *
     * <alias>
     * <type>
     * <filename>
     *
     * If the type is "UPLOAD", the request will also contain one line for each line in the File associated with the
     * provided filename.
     *
     * @param type the type of request (one of "UPLOAD", "DOWNLOAD", or "DELETE")
     * @param filename the filename to send to the server if targetName is "", and for "UPLOAD" the filename to read from
     * @param targetName the filename to send to the server, if not ""
     */
    private void sendRequest(String type, String filename, String targetName) {
        // send the alias and type
        out.println(alias);
        out.println(type);

        // initialize the File associated with this request (always used one way or another)
        File file = new File(filename);

        // if the target name is blank, send the name of the file with path stripped out, else send target name
        if ("".equals(targetName)) {
            out.println(file.getName());
        } else {
            out.println(targetName);
        }

        // if upload,
        if (type.equals("UPLOAD")) {
                // read and copy file
            try {
                // initialize the BufferedReader
                BufferedReader input = new BufferedReader(new FileReader(file));

                // initialize the variable to hold a single line
                String line;

                // read each line in the file and send it to server
                while (null != (line = input.readLine())) {
                    out.println(line);
                }

                // send the confirmation to the controller
                // if the target name is blank, send the name of the file with path stripped out, else send target name
                if ("".equals(targetName)) {
                    controller.giveFeedback("Uploaded '" + file.getName() + "' successfully!", true);
                } else {
                    controller.giveFeedback("Uploaded '" + targetName + "' successfully!", true);
                }
            } catch(IOException e) {
                // send a notification to the controller that something went wrong
                controller.giveFeedback("Something went wrong reading the file.", false);
            }

        }

        // release the output to the server
        out.flush();
    }

    /**
     * Establishes a connection with the server. If successful, clears the feedback Label and returns true. Otherwise,
     * clears the server tree, updates the feedback Label with an appropriate error, and returns false.
     *
     * @return whether or not the connection was established
     */
    private boolean establishConnection() {
        try {
            controller.giveFeedback("Attempting to connect, please wait...", true);

            // establish the connection, 2500ms timeout
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(hostname, port), 2500);

            // initialize a Reader and Writer
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());

            // send the feedback to the controller
            controller.giveFeedback("", true);

            return true;

        } catch(IOException e) {
            // clear the server tree and send feedback to the controller
            controller.clearServerTree();
            controller.giveFeedback("A connection was not established.", false);

            return false;
        }
    }
}
