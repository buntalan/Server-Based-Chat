package ServerClient;

import java.net.InetAddress;

public class ClientProgram {
	public static void main (String[] args) throws Exception {
		
		boolean running;
		
		// Create client instance
		Client client = new Client();
		client.setAddress(InetAddress.getLocalHost());
		
		// Establish Connection
		client.HELLO(client.getClient_ID());
		
		
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
