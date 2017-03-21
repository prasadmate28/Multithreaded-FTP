# Multithreaded-FTP

Distributed Computing Systems (Spring 2017 CSCI 6780) - Project 2

Team Members: Prasad Mate, Sharmin Pathan

Technologies Used: Java 1.8

Problem Statement: To design and implement multithreaded FTP Client and Server

Commands:
--------

We have implemented the following FTP commands. The syntax of the command is indicated in the parenthesis.

- get (get remote_filename) -- Copy file with the name <remote_filename> from remote directory to local directory.
- put (put local_filename) -- Copy file with the name <local_filename> from local directory to remote directory.
- get (get remote_filename) & -- Copy file with the name <remote_filename> from remote directory to local directory by creating a new thread.
- put (put local_filename) & -- Copy file with the name <local_filename> from local directory to remote directory by creating a new thread.
- terminate (terminate command_id) -- to terminate a long running command.
- delete (delete remote_filename) – Delete the file with the name <remote_filename> from the remote directory.
- ls (ls) -- List the files and subdirectories in the remote directory.
- cd (cd remote_directory_name or cd ..) – Change to the <remote_direcotry_name > on the remote machine or change to the parent directory of the current directory
- mkdir (mkdir remote_directory_name) – Create directory named <remote_direcotry_name> as the sub-directory of the current working directory on the remote machine.
- pwd (pwd) – Print the current working directory on the remote machine.
- quit (quit) – End the FTP session.

FTP Server (myftpserver program):
--------------------------------
The server program takes two command line parameters, which are the port numbers where the server will wait on (one for normal commands and another for the “terminate” command). The port for normal commands are henceforth referred to as “nport” and the terminate port is referred to as “tport”. Once the myftpserver program is invoked, it should create two threads. The first thread will create a socket on the first port number and wait for incoming clients. The second thread will create a socket on the second port and wait for incoming “terminate” commands.

When a client connects to the normal port, the server will spawn off a new thread to handle the client commands. The operation of this thread is same as described in project 1. However, when the serve gets a “get” or a “put” command, it will immediately send back a command ID to the client. This is for the clients to use when they need to terminate a currently-running command. Furthermore, the threads executing the “get” and “put” commands, will periodically (after transferring 1000 bytes) check the status of the command to see if the client needs the command to be terminated. If so, it will stop transferring data, delete any files that were created and will be ready to execute more commands.

Note that there might be several normal threads executing concurrency. The server should handle the consistency issues arising out of such concurrency. For example, if two put requests arrive concurrently for the same file, one request should be completed before the other starts.
 
When a client connects to the “tport”, the server accepts the terminate command. The command will identify (using the command-ID) which command needs to be terminated. It will set the status of that command to “terminate” so that the thread executing that command will notice it and gracefully terminate.

FTP Client:
----------
The ftp client program will take three command line parameters the machine name where the server resides, the normal port number, and the terminate port number. Once the client starts up, it will display a prompt “mytftp>”. It should then accept and execute commands as in Project 1. However, if any command is appended with a “&” sign (e.g., get file1.txt &), then this command should be executed in a separate thread. The main thread should continue to wait for more commands (i.e., it should not be blocked for the other threads to complete). For “get” and “put” commands, the client should display the command-ID received from the server. When the user enters a terminate command, the client should use the tport to relay the command to the server. The client should also clean up any files that were created as a result of commands that were terminated.

Execution:
---------
The project is developed using Eclipse.

- To execute myftpserver, goto DCSProject2Server’s bin directory and run 'java com.ds.server.myftpserver portNumber terminatePortNumber’
- To execute myftp, goto DCSProject2Client’s bin directory and run 'java com.ds.client.myftp ipAddress portNumber terminatePortNumber’

Note: This project was done in its entirety by Prasad Mate and Sharmin Pathan. We hereby state that we have not received unauthorized help of any form. The project description is taken from the description provided in CSCI 6780.
