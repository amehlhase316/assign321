package Assignment3Starter;
import java.net.*;
import java.util.Base64;
import java.util.Set;
import java.util.Stack;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import java.awt.image.BufferedImage;
import java.io.*;
import org.json.*;


/**
 * A class to demonstrate a simple client-server connection using sockets.
 * Ser321 Foundations of Distributed Software Systems
 * see http://pooh.poly.asu.edu/Ser321
 * @author Tim Lindquist Tim.Lindquist@asu.edu
 *         Software Engineering, CIDSE, IAFSE, ASU Poly
 * @version August 2020
 * 
 * @modified-by David Clements <dacleme1@asu.edu> September 2020
 */
public class SockServer {
	static Stack<String> imageSource = new Stack<String>();
	static Stack<String> questionSource = new Stack<String>();

	public static void main (String args[]) {
		Socket sock;
		try {

			questionSource.push("hat");
			questionSource.push("pug");
			questionSource.push("cucumber");
			questionSource.push("cat");
			questionSource.push("car");
			questionSource.push("puppy");


			int points = 0;
			int clientID = 0;
			int wrong = 0;
			int number = 0;
			int counter = 0;
			long startTime = System.currentTimeMillis();

			//open socket
			ServerSocket serv = new ServerSocket(8888,1); // create server socket on port 8888
			System.out.println("Server ready for connetion");

			System.out.println("Server waiting for a connection");

			GridMaker maker = new GridMaker();
			String filename;
			// create an object output writer (Java only)
			Stack<String> images = new Stack<String>();
			String answer = "Too funny for words";
			String answer2 = "";

			String name = "";
			int index = 0;

			// read in one object, the message. we know a string was written only by knowing what the client sent. 
			// must cast the object from Object to desired type to be useful
			while(true) {
				sock = serv.accept(); // blocking wait
				// setup the object reading channel
				ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
				OutputStream out = sock.getOutputStream();
				String s = (String) in.readObject();
				JSONObject json = new JSONObject(s);

				if (json.getString("type").equals("start")){
					System.out.println("- Got a start");
					if (clientID == 0 || clientID == json.getInt("clientID")) {
						clientID = json.getInt("clientID");
					} else {
						points = 0;
						JSONObject obj = new JSONObject();
						System.out.println("already connected");
						obj.put("type","error" );
						obj.put("value","busy" );
						PrintWriter outWrite = new PrintWriter(sock.getOutputStream(), true);
						outWrite.println(obj.toString());
						break;
					}
					JSONObject obj = new JSONObject();
					System.out.println("New connection");
					obj.put("type","hello" );
					obj.put("value","Hello, please tell me your name." );
					sendPic("img/hi.png", sock, obj);
					// PrintWriter outWrite = new PrintWriter(sock.getOutputStream(), true);
					// outWrite.println(obj.toString());
					
				} else if (json.getString("type").equals("name")){
					System.out.println("- Got a name");
					name = json.getString("value");
					JSONObject obj = new JSONObject();
					obj.put("type","hello" );
					obj.put("value","Hello " + name +  ", how many question would you like me to ask?" );
					PrintWriter outWrite = new PrintWriter(sock.getOutputStream(), true);
					outWrite.println(obj.toString());

				} else if (json.getString("type").equals("wrongStart")){
					System.out.println("-- Got a wrong start");
					JSONObject obj = new JSONObject();
					obj.put("type","wrongStart" );
					obj.put("value","Sorry I do not know what you want. You are only allowed to type 'start' at this stage." );
					PrintWriter outWrite = new PrintWriter(sock.getOutputStream(), true);
					outWrite.println(obj.toString());
				}
				else if (json.getString("type").equals("number")){
					System.out.println("- Got a number");
					number = json.getInt("value");
					JSONObject obj = new JSONObject();
					if (number > questionSource.size()){
						number = questionSource.size();
						obj.put("value","Thank you " + name +  ", I only have " + questionSource.size()+ " images, so I will use these. \n You have " + number * 30 + " seconds to guess them correctly. Type 'start' to get the first question." );
						
					} else {
						obj.put("value","Thank you " + name +  ", I will show you " + number + " different images to guess. You have " + number * 30 + " seconds to guess them correctly. Type 'start' to get the first question." );
					}

					obj.put("type","ready" );
					
					PrintWriter outWrite = new PrintWriter(sock.getOutputStream(), true);
					outWrite.println(obj.toString());
				} 
				else if (json.getString("type").equals("startGame")){
					startTime = System.currentTimeMillis();
					counter = 0;
					System.out.println("- Got a start game");
	
					answer2 = questionSource.pop();

					System.out.println(answer2);
					JSONObject obj = new JSONObject();
					obj.put("type","question" );
					obj.put("value", "What is this?");
					System.out.println("img/"+answer2+"/"+answer2+1+".png");
					sendPic("img/"+answer2+"/"+answer2+1+".png", sock, obj);
					index = 1;

				} else if (json.getString("type").equals("answer")){
					long end = System.currentTimeMillis();


					JSONObject obj = new JSONObject();
					if (end - startTime > number * 30 * 1000) {
						obj.put("type","lost" );
						if (json.getString("value").equals(answer2)){
							obj.put("value", "correct");
						} else {
							obj.put("value", "wrong");
						}
						
					sendPic("img/lose.jpg", sock, obj);
					}
					if (json.getString("value").equals(answer2)) {

						if (index == 1) points +=10;
						else if (index == 2) points +=5;
						else if (index == 3) points += 2;

						System.out.println("correct");
						counter++;
						if (counter == number) {
							obj.put("type","eval" );
							obj.put("value","won" );

							String winText = "You won, good job. ";

							if (points < number * 5) {
								winText = "You did win but you used the 'more' option a LOT :-( ! ";
							} else if (points < number * 8){
								winText = "You did win but you did use the 'more' option pretty frequently! ";
							}


							obj.put("text", winText + "You got " + points + " points.");
							obj.put("points",points );
							counter = 0;
							sendPic("img/win.jpg", sock, obj);
						} else {
							index = 1;
							answer2 = questionSource.pop();
							
							obj.put("type","question" );
							obj.put("value", "What is this?");
							
							sendPic("img/"+answer2+"/"+answer2+index+".png", sock, obj);
						}
					} else if (json.getString("value").equals("next")){
							System.out.println("Skip question");
							String old = answer2;
							answer2 = questionSource.pop();

							questionSource.push(old);

							System.out.println(answer2);
							obj.put("type","question" );
							obj.put("value", "What is this?");
							System.out.println("img/"+answer2+"/"+answer2+1+".png");
							sendPic("img/"+answer2+"/"+answer2+1+".png", sock, obj);
							index = 1;
						
					} else if (json.getString("value").equals("more")){
							System.out.println("More details: " + index);

							if (index == 3) {
								obj.put("type","question" );
								obj.put("value", "No more details available, you got to figure it out or type 'next'");
								sendPic("img/"+answer2+"/"+answer2+index+".png", sock, obj);
							}
							index += 1;
							obj.put("type","question" );
							obj.put("value", "Here you go with more details");
							sendPic("img/"+answer2+"/"+answer2+index+".png", sock, obj);
						
					} else {
						System.out.println("wrong");
						
						obj.put("type","eval" );
						obj.put("value","wrong" );
					
						PrintWriter outWrite = new PrintWriter(sock.getOutputStream(), true);
						outWrite.println(obj.toString());
					}
				} else {
					JSONObject obj = new JSONObject();
					System.out.println("not sure what you meant");
					obj.put("type","error" );
					obj.put("value","unknown request" );
					PrintWriter outWrite = new PrintWriter(sock.getOutputStream(), true);
					outWrite.println(obj.toString());
				}
			}
		} catch(Exception e) {e.printStackTrace();}
	}

	public static void sendPic(String filename, Socket sock, JSONObject obj) throws Exception {
		File file = new File(filename);

		int stringLength = filename.length();
		if (file.exists()) {
			// import image
			FileInputStream imageInFile = new FileInputStream(file);
			byte imageData[] = new byte[(int) file.length()];
			imageInFile.read(imageData);


			//Image conversion byte array in Base64 String

			String imageDataString = Base64.getEncoder().encodeToString(imageData);
			imageInFile.close();
			System.out.println("Image Successfully Manipulated!");


			obj.put("image",imageDataString);

			PrintWriter outWrite = new PrintWriter(sock.getOutputStream(), true);
			outWrite.println(obj.toString());
		} 

	}
}
