# FileSharer

## Author

Cody Malcolm 100753739

## Project Information

This application consists of a Server and Client to facilitate file sharing. The Server is implemented in Java and the client is implemented in JavaFX.

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

- Improved the aesthetics of the basic layout

- Allow the clients to change the local directory during runtime

- A pane was added to display the contents of a selected file

- Clients are given the ability to password-protect access to files on the server

### Specific exclusions

- The server does not need a user interface

- No mechanism for navigating to sub-directories is required, and it can be presumed that only text files can be in the shared directory

## How to Run

After cloning or unzipping the project, navigate to the root directory in a terminal and start the server with `gradle startServer`. 

After cloning or unzipping the project, navigate to the root directory in a terminal and start the clients with `gradle run`.

Note that in a Bash terminal, you can append "&" to the end of a command to run the process in the background, allowing you to run one or more clients in the same terminal window you used to start the server. For example: 
```
gradle startServer &
gradle run &
gradle run
```
Gradle version 5.6.4 or newer is required.
