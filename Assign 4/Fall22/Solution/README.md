##The Pair Guessing Game (similarities with Memory)

### Description:
This project utilizes the SocketServer and Socket classes to create a Server and Clients.  The Server and Clients communicate using protobuf in order to bridge any 
potential differences in language (which is not the case here but could happen in the future).  


### How to run it
Generate the proto files using

`gradle generateProto`

(This will also be done when building the project.) 

#### Default:
Server is Java per default on port 9099
`gradle runServer`

Clients is Java and runs per default on host localhost, port 9099
`gradle runClient`


#### With parameters:
`gradle runClient -Pport=9099 -Phost='localhost'`

`gradle runServer -Pport=9099`

Can also use `-q --console=plain` on both Client and Server if desired.

### How to work the program
The program accepts all inputs and will ask for different input if an invalid input is entered during the menu phase.

During the game, the answers are lowercase only (for simplicity sake) and will be marked wrong if the correct answer is given but typed with uppercase letters.

### Screencast Link


### Requirements Fulfilled
- [ ] (1) 
- [ ] (2) 
- [ ] (3)
- [ ] (4)
- [ ] (5)
- [ ] (6)
- [ ] (7)
- [ ] (8)
- [ ] (9)
- [ ] (10)
- [ ] (11)
- [ ] (12) 
- [ ] (13) 
- [ ] (14)
- [ ] (15) 
- [ ] (16) 
- [ ] (17) 
- [ ] (18) 
- [ ] (19) 
- [ ] (20) 
- [ ] (21) 
- [ ] (22) 
