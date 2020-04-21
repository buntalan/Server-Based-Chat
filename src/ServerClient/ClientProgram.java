package ServerClient;

import java.net.*;
import java.util.*;
import java.io.*;

public class ClientProgram {
	public static void main (String[] args) throws Exception {
		String received = null;
		String response = null;
		boolean running;
		
		// Create client instance
		Client client = new Client();
		client.setSocket(new DatagramSocket());
		client.setAddress(InetAddress.getLocalHost());
				
		// FIXME: Setting custom Client_ID and key for testing
		client.setClient_ID("A");
		client.setKey(4);
		
		// Set up ClientProgram for receiving data UDP Datagrams
		// FIXME: Uncomment when done debugging.
		// client.getSocket().setSoTimeout(1000);
		client.setBuf(new byte[65535]);
		client.setPacket(new DatagramPacket(client.getBuf(),
				client.getBuf().length));
		
		// User has to log on
		// FIXME: Comment out for debugging
		// loggingOn();
		
		
		/********************
		 * Connection Phase *
		 *******************/
		
		// Establish Connection
		client.HELLO(client.getClient_ID());
		
		// Receive response for HELLO
		client.setBuf(new byte[65535]);
		client.setPacket(new DatagramPacket(client.getBuf(), client.getBuf().length));
		client.getSocket().receive(client.getPacket());
		received = Client.data(client.getBuf()).toString();
		
		// FIXME: For testing
		System.out.println(received);
		
		// If client is not subscriber, will receive FFFF as response from server.
		if (received.contentEquals("FFFF")) {
			System.out.println("Client is not a subscriber.");
		}
		// Else send out response to CHALLENGE
		else {
			// Create and send out RESPONSE to CHALLENGE
			String RES = new String(AES.encrypt(String.valueOf(received) + String.valueOf(client.getKey()), 
					String.valueOf(client.getKey())));
			// FIXME: Testing response
			System.out.println(AES.decrypt(RES, String.valueOf(client.getKey())));
			client.RESPONSE(client.getClient_ID(), RES, client.getPacket().getAddress(), client.getPacket().getPort());
			
			// Receive response for RESPONSE
			// and reset buffer
			client.setBuf(new byte[65535]);
			client.setPacket(new DatagramPacket(client.getBuf(), client.getBuf().length));
			client.getSocket().receive(client.getPacket());
			received = Client.data(client.getBuf()).toString();
			
			// TODO: Set up branch for AUTH_SUCCESS and AUTH_FAIL
			if (received.equals("FAILED")) {
				System.out.println("Authentication unsuccessful.");
			}
			else {
				System.out.println("Authentication successful!");
				client.setCK_A(AES.decrypt(received, String.valueOf(client.getKey())));
				
				// Receive rand_cookie for use in CONNECT
				client.setBuf(new byte[65535]);
				client.setPacket(new DatagramPacket(client.getBuf(), client.getBuf().length));
				client.getSocket().receive(client.getPacket());
				received = Client.data(client.getBuf()).toString();
				
				// Set rand_cookie for client
				client.setCookie(Integer.valueOf(received));
				
				// Receive TCP port
				client.setBuf(new byte[65535]);
				client.setPacket(new DatagramPacket(client.getBuf(), client.getBuf().length));
				client.getSocket().receive(client.getPacket());
				received = Client.data(client.getBuf()).toString();
				
				// Set TCP port for client
				client.setTcpPort(Integer.valueOf(received));
				
				// Establish TCP connection with Server
				client.setClientSocket(new Socket(InetAddress.getLocalHost(), client.getTcpPort()));
				client.setOut(new PrintWriter(client.getClientSocket().getOutputStream(), true));
				client.setIn(new BufferedReader(new InputStreamReader(client.getClientSocket().getInputStream())));
			}
		}
		
		/**************
		 * Chat Phase *
		 *************/
		
		// Always running to catch responses
		running = true;
		while (running) {
			
		}
		
	}

	
	// AES test function
	private static void testAES() {
		// Testing AES class
		String secretKey = "key";
		
		String originalString = "Please Work!";
		String encryptedString = AES.encrypt(originalString, secretKey);
		String decryptedString = AES.decrypt(encryptedString, secretKey);
		
		System.out.println(originalString);
		System.out.println(encryptedString);
		System.out.println(decryptedString);
	}
	
	// Function for allowing user to log on
	private static void loggingOn() {
		// User has to "log on"
		boolean loggingOn = true;
		String userInput;
		try (Scanner scanner = new Scanner(System.in)){
			while (loggingOn) {
				userInput = scanner.nextLine();
				
				if (userInput.equals("Log on")) {
					loggingOn = false;
				}
			}
			scanner.close();
		}
	}
}
