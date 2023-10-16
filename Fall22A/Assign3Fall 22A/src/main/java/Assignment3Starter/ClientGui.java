package Assignment3Starter;

import java.awt.Dimension;

import org.json.*;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Base64;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

/**
 * The ClientGui class is a GUI frontend that displays an image grid, an input text box,
 * a button, and a text area for status. 
 * 
 * Methods of Interest
 * ----------------------
 * show(boolean modal) - Shows the GUI frame with the current state
 *     -> modal means that it opens the GUI and suspends background processes. Processing 
 *        still happens in the GUI. If it is desired to continue processing in the 
 *        background, set modal to false.
 * newGame(int dimension) - Start a new game with a grid of dimension x dimension size
 * insertImage(String filename, int row, int col) - Inserts an image into the grid
 * appendOutput(String message) - Appends text to the output panel
 * submitClicked() - Button handler for the submit button in the output panel
 * 
 * Notes
 * -----------
 * > Does not show when created. show() must be called to show he GUI.
 * 
 */
public class ClientGui implements Assignment3Starter.OutputPanel.EventHandlers {
	JDialog frame;
	PicturePanel picturePanel;
	OutputPanel outputPanel;
	boolean gameStarted = false;
	String currentMessage;
	Socket sock;
	OutputStream out;
	ObjectOutputStream os;
	BufferedReader bufferedReader;
	int errors= 0;
	String id;
	int status = 0; // 0-no game, 1-wait for name, 2-wait for number, 3-wait for start, 4-in progress

	String host = "localhost";
	int port = 9000;

	/**
	 * Construct dialog
	 * @throws IOException 
	 */
	public ClientGui(String host, int port, String id) throws IOException {
		this.host = host;
		this.port = port;
		this.id = id;
//		open();

		frame = new JDialog();
		frame.setLayout(new GridBagLayout());
		frame.setMinimumSize(new Dimension(500, 500));
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);


		// setup the top picture frame
		picturePanel = new PicturePanel();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 0.25;
		frame.add(picturePanel, c);

		// setup the input, button, and output area
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.weighty = 0.75;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		outputPanel = new OutputPanel();
		outputPanel.addEventHandlers(this);
		frame.add(outputPanel, c);

		picturePanel.newGame(1);

