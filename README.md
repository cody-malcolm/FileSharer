# FileSharer

## Author

Cody Malcolm 100753739

## Project Information

This application consists of a Server and Client to facilitate the sharing of text-based files. The Server is 
implemented in Java and the client is implemented in JavaFX.

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

- The server can optionally take arguments for the directory name (default `shared`) and port (default `9001`) as arguments.
  Whether a custom directory is provided or not, if the directory does not exist, it will be created during startup.
  
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

- No mechanism for navigating to sub-directories is required, and it can be presumed that only text files can be in the shared directory

## How to Run

After cloning or unzipping the project, navigate to the root directory in a terminal and start the server with one of the following commands: 
- `gradle startServer`
- `gradle startServer --args="<directory-name>"`
- `gradle startServer --args="<directory-name> <port>"`

The `directory-name` is the local directory the server will use this session (default `shared`). 

The `port` is the port the server will listen to this session (default `9001`).

With the server running, navigate to the root directory in a terminal and start one or more clients with one of the following commands:
- `gradle run --args="<alias> <directory-path>"`
- `gradle run --args="<alias> <directory-path> <hostname>"`
- `gradle run --args="<alias> <directory-path> <hostname> <port>"`

The `alias` is an identifier for the client.

The `directory-path` is the initial directory for the local files (this can be changed during runtime).

The `hostname` is the IP address of the server (default `localhost`). If the client is running on the same machine as the server, this can be omitted.

The `port` is the port the client will seek to connect to (default `9001`).

*Gradle version 5.6.4 or newer is required.*

Note that in a Bash terminal, you can append `&` to the end of a command to run the process in the background, allowing you to run one or more clients in the same terminal window you used to start the server. For example: 
```
gradle startServer &
gradle run --args="myPC1 src/main/resources/local/" &
gradle run --args="myPC2 src/main/resources/local/"
```

## Known Issues

The program will crash and freeze if a binary file (such as an image file) is clicked on.
This program should only be used with text-based files.