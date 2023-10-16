package client;

import java.net.*;
import java.io.*;

import org.json.*;

import buffers.RequestProtos.Request;
import buffers.ResponseProtos.Response;
import buffers.ResponseProtos.Entry;

import java.util.*;
import java.util.stream.Collectors;

class SockBaseClient {

    public static void main (String args[]) throws Exception {
        Socket serverSock = null;
        OutputStream out = null;
        InputStream in = null;
        int i1 = 0, i2 = 0;
        int port = 9099; // default port

        // Make sure two arguments are given
        if (args.length != 2) {
            System.out.println("Expected arguments: <host(String)> <port(int)>");
            System.exit(1);
        }
        String host = args[0];
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be integer");
            System.exit(2);
        }

        // Ask user for username
        System.out.println("Please provide your name for the server.");
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        String strToSend = stdin.readLine();

        // Build the first request object just including the name
        Request op = Request.newBuilder()
                .setOperationType(Request.OperationType.NAME)
                .setName(strToSend).build();
        Response response;
        try {
            // connect to the server
            serverSock = new Socket(host, port);

            // write to the server
            out = serverSock.getOutputStream();
            in = serverSock.getInputStream();

            op.writeDelimitedTo(out);
            // read from the server
            response = Response.parseDelimitedFrom(in);

            // print the server response.
            System.out.println(response.getGreeting());

            boolean connected = true;

            while (connected) {
                System.out.println("What would you like to do? \n 1 - to see " +
                        "the leader board \n 2 - to enter a game \n 3 - to exit");

                // get player's choice from menu
                boolean invalidChoice;
                Request.Builder req = Request.newBuilder();
                do {
                    System.out.print("Your choice: ");
                    strToSend = stdin.readLine();
                    invalidChoice = false;

                    switch (strToSend) {
                        case ("1"):
                            req.setOperationType(Request.OperationType.LEADER);
                            break;
                        case ("2"):
                            req.setOperationType(Request.OperationType.NEW);
                            break;
                        case ("3"):
                            req.setOperationType(Request.OperationType.QUIT);
                            break;
                        default:
                            System.out.println("Invalid choice. Please try again.");
                            invalidChoice = true;
                            break;
                    }
                } while (invalidChoice);
                Request request = req.build();
                request.writeDelimitedTo(out);

                boolean continueHere = true;
                while (continueHere) {
                    Request.Builder req2 = Request.newBuilder();
                    response = Response.parseDelimitedFrom(in);
                    if (response.getResponseType() == Response.ResponseType.LEADER) {
                        System.out.println(response.getBoard());
                        System.out.println(response.getMessage());
                        for (Entry entry : response.getLeaderList()) {
                            System.out.println(entry.getWins() + ": " + entry.getName());
                        }
                        System.out.println();
                        continueHere = false;
                    } else if (response.getResponseType() == Response.ResponseType.PLAY) {
                        //System.out.println(response.getMessage()); //print message
                        if (response.hasFlippedBoard()){
                            System.out.println("\n" + response.getFlippedBoard());
                        } else {
                            System.out.println("\n" + response.getBoard());
                        }
                        if (response.hasEval()) {
                            if (response.getEval()){
                                System.out.println("You found a match");
                                System.out.println("\n" + response.getBoard());
                            } else {
                                System.out.println("Enter anything to continue: ");
                                String cont = stdin.readLine();
                                System.out.println("\n" + response.getBoard());
            
                            }


                        }
                        // 
                        System.out.print("Please enter the location of a tile: ");
                        String tile1 = stdin.readLine();
                        if (response.getSecond()) {
                            req2.setOperationType(Request.OperationType.TILE2);
                        } else {
                            req2.setOperationType(Request.OperationType.TILE1);
                        }
                        req2.setTile(tile1);
                        

                    } else if (response.getResponseType() == Response.ResponseType.WON) {
                        System.out.println("**** You Won! ****\n");
                        System.out.println("**** Final Board ****");
                        System.out.println(response.getBoard());
                        continueHere = false;
                    } else if (response.getResponseType() == Response.ResponseType.BYE) {
                        System.out.println(response.getMessage());
                        continueHere = false;
                        connected = false;
                    }

                    if (continueHere) {
                        Request request2 = req2.build();
                        request2.writeDelimitedTo(out);
                    }
                }
            }
        } catch (IOException ioE) {
            System.out.println("IOException");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("finally");
//			if (stdin != null)
			    stdin.close();
//            if (in != null)   in.close();
//            if (out != null)  out.close();
//            if (serverSock != null) serverSock.close();
            System.exit(0);
        }
    }
}