		open();
		currentMessage = "{'type': 'start', 'clientID': "+ this.id + "}";
		try {
			os.writeObject(currentMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String string = this.bufferedReader.readLine();
		System.out.println("Got a connection to server");
		JSONObject json = new JSONObject(string);
		outputPanel.appendOutput(json.getString("value"));
		status = 1;
		byte[] imageByteArray = Base64.getDecoder().decode(json.getString("image"));

		ByteArrayInputStream bis = new ByteArrayInputStream(imageByteArray);
		try {
			picturePanel.insertImage(bis, 0, 0);
		} catch (Exception e){
			System.out.println(e);
		}
	}

	/**
	 * Shows the current state in the GUI
	 * @param makeModal - true to make a modal window, false disables modal behavior
	 */
	public void show(boolean makeModal) {
		frame.pack();
		frame.setModal(makeModal);
		frame.setVisible(true);
	}

	/**
	 * Creates a new game and set the size of the grid 
	 * @param dimension - the size of the grid will be dimension x dimension
	 */
	public void newGame(int dimension) {
		picturePanel.newGame(1);
		outputPanel.appendOutput("Started new game with a " + dimension + "x" + dimension + " board.");
	}

	/**
	 * Insert an image into the grid at position (col, row)
	 * 
	 * @param filename - filename relative to the root directory
	 * @param row - the row to insert into
	 * @param col - the column to insert into
	 * @return true if successful, false if an invalid coordinate was provided
	 * @throws IOException An error occured with your image file
	 */
	public boolean insertImage(String filename, int row, int col) throws IOException {
		System.out.println("Image insert");
		String error = "";
		try {
			// insert the image
			if (picturePanel.insertImage(filename, row, col)) {
				// put status in output
				outputPanel.appendOutput("Inserting " + filename + " in position (" + row + ", " + col + ")");
				return true;
			}
			error = "File(\"" + filename + "\") not found.";
		} catch(PicturePanel.InvalidCoordinateException e) {
			// put error in output
			error = e.toString();
		}
		outputPanel.appendOutput(error);
		return false;
	}

	/**
	 * Submit button handling
	 * 
	 * Change this to whatever you need
	 */
	@Override
	public void submitClicked() {
		try {
			open();
			System.out.println("submit");
		// Pulls the input box text
			String input = outputPanel.getInputText();

		// if has input

			if (!gameStarted) {
				if (input.length() > 0) {
				// append input to the output panel
					outputPanel.appendOutput(input);
				// clear input text box
					outputPanel.setInputText("");

				}
				gameStarted = true;
				currentMessage = "{'type': 'name', 'clientID': "+ this.id +",'value' : '"+input+"'}";
				try {
					os.writeObject(currentMessage);
				} catch (IOException e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				if (status == 2) {
					System.out.println("Would you like to guess countries (co) or cities (ci)?");
					outputPanel.appendOutput("   " + input);
					currentMessage = "{'type': 'co/ci', 'clientID': '"+ this.id +"','value' : '"+input+"'}";
				} else if (status == 3 && input.equals("start")){
					System.out.println("Send a start");
					outputPanel.appendOutput("   " + input);
					currentMessage = "{'type': 'startGame', 'clientID': '"+ this.id +"'}";
				} else if (status == 3 && !input.equals("start")){
					outputPanel.appendOutput("   " + input);
					System.out.println("Wrong start");
				//outputPanel.appendOutput("I do not know what you want from me, the only thing you are allowed to enter is 'start'");
					outputPanel.setInputText("");
					currentMessage = "{'type': 'wrongStart', 'clientID': '"+ this.id +"'}";
				} else {
					System.out.println("Send an answer");
					outputPanel.appendOutput("   " + input);
					currentMessage = "{'type': 'answer', 'clientID': '"+ this.id +"','value' : '"+input+"'}";
				}
				try {
					os.writeObject(currentMessage);
				} catch (IOException e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			try {
				System.out.println("Waiting on response");
				String string = this.bufferedReader.readLine();
				JSONObject json = new JSONObject(string);

				System.out.println("Got a " + json.getString("type"));

				if (json.getString("type").equals("question")) {
					status =4;
					if (json.has("image")){
						byte[] imageByteArray = Base64.getDecoder().decode(json.getString("image"));

						ByteArrayInputStream bis = new ByteArrayInputStream(imageByteArray);
						picturePanel.insertImage(bis, 0 ,0);
						outputPanel.setPoints(json.getInt("points"));
					}
					System.out.println("Question ");
					outputPanel.appendOutput(json.getString("value"));
					outputPanel.setBlanks(json.getString("blanks"));
					status = 4;
				}
				else if (json.getString("type").equals("leader")) {
					System.out.println("Got Leaderboard");
					outputPanel.appendOutput(json.getJSONObject("value").toString());
				}
				else if (json.getString("type").equals("found")){
					outputPanel.setBlanks(json.getString("blanks"));
					outputPanel.setPoints(json.getInt("points"));
				}
				else if (json.getString("type").equals("won")){
					outputPanel.setBlanks(json.getString("blanks"));
					outputPanel.setPoints(json.getInt("points"));
					outputPanel.appendOutput("You won, nice job!");
					gameStarted = false;
					outputPanel.appendOutput("Type your name if you want to start again.");
				} else if (json.getString("type").equals("wrong")){
					outputPanel.setBlanks(json.getString("blanks"));
					outputPanel.setPoints(json.getInt("points"));
				}
				 else if (json.getString("type").equals("error")) {
					System.out.println("Server is busy");
					System.exit(0);
				} else if (json.getString("type").equals("lost")) {
					outputPanel.setPoints(json.getInt("points"));
					outputPanel.setBlanks(json.getString("blanks"));
					outputPanel.appendOutput("YOU LOST, you have 0 or less points");
					byte[] imageByteArray = Base64.getDecoder().decode(json.getString("image"));

					ByteArrayInputStream bis = new ByteArrayInputStream(imageByteArray);
					picturePanel.insertImage(bis, 0 ,0);
					gameStarted = false;
					outputPanel.appendOutput("Type your name if you want to start again.");
				} else if (json.getString("type").equals("hello")) {
					System.out.println("Got start message");
					outputPanel.appendOutput(json.getString("value"));

					status = 0;
					gameStarted = true;
					status = 2;
				} else if (json.getString("type").equals("ready")) {
					outputPanel.appendOutput(json.getString("value"));
					gameStarted = true;
					status = 3;
				} else if (json.getString("type").equals("wrongStart")) {
					outputPanel.appendOutput(json.getString("value"));
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			outputPanel.setInputText("");
			close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	/**
	 * Key listener for the input text box
	 * 
	 * Change the behavior to whatever you need
	 */
	@Override
	public void inputUpdated(String input) {
		if (input.equals("surprise")) {
			outputPanel.appendOutput("You found me!");
		}
	}

	public void open() throws UnknownHostException, IOException {
		this.sock = new Socket(host, port); // connect to host and socket on port 8888

		// get output channel
		this.out = sock.getOutputStream();
		// create an object output writer (Java only)
		this.os = new ObjectOutputStream(out);
		this.bufferedReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));

	}
	
	public void close() {
		try {
			if (out != null)  out.close();
			if (bufferedReader != null)   bufferedReader.close(); 
			if (sock != null) sock.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		// create the frame


		try {
			String host = "localhost";
			int port = 8888;


			ClientGui main = new ClientGui(host, port, args[0]);
			main.show(true);


		} catch (Exception e) {e.printStackTrace();}



	}
}
