package server;

import java.net.*;
import java.io.*;
import java.util.*;
import org.json.*;
import java.lang.*;

import buffers.RequestProtos.Request;
import buffers.RequestProtos.Logs;
import buffers.RequestProtos.Message;
import buffers.ResponseProtos.Response;
import buffers.ResponseProtos.Entry;

class SockBaseServer implements Runnable{
    static String logFilename = "logs.txt";
    static String leaderboardFile = "leaderboard.json";

    InputStream in = null;
    OutputStream out = null;
    Socket clientSocket;
    Game game;

    public SockBaseServer(Socket sock, Game game){
        this.clientSocket = sock;
        this.game = game;
        try {
            in = clientSocket.getInputStream();
            out = clientSocket.getOutputStream();
        } catch (Exception e){
            System.out.println("Error in constructor: " + e);
        }
    }

    // Handles the communication right now it just accepts one input and then is done you should make sure the server stays open
    // can handle multiple requests and does not crash when the server crashes
    // you can use this server as based or start a new one if you prefer. 
    public void run() {
        try {
            String name = "";
            int modifier;

            System.out.println("Ready...");
            try {
                // read the proto object and put into new objct
                Request op = Request.parseDelimitedFrom(in);
                String result = null;


                    // if the operation is NAME (so the beginning then say there is a commention and greet the client)
                    if (op.getOperationType() == Request.OperationType.NAME) {
                        // get name from proto object
                        name = op.getName();

                        // writing a connect message to the log with name and CONNENCT
                        writeToLog(name, Message.CONNECT);
                        writeToLeaderboard(name, false, true);
                        System.out.println("Got a connection and a name: " + name);
                    }

                    Response response = Response.newBuilder()
                            .setResponseType(Response.ResponseType.GREETING)
                            .setGreeting("Hello " + name + " and welcome.")
                            .build();
                    response.writeDelimitedTo(out);

                while (true) {
                    Request req = Request.parseDelimitedFrom(in);
                    if (req.getOperationType() == Request.OperationType.LEADER) {
                        System.out.println("Sending Leaderboard.");

                        ArrayList<Entry> entries = readLeaderboard();
                        Response.Builder resp = Response.newBuilder()
                                .setResponseType(Response.ResponseType.LEADER)
                                .setMessage("******** Leaderboard ********");
                        for (Entry entry : entries) {
                            resp.addLeader(entry);
                        }
                        Response leaders = resp.build();
                        leaders.writeDelimitedTo(out);
                    } else if (req.getOperationType() == Request.OperationType.NEW) {
                        // Example how to start a new game and how to build a response with the image which you could then send to the server
                        // LINE 67-108 are just an example for Protobuf and how to work with the differnt types. They DO NOT
                        // belong into this code.
//                    if (!gameInProgress) {
                        game.newGame(); // starting a new game
                        modifier = findOnLeaderboard(name);
//                    }

                        boolean hasAnswered = false;
                        boolean answerCorrect = false;
                        boolean error = false;
                        String message = "";
                        String flippedBoard = "";
                        boolean second = false;
                        int tile1row = 8;
                        int tile1col = 8;
                        char tile1= 0;
                        int tile2row =8;
                        int tile2col = 8;
                        char tile2 = 0;
                        boolean start = true;
                        while (!game.getWon()) {
//                            int randomInt = rand.nextInt() % questions.length;
//                            if (randomInt < 0) {
//                                randomInt = randomInt * -1;
//                            }
//                            System.out.println(randomInt);
//                            String randQuestion = questions[randomInt];
//                            String randAnswer = answers[randomInt];
//
                            // adding the String of the game to
                            Response.Builder res2 = Response.newBuilder()
                                    .setResponseType(Response.ResponseType.PLAY)
                                    .setMessage(message)
                                    .setErr(error)
                                    .setBoard(game.getBoard());
                            if (!start) {
                                res2.setFlippedBoard(flippedBoard);
                            } else {
                                start = false;
                            }
                            if (hasAnswered) {
                                res2.setEval(answerCorrect);
                            }
                            if (second) {
                                res2.setSecond(true);
                            }

                            Response response2 = res2.build();
                            response2.writeDelimitedTo(out);
                            message = "";
                            flippedBoard = "";
                            error = false;

                            // On the client side you would receive a Response object which is the same as the one in line 70, so now you could read the fields
                            System.out.println("Task: " + response2.getResponseType());
                            System.out.println("Board: \n" + game.showBoard());

                            Request answer = Request.parseDelimitedFrom(in);


                            System.out.println(answer.toString());

                            if (answer.getOperationType() == Request.OperationType.TILE1) {
                                second = true;
                                tile1row = answer.getTile().charAt(0) - 'a' + 1;
                                tile1col = (answer.getTile().charAt(1) - '0') * 2;
                                tile1 = game.getTile(tile1row, tile1col);
                                System.out.println("Tile1: " + tile1);
                                if (tile1 == '?') {
                                    message = "Invalid entry. Please try again.";
                                    error = true;
                                    System.out.println(message);
                                    System.out.println(error);
                                }
                            } else {
                                second = false;
                                tile2row = answer.getTile().charAt(0) - 'a' + 1;
                                tile2col = (answer.getTile().charAt(1) - '0') * 2;
                                tile2 = game.getTile(tile2row, tile2col);
                                System.out.println("Tile2: " + tile2);
                                if (tile2 == '?') {
                                    message = "Invalid entry. Please try again.";
                                    error = true;
                                    System.out.println(message);
                                    System.out.println(error);
                                }
                            }

                            
                            if (answer.getOperationType() == Request.OperationType.TILE2 && tile1 == tile2 && ((tile1row!=tile2row) || tile1col!=(tile2col))) {
                                game.replaceOneCharacter(tile1row, tile1col);
                                game.replaceOneCharacter(tile2row, tile2col);
                                answerCorrect = true;
                                hasAnswered = true;
                                message = "You found a match!";
                            } 
                            else if (answer.getOperationType() == Request.OperationType.TILE1) {
                                flippedBoard = game.tempFlipWrongTiles(tile1row, tile1col);
                                System.out.println("flipped?");
                                System.out.println(flippedBoard);
                                error = false;
                                hasAnswered = false;
                            } else {
                                flippedBoard = game.tempFlipWrongTiles(tile1row, tile1col, tile2row, tile2col);
                                System.out.println("flipped?");
                                System.out.println(flippedBoard);
                                answerCorrect = false;
                                error = false;
                                hasAnswered = true;
                            }
                            
                            game.checkWin();
                        }
                        // start = true;
                        Response won = Response.newBuilder()
                                .setResponseType(Response.ResponseType.WON)
                                .setBoard(game.getBoard())
                                .setMessage(message)
                                .build();
                        won.writeDelimitedTo(out);
                        writeToLeaderboard(name, true, false);
                    } else if (req.getOperationType() == Request.OperationType.QUIT) {
                        Response goodbye = Response.newBuilder()
                                .setResponseType(Response.ResponseType.BYE)
                                .setMessage("**** Goodbye " + name + ". See you soon! ****")
                                .build();
                        goodbye.writeDelimitedTo(out);
                        break;
                    }
                }
            } catch (SocketException connectionException) {
                if (out != null) out.close();
                if (in != null) in.close();
                if (clientSocket != null) clientSocket.close();
            } catch (Exception ex) {
                System.out.println(ex.toString() + " occurred");
            } finally {
                if (out != null) out.close();
                if (in != null) in.close();
                if (clientSocket != null) clientSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Could not close properly");
        }
    }

    /**
     * Writing a new entry to our log
     * @param name - Name of the person logging in
     * @param message - type Message from Protobuf which is the message to be written in the log (e.g. Connect) 
     * @return String of the new hidden image
     */
    public synchronized static void writeToLog(String name, Message message){
        try {
            // read old log file 
            Logs.Builder logs = readLogFile();

            // get current time and data
            Date date = java.util.Calendar.getInstance().getTime();

            // we are writing a new log entry to our log
            // add a new log entry to the log list of the Protobuf object
            logs.addLog(date.toString() + ": " +  name + " - " + message);

            // open log file
            FileOutputStream output = new FileOutputStream(logFilename);
            Logs logsObj = logs.build();

            // This is only to show how you can iterate through a Logs object which is a protobuf object
            // which has a repeated field "log"

            for (String log: logsObj.getLogList()){

                System.out.println(log);
            }

            // write to log file
            logsObj.writeTo(output);
        }catch(Exception e){
            System.out.println("Issue while trying to save");
        }
    }

    /**
     * Reading the current log file
     * @return Logs.Builder a builder of a logs entry from protobuf
     */
    public synchronized static Logs.Builder readLogFile() throws Exception{
        Logs.Builder logs = Logs.newBuilder();

        try {
            // just read the file and put what is in it into the logs object
            return logs.mergeFrom(new FileInputStream(logFilename));
        } catch (FileNotFoundException e) {
            System.out.println(logFilename + ": File not found.  Creating a new file.");
            return logs;
        }
    }

    public synchronized static void writeToLeaderboard(String name, boolean won, boolean connect) {
        try {
            ArrayList<Entry> entries = readLeaderboard();

            int wins = 0;
            int logins = 0;
            if (won) {
                wins = 1;
            }
            if (connect) {
                logins = 1;
            }
            boolean foundPlayer = false;
            int i = 0;
            while (i < entries.size()) {
                Entry e = entries.get(i);
                if (e.getName().equals(name)) {
                    Entry found = Entry.newBuilder()
                            .setName(name)
                            .setWins(e.getWins() + wins)
                            .setLogins(e.getLogins() + logins)
                            .build();

                    entries.remove(i);
                    entries.add(found);
                    foundPlayer = true;
                    break;
                }
                i++;
            }

            if (!foundPlayer) {
                Entry newEntry = Entry.newBuilder()
                        .setName(name)
                        .setWins(wins)
                        .setLogins(logins)
                        .build();
                entries.add(newEntry);
            }

            JSONObject array = new JSONObject();
            for(Entry value : entries) {
                JSONObject json = new JSONObject();
                json.put("wins", value.getWins());
                json.put("logins", value.getLogins());
                array.put(value.getName(), json);
            }

            FileWriter file = new FileWriter(leaderboardFile);
            file.write(array.toString());
            file.close();

            // This is only to show how you can iterate through a Logs object which is a protobuf object
            // which has a repeated field "log"

            System.out.println(array.toString());
        }catch(Exception e){
            System.out.println("Issue while trying to save");
        }
    }

    public synchronized static ArrayList<Entry> readLeaderboard() throws Exception {
        ArrayList<Entry> entries = new ArrayList<Entry>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(leaderboardFile));
            String json = reader.readLine();
            JSONObject jsonArray = new JSONObject(new JSONTokener(json));
            for (Iterator<String> names = jsonArray.keys(); names.hasNext(); ) {
                String name = names.next();
                JSONObject jsonEntry = jsonArray.getJSONObject(name);
                Entry entry = Entry.newBuilder()
                        .setName(name)
                        .setWins(jsonEntry.getInt("wins"))
                        .setLogins(jsonEntry.getInt("logins"))
                        .build();
                entries.add(entry);
            }
            reader.close();
            return entries;
        } catch (FileNotFoundException e) {
            System.out.println(leaderboardFile + ": File not found.  Creating a new file.");
            return entries;
        }
    }

    public synchronized static int findOnLeaderboard(String name) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(leaderboardFile));
            String json = reader.readLine();
            JSONObject jsonList = new JSONObject(new JSONTokener(json));
            return jsonList.getJSONObject(name).getInt("wins");
        } catch (Exception e) {
            System.out.println("Error reading from " + leaderboardFile);
            return 1;
        }
    }

    public static void main (String args[]) throws Exception {
        Game game = new Game();

        if (args.length != 2) {
            System.out.println("Expected arguments: <port(int)> <delay(int)>");
            System.exit(1);
        }
        int port = 9099; // default port
        int sleepDelay = 10000; // default delay
        Socket clientSocket = null;
        ServerSocket serv = null;

        try {
            port = Integer.parseInt(args[0]);
            sleepDelay = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port|sleepDelay] must be an integer");
            System.exit(2);
        }
        try {
            serv = new ServerSocket(port);
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(2);
        }

        while (true) {
            clientSocket = serv.accept();
            SockBaseServer server = new SockBaseServer(clientSocket, game);
            Thread thread = new Thread(server);
            thread.start();
        }
    }
}

