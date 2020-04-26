package ServerClient;

import java.net.*;
import java.util.*;
import java.io.*;

public class ClientProgram {
	public static void main (String[] args) throws Exception {
		Scanner in = new Scanner(System.in);
		String received = null;
		String response = null;
		
		// Create client instance
		Client client = new Client();
		client.setSocket(new DatagramSocket());
		// Set address to local host when testing
		client.setAddress(InetAddress.getLocalHost());
				
		// FIXME: Setting custom Client_ID and key for testing
		// Normally A and 4 for testing
		System.out.println("Enter clientID, then Key");
		client.setClient_ID(in.nextLine());
		client.setKey(Integer.valueOf(in.nextLine()));
		
		// Set up ClientProgram for receiving data UDP Datagrams
		// FIXME: Uncomment when done debugging.
		// client.getSocket().setSoTimeout(1000);
		client.setBuf(new byte[65535]);
		client.setPacket(new DatagramPacket(client.getBuf(),
				client.getBuf().length));
		
		// History
		ArrayList<History> history = new ArrayList<>();
		
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

				// Print that TCP connection is successful
				System.out.println("Connection successful.");
				client.getOut().println(AES.encrypt("Working on this end!", client.getCK_A()));
				
				// Send out connect request to server
				// FIXME: Do not need this here. TCP connection established.
				// Control given to user
				// client.CONNECT(client.getCookie());
			}
		}
		
		/**************
		 * Chat Phase *
		 *************/
		
		
		// Prompt user on how to log on
		System.out.println("Type \"Log on\" to connect.");
		System.out.println("Once logged on, you may log off by typing \"Log off\".");
		System.out.println("To initiate chat, type: Chat Client-ID");
		System.out.println("If you are in a chat, simply just type a message.");
		
		// Create separate threads to handle incoming/outgoing messages
		Thread sendMessage = new Thread (new Runnable() {
			@Override
			public void run() {
				while (true) {
					// Prompt user on what to do next
					String response = in.nextLine();
					
					// CONNECT
					if (response.equals("Log on")) {
						client.CONNECT(client.getCookie());
					}
					// Log off
					else if (response.equals("Log off")) {
						client.getOut().println(AES.encrypt(response, client.getCK_A()));
						try {
							client.getIn().close();
							client.getOut().close();
							client.getSocket().close();
							client.getClientSocket().close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						client.setSessionID(null);
					}
					// CHAT_REQUEST
					else if (response.matches("[Cc]hat [A-Za-z]+")) {
						String[] tokens;
						tokens = response.split("\\W+");
						client.CHAT_REQUEST(tokens[1]);
					}
					// END_REQUEST
					else if (response.matches("[Ee]nd [Cc]hat")) {
						client.END_REQUEST(client.getSessionID());
					}
					// HISTORY_REQ
					else if (response.matches("[Hh]istory [A-Za-z]+")) {
						String[] tokens;
						tokens = response.split("\\W+");
						client.HISTORY_REQ(tokens[1]);
					}
					// CHAT
					// FIXME: This needs fixing.
					// response != null && client.inChat
					else if (response != null){
						client.CHAT(client.getSessionID(), response);
					}
				}
			}
		});
		
		Thread readMessage = new Thread(new Runnable() {
			@Override
			public void run() {
				// Receive response from server
				while (true) {
					try {
						String response = client.getIn().readLine();
						response = AES.decrypt(response, client.getCK_A());
						System.out.println(response);
						
						if (response.matches("You are in session [0-9]+\\.")) {
							String[] tokens = response.split("\\W+");
							client.setSessionID(tokens[4]);
						}
						
					} catch (IOException e) {
						// FIXME: If this exception is caught, this implies that the chat is closed and/or
						// No connection found. 
						// e.printStackTrace();
						System.out.println("Connection has ended");
						break;
					} catch (NullPointerException e) {
					}
				}
			}
		});
		
		// Start send/read threads
		sendMessage.start();
		readMessage.start();
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
