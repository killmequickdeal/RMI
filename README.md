#Distributed-Computing-Project-2 (File Transfer)

From the directory this README is in the following commands can be used

https://github.com/killmequickdeal/RMI

## Commands

### make

Build the .class files

### make clean
Clean up .class files, executables, etc

### Running the server-side
make run-server   (NOTE: This should be done on in-csci-rrpc01.cs.iupui.edu as the clients are currently hard coded to look here)

### Running the client-side
make run-client

In order to run the code as intended, make, then run the server on in-csci-rrpc01.cs.iupui.edu. Next run 4 clients on rrpc02,rrpc03,rrpc04,rrpc05 (although these do not matter as long as the server can see rrpc01 server). 

## Examples:

[rjdeal@in-csci-rrpc03 RMI]$ make clean
rm -f ./src/src/RegistrationService.class ./src/src/DetectionNode\$1.class ./src/src/DetectionNode.class ./src/src/RemoteNode.class ./src/src/Message.class ./src/src/Server.class
[rjdeal@in-csci-rrpc03 RMI]$

[rjdeal@in-csci-rrpc03 RMI]$ make
javac  src/src/*.java
[rjdeal@in-csci-rrpc03 RMI]$

[rjdeal@in-csci-rrpc03 RMI]$ make run-server
java -Djava.security.policy=policy  -cp src/src/ Server 1997
Server Ready!

[rjdeal@in-csci-rrpc03 RMI]$ make run-client
java -Djava.security.policy=policy  -cp src/src/ DetectionNode
nodes currently connected: 1
Waiting for all four Detection nodes to register
