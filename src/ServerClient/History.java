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
	public static void save(String clientIDA, String clientIDB, String message) throws IOException{
		// Open FileOutputStream to sessionID.txt.
		// true argument gives us appending mode.
		out = new FileOutputStream((clientIDB + ".txt"), true);
		String text = clientIDA + "-" + clientIDB + ": " + message + "\n";
		
		// Append text to B file history
		out.write(text.getBytes());
		
		// Write to A file history
		out = new FileOutputStream ((clientIDA + ".txt"), true);
		text = clientIDB + "-" + clientIDA + ": " + message + "\n";
		out.write(text.getBytes());
		
		// Close out after writing message
		out.close();
	}
	
	// Load message sent from client
	public static void load(String clientIDA, String clientIDB, PrintWriter out, String CK_A) throws IOException {
		// Open sessionID.txt FileInputStream
		File tmpDir = new File(clientIDB + ".txt");
		boolean exists = tmpDir.exists();
		
		if (exists) {
			in = new FileInputStream(clientIDB + ".txt");
			reader = new BufferedReader(new InputStreamReader(in));

			// Read next line of file
			String line = reader.readLine();
			
			// Loop through file and send out to PrintWriter
			// PrintWriter should be connected to 
			// associated clientSocket.getOutputStream()
			while (line != null) {
				// HIS_RESPONSE sent out multiple times.
				if ((line.matches(clientIDA + "-" + clientIDB + ": .*"))
						|| (line.matches(clientIDB + "-" + clientIDA + ": .*"))) {
					out.println(AES.encrypt(line, CK_A));
					line = reader.readLine();
				}
			}
		}
		
	}
}
