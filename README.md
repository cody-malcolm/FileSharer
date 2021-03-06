# FileSharer

## Author

Cody Malcolm 100753739

## Project Information

This application consists of a Server and Client to facilitate the sharing of text-based files. The Server is 
implemented in Java and the client is implemented in JavaFX.

## Runtime Examples

1. Download demonstration
![Client Demonstration 1](./demos/ClientDemo2.png)

2. Upload demonstration
![Client Demonstration 2](./demos/ClientDemo4.png)

3. Server admin demonstration
![Server Demonstration](./demos/ServerDemo1.png)

More demonstrations, including of client error messages and a server log file, can be found in the demos folder.

## Requirements

### Main

The base requirements of the project are as follows:

- The clients will connect to a central server

- The clients are to have a "simple" user interface implemented in JavaFX

- The server must be multi-threaded, and each incoming client connection should be handled with a separate thread of type `ClientConnectionHandler`

- The threads and corresponding sockets should remain open only until the command has been handled (the server should terminate the connection)

- When a client is started, the computer alias and local directory path are passed as command-line arguments

- The client will show a split screen showing two lists of filenames:

  - The left will be the list of files in the local directory
  
  - The right will be the list of files in the shared directory of the server
  
- The client will have two buttons at the top, an UPLOAD and DOWNLOAD button:
 
  - The UPLOAD button will transfer a selected file in the left pane to the server's remote directory
  
  - The DOWNLOAD button will transfer a selected file in the right pane to the client's local directory
  
- When an UPLOAD or DOWNLOAD takes place, every character in the file will be transferred, and the user interface will need to refresh both lists of files

### Implemented Improvements

- Improved the aesthetics of the basic layout.

- Allow the clients to change the local directory during runtime.

- Allow the server to change the shared directory during runtime. Whether a custom directory is provided or not, if 
  the directory does not exist, it will be created.

- A pane was added to display the contents of a selected file.

- Clients can request the deletion of files stored in the shared directory.

- Feedback is provided on the client UI for most successful and unsuccessful actions (as appropriate)

- The server application can be controlled via the command line with basic commands.

- The server logs the following events to both the console and a log file:
    - When a connection is established (including the IP of the client)
    - When a request is received (including type of request, client alias, and client IP)
    - When a request has been fulfilled (including details about the request, client alias, and client IP)
    - When a connection is terminated
    
- If the log file is not found, a new one will be created. Additionally, the log records the time the event occurs.

- The server can optionally take an argument for the port to listen to (default `9001`) as a command-line argument.

- The client can optionally take arguments for both the hostname (default `localhost`) and port (default `9001`).
  
- When uploading or downloading a file, the client can optionally provide a new name for the file.
If this is not done, the filename will default to the current name. 
  In either case, if a file with the requested name already exists in the destination directory,
  a unique name will be generated in the same way as common OS filesystems handle filename conflicts 
  (eg. if `sample.txt` is requested, but `sample.txt` and `sample(1).txt` already exist, the file will be named `sample(2).txt`).
  
- Significant effort was made to make the program reasonably robust (given the scope) and to protect against bad inputs,
even in cases where such inputs are not possible with the client as implemented. For example, the server correctly 
  handles requests of types other than `DIR`, `UPLOAD`, `DOWNLOAD`, and `DELETE` with an appropriate error message,
  even though the provided client is incapable of sending any other types of requests. After all, never trust the users.
  
### Specific Exclusions

- The server does not need a user interface

- No mechanism for navigating to sub-directories is required, and it can be presumed that only text files can be in 
  the shared directory

## Usage

### How to Run

After cloning or unzipping the project, navigate to the root directory in a terminal. Then:

The server can be started with the following command: `gradle -q start [--args="<port>"]`

The `port` is the port the server will listen to this session (default `9001`).

The client(s) can be started with the following command: 

`gradle -q run --args="<alias> <directory-path> [<hostname> [<port>]]"`

The `alias` is an identifier for the client.

The `directory-path` is the initial directory for the local files (this can be changed during runtime).

The `hostname` is the IP address of the server (default `localhost`). If the client is running on the same machine as the server, this can be omitted.

The `port` is the port the client will seek to connect to (default `9001`).

*Gradle version 5.6.4 or newer is required.*

Note that in a Bash terminal, you can append `&` to the end of a command to run the process in the background, allowing you to run one or more clients in the same terminal window you used to start the server. For example: 
```
gradle -q start &
gradle -q run --args="myPC1 src/main/resources/local/" &
gradle -q run --args="myPC2 src/main/resources/local/"
```

### How to Use

The server takes 6 instructions which are described during startup, when an unsupported instruction is received, and 
when the `help` instruction is received. The important details are that the server must be told to listen for 
connections with the `start [name]` instruction, and the admin can change the shared directory without stopping the 
Application by issuing a `stop` instruction followed by a new `start [name]` instruction.

The client has a clickable user interface. The directory can be changed during runtime by clicking on the input to the
right of the buttons. If an upload or download is requested and there is text in the custom filename field, the
Application will rename the file at the destination according to the contents of the custom filename field. It is
crucial that the user not click on any binary file types (such as images), or the Application will crash and freeze.

## Known Issues

Major: The program will crash and freeze if a binary file (such as an image file) is clicked on.
This program should only be used with text-based files. A blacklist of some common file binary type file extensions 
has been included, which should eliminate most accidental crashes for files which have accurate extensions.

Minor: When displaying file previews, the client will display empty files as though no file is selected.