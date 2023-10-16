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

import java.util.Random;


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
			JSONObject leader = new JSONObject();



			int points = 10;
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
			String currentString = "";

			// read in one object, the message. we know a string was written only by knowing what the client sent. 
			// must cast the object from Object to desired type to be useful
			while(true) {
				sock = serv.accept(); // blocking wait
				// setup the object reading channel
				ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
				OutputStream out = sock.getOutputStream();
				String s = (String) in.readObject();
				JSONObject json = new JSONObject(s);
				JSONObject obj = new JSONObject();


				if (json.getString("type").equals("start")){
					System.out.println("- Got a start");
					if (clientID == 0 || clientID == json.getInt("clientID")) {
						clientID = json.getInt("clientID");
					} else {
						points = 0;
						System.out.println("already connected");
						obj.put("type","error" );
						obj.put("value","busy" );
						PrintWriter outWrite = new PrintWriter(sock.getOutputStream(), true);
						outWrite.println(obj.toString());
						break;
					}
					System.out.println("New connection");
					obj.put("type","hello" );
					obj.put("value","Hello, please tell me your name." );
					sendPic("img/hi.png", sock, obj);
					// PrintWriter outWrite = new PrintWriter(sock.getOutputStream(), true);
					// outWrite.println(obj.toString());
					
				} else if (json.getString("type").equals("name")){
					points = 10;
					System.out.println("- Got a name");
					name = json.getString("value");
					obj.put("type","hello" );
					obj.put("value","Hello " + name +  ", would you like to guess a citi (ci) or a country (co) or see the leaderboard (leader)?" );
					PrintWriter outWrite = new PrintWriter(sock.getOutputStream(), true);
					outWrite.println(obj.toString());

				} 
				else if (json.getString("type").equals("co/ci")){
					System.out.println("- Got a co/ci");
					String coci = json.getString("value");
					System.out.println(coci);

					obj.put("type","question" );


					if (coci.equals("co")){

						// File f = new File("img/country");
						// String[] list = f.list();

						// Random rand = new Random(); //instance of random class
						// System.out.println(rand);
      // 					int int_random = rand.nextInt(list.length-1);

      // 					String image = list[int_random+1];

      // 					System.out.println(image);

      					File f = new File("img/country");
						String image = getImage(f);
      					answer = image.split("\\.")[0];

						currentString = "";
						for (int i = 0; i < answer.length(); i++) {

				            currentString += "_ ";
				        }
				        
						obj.put("blanks", currentString);
						obj.put("value","Thank you " + name +  ", I will show you a picture of a country and you have to guess which one it is." );
						obj.put("points", points);
						sendPic("img/country/" + image, sock, obj);
					}
					else if (coci.equals("ci")) {
						File f = new File("img/city");
						String image = getImage(f);
						
      					answer = image.split("\\.")[0];
  

						obj.put("value","Thank you " + name +  ", I will show you a picture of a city and you have to guess which one it is" );
						currentString = "";
						for (int i = 0; i < answer.length(); i++) {

				            currentString += "_ ";
				        }

						obj.put("points", points);
						obj.put("blanks", currentString);
						sendPic("img/city/"+image, sock, obj);
					} else if (coci.equals("leader")) {
						System.out.println("- Got a leader");
						obj.put("type","leader" );
						obj.put("value", leader);
						PrintWriter outWrite = new PrintWriter(sock.getOutputStream(), true);
						outWrite.println(obj.toString());
					}
					else {
						obj.put("type","error" );
						obj.put("value","unknown request" );
					}
					
					PrintWriter outWrite = new PrintWriter(sock.getOutputStream(), true);
					outWrite.println(obj.toString());
				} else if (json.getString("type").equals("answer")){
					long end = System.currentTimeMillis();

					if (json.getString("value").length() == 1) {
						System.out.println(answer.contains(json.getString("value")));
						if (answer.contains(json.getString("value"))){
							int index1 = answer.indexOf(json.getString("value"));
							StringBuilder shortTerm = new StringBuilder(currentString);
							System.out.println(index1);
							int test = index1 * 2;
							shortTerm.setCharAt(test, json.getString("value").charAt(0));
							obj.put("blanks", shortTerm.toString());

							if (shortTerm.toString().contains("_")){
								obj.put("type", "found");
								points += 1;
								obj.put("points", points);
								
							} else {
								obj.put("type", "won");
								points += 5;
								obj.put("points", points);
								if (leader.has(name)) {
									leader.put(name, leader.getInt(name) + points);
								} else {
									leader.put(name, points);
								}

							}
							obj.put("points", points);
							PrintWriter outWrite = new PrintWriter(sock.getOutputStream(), true);
							currentString = shortTerm.toString();
							outWrite.println(obj.toString());

						}
						else { 
							
							points -= 1;
							if (points <= 0) {
								obj.put("type", "lost");
								obj.put("blanks", answer);
								obj.put("points", points);
								sendPic("img/lose.jpg", sock, obj);
							} else{
								obj.put("type", "wrong");
								obj.put("points", points);
								obj.put("blanks", currentString);
							}
							PrintWriter outWrite = new PrintWriter(sock.getOutputStream(), true);
							outWrite.println(obj.toString());
						}
					} else { 
						if (json.getString("value").equals(answer)) {
							System.out.println("won");

							obj.put("type", "won");
							points += 5;
							obj.put("points", points);
							obj.put("blanks", answer);
							if (leader.has(name)) {
								leader.put(name, leader.getInt(name) + points);
							} else {
								leader.put(name, points);
							}


							sendPic("img/win.jpg", sock, obj);
						} else {

							System.out.println("wrong");
							points -= 5;
							points -= 1;
							if (points <= 0) {
								obj.put("type", "lost");
								obj.put("blanks", answer);
								obj.put("points", points);
								sendPic("img/lose.jpg", sock, obj);
							} else{
								obj.put("type", "wrong");
								obj.put("points", points);
								obj.put("blanks", currentString);
							}
	
							PrintWriter outWrite = new PrintWriter(sock.getOutputStream(), true);
							outWrite.println(obj.toString());
						}
					}
					
				} else if (json.getString("value").equals("help")){
					System.out.println("get a letter");
					String old = answer2;
					answer2 = questionSource.pop();

					questionSource.push(old);

					System.out.println(answer2);
					obj.put("type","question" );
					obj.put("value", "What is this?");
					System.out.println("img/"+answer2+"/"+answer2+1+".png");
					sendPic("img/"+answer2+"/"+answer2+1+".png", sock, obj);
					index = 1;

				} else if (json.getString("value").equals("next")){
					System.out.println("get new word");

					if (index == 3) {
						obj.put("type","question" );
						obj.put("value", "No more details available, you got to figure it out or type 'next'");
						sendPic("img/"+answer2+"/"+answer2+index+".png", sock, obj);
					}
					index += 1;
					obj.put("type","question" );
					obj.put("value", "Here you go with more details");
					sendPic("img/"+answer2+"/"+answer2+index+".png", sock, obj);

				} 

				else {
					System.out.println("not sure what you meant");
					obj.put("type","error" );
					obj.put("value","unknown request" );
					PrintWriter outWrite = new PrintWriter(sock.getOutputStream(), true);
					outWrite.println(obj.toString());
				} }
			} catch(Exception e) {e.printStackTrace();}
		}

		public static String getImage(File f) throws Exception {
			String[] list = f.list();

			Random rand = new Random(); //instance of random class
			System.out.println(rand);
			int int_random = rand.nextInt(list.length-1);

			String image = list[int_random+1];
			System.out.println(image);

			if (image.equals(".DS_Store")){
				image = getImage(f);
			} 
			return image;
			

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
