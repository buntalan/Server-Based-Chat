package ServerClient;

import java.net.*;
import java.util.*;

public class ClientProgram {
	public static void main (String[] args) throws Exception {
		String received = null;
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
		
		
		/********************
		 * Connection Phase *
		 *******************/
		
		// Establish Connection
		client.HELLO(client.getClient_ID());
		
		// Receive Response
		client.getSocket().receive(client.getPacket());
		received = Client.data(client.getBuf()).toString();
		
		// FIXME: For testing
		System.out.println(received);
		
		if (received.contentEquals("FFFF")) {
			System.out.println("Client is not a subscriber.");
		}
		else {
			client.setBuf(new byte[65535]);
			
			// Receive response for HELLO
			client.getSocket().receive(client.getPacket());
			received = Client.data(client.getBuf()).toString();
			
			
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
}
