package ServerClient;

import java.io.*;
import java.net.*;

public class History {
	static FileInputStream in = null;
	static FileOutputStream out = null;
	static BufferedReader reader = null;
	
	String sessionID;
	String clientA;
	String clientB;
	
	
	History(String sessionID, String clientA, String clientB) {
		this.sessionID = sessionID;
		this.clientA = clientA;
		this.clientB = clientB;
	}
	
	// Save message sent between client
	public static void save(String sessionID, String client, String message) throws IOException{
		// Open FileOutputStream to sessionID.txt.
		// true argument gives us appending mode.
		out = new FileOutputStream((sessionID + ".txt"), true);
		String text = client + ": " + message;
		
		// Append text to file
		out.write(text.getBytes());
		
		// Close out after writing message
		out.close();
	}
	
	// Load message sent from client
	public static void load(String sessionID, PrintWriter out) throws IOException {
		// Open sessionID.txt FileInputStream
		in = new FileInputStream(sessionID + ".txt");
		reader = new BufferedReader(new InputStreamReader(in));
		
		// Read next line of file
		String line = reader.readLine();
		
		// Loop through file and send out to PrintWriter
		// PrintWriter should be connected to 
		// associated clientSocket.getOutputStream()
		while (line != null) {
			// FIXME: Might not need to be println, could be print
			out.println(line);
			line = reader.readLine();
		}
	}
}
