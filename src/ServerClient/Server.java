package ServerClient;

import java.net.*;
import java.nio.ByteBuffer;
import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.security.*;


// Server will run on port 5180 for UDP and 5380 for TCP

public class Server {
	// For keeping list of clients
	// and their respective SubID, SubKey, rand_Cookie from txt file
	// Adding list of active users
	// FIXME: Might not need to be static.
	static ArrayList<Client> listClient;
	static int indexOfSearch;
	
	// Variables required for UDP connection
	static DatagramSocket socketUDP; 	// UDP Socket for "Welcoming Server"
	static int udpPort = 5180;
	InetAddress address;
	
	// Variables required for TCP connection
	static ServerSocket socketTCP;		// One TCP Socket for incoming connections
	// FIXME: Might not need clientSocket on Server. Have ArrayList<Client>
	// that we may make Sockets on
	static Socket clientSocket;			// One client socket for communication between server-client
	static int tcpPort = 5380;			// Port of server to be passed to client for TCP connections
	static PrintWriter out;				// For sending to clients
	static BufferedReader in;			// For receiving from clients
	private String received = null;		// String for receiving
	
	
	// Server constructor
	public Server() throws Exception {
	}
	
	/*************************
	 * Main/Driver Function  *
	 ************************/
	public static void main (String[] arg) throws Exception {
		
		String clientRequest = null; 				// Client Request
		String response = null;						// Server response
		boolean running;							// Boolean for looping connection
		indexOfSearch = -1;							// Index of Search
		
		// Open UDP socket for connection
		socketUDP = new DatagramSocket(udpPort);	// Open UDP Socket on port 5180.
		// FIXME: Commented out for debugging. Take out comments when done.
		// socketUDP.setSoTimeout(1000);			// Timeout for 1000ms
		
		// Open TCP socket for connection
		socketTCP = new ServerSocket(tcpPort);		// Open TCP socket on tcpPort
		// FIXME: Commented out for debugging.
		// serverSocket.setSoTimeout(1000);			// Timeout for 1000ms on blocking
													// operations.
		
		
		// Variables for receiving datagrams from UDP
		byte[] buf = new byte[65535]; 									// Size of byte buffer
		DatagramPacket received = new DatagramPacket(buf, buf.length);	// For receiving packets
		DatagramPacket outgoing;										// For outgoing packets
		
		// Populate subscriber list
		listClient = fillSubscriberList();
		
		
		// Will always be running
		running = true;
		
		// ... forever.
		while (running) {
			try {
				// Receive packet
				socketUDP.receive(received);
				
				// Extract data
				clientRequest = data(buf).toString();
				
				// FIXME: For testing
				System.out.println(clientRequest);
				
				// Search subscriber list for matching Client-ID
				// if clientMessage is not null
				// This implies a received UDP Datagram.
				if (!clientRequest.isEmpty()) {
					for (int i = 0; i < listClient.size(); i++) {
						// If client is found, stop and take index
						if (clientRequest.equals(listClient.get(i).getClient_ID())) {
							indexOfSearch = i;
							break;
						}
					}
					
					// Run CHALLENGE
					if (indexOfSearch != -1) {
						int rand = randNum();
						
						// AUTH_SUCCESS or AUTH_FAIL depending on if subscriber passes authentication
						if (CHALLENGE(rand, indexOfSearch, received.getAddress(), received.getPort())) {
							// Run AUTH_SUCCESS here
							AUTH_SUCCESS(randNum(0, 1000), tcpPort, received.getAddress(), received.getPort(), listClient.get(indexOfSearch).getKey(), indexOfSearch);
						}
						else {
							// Run AUTH_FAIL here
							AUTH_FAIL(received.getAddress(), received.getPort());
						}
					}
					else {
						// Send "Subscriber not found" Message
						// FIXME: Buffer refresh may be redundant. Might delete later.
						buf = new byte[65535]; 
						response = "FFFF";
						buf = response.getBytes();
						
						// Make Datagram and send out to client
						outgoing = new DatagramPacket(buf, buf.length, received.getAddress(), received.getPort());
						
						socketUDP.send(outgoing);
					}
					
					
					// Reset clientMessage and indexOfSearch for more connection attempts
					clientRequest = null;
					indexOfSearch = -1;
					
					// Must clear buffer and create new receiving Datagram
					buf = new byte[65535];
					received = new DatagramPacket(buf, buf.length);
				}
			}
			catch (SocketTimeoutException e) {
                // Timeout Reached
                // System.out.println("Timeout reached or no incoming connections." + e);
            }
		}
		
		// Close socket if loop ends
		socketUDP.close();
		socketTCP.close();
	}

	
	/*CONNECTION FUNCTIONS*/
	public static boolean CHALLENGE(int rand, int index, InetAddress address, int port) throws Exception {
		// Sent by server to challenge client to authenticate self. New rand generated every challenge.
		String XRES = null;
		String RES = null;
		DatagramPacket packet;

		// Create hash code for CK_A
		XRES = new String(AES.encrypt(String.valueOf(rand) + String.valueOf(listClient.get(index).getKey()), 
				String.valueOf(listClient.get(index).getKey())));
		// FIXME: Testing response
		System.out.println(AES.decrypt(XRES, String.valueOf(listClient.get(index).getKey())));
		// Convert rand to bytes for CHALLENGE
		String temp = String.valueOf(rand);
		byte[] array = temp.getBytes();
		
		// Send out packet for CHALLENGE
		packet = new DatagramPacket(array, array.length, address, port);
		socketUDP.send(packet);
		
		// Receive packet from RESPONSE
		array = new byte[65535];
		packet = new DatagramPacket(array, array.length, address, port);
		socketUDP.receive(packet);
		RES = Client.data(array).toString();
		
		// Authenticate...
		if (RES.equals(XRES)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static void AUTH_SUCCESS(int rand_cookie, int port_number, InetAddress address, int portUDP, int secretKey, int index) throws Exception {
		// Sent by server to notify the client authentication was successful. 
		// rand_cookie is rand generated by server, port_number is TCP port assigned by server for subsequent connection by client.
		// Create CK_A for Server and Client to use for transfers.
		// CK_A = hash(rand_cookie + secretKey) (String Values)
		String clientCK_A = String.valueOf(rand_cookie) + String.valueOf(secretKey);
		
		// Update subscriber's CK_A, rand_cookie, and port_number
		listClient.get(indexOfSearch).setCK_A(clientCK_A);
		listClient.get(indexOfSearch).setCookie(rand_cookie);
		listClient.get(indexOfSearch).setTcpPort(port_number);
		
		// FIXME: May need to encrypt the other packets being sent here as well.
		// Send over CK_A, encrypting it with subscriber's secretKey
		// Future transfers will use CK_A
		byte[] buffer = AES.encrypt(clientCK_A, String.valueOf(secretKey)).getBytes();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, portUDP);
		socketUDP.send(packet);
		
		// Send over rand_cookie for CONNECT
		buffer = String.valueOf(rand_cookie).getBytes();
		packet = new DatagramPacket(buffer, buffer.length, address, portUDP);
		socketUDP.send(packet);
		
		// Send over TCP port for client use
		buffer = String.valueOf(port_number).getBytes();
		packet = new DatagramPacket(buffer, buffer.length, address, portUDP);
		socketUDP.send(packet);
		
		// Accept TCP connection request as well as
		// open input/output stream to that client.
		// Using client handler for multiple threads. This section may not be
		// necessary. 
		listClient.get(index).setClientSocket(socketTCP.accept());
		listClient.get(index).setOut(new PrintWriter(listClient.get(index).getClientSocket().getOutputStream(), true));
		listClient.get(index).setIn(new BufferedReader(new InputStreamReader(listClient.get(index).getClientSocket().getInputStream())));
		
		// Start new thread for this connection
		Thread thread = new Thread(listClient.get(index));
		
		// Add client to active clients, and start thread
		listClient.get(index).online = true;
		thread.start();
	}
	
	public static void AUTH_FAIL(InetAddress address, int port_number) throws Exception {
		// Sent by server to notify the client authentication has failed.
		byte[] buffer = "FAILED".getBytes();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port_number);
		socketUDP.send(packet);
	}
	
	public static void CONNECTED(PrintWriter out, String CK_A) {
		// Sent by the server to notify the client it has been connected.
		out.println(AES.encrypt("Connected!", CK_A));
	}
	
	/*CHAT FUNCTIONS*/
	public static void CHAT_STARTED(String sessionID, String ClientIDA, String ClientIDB, String CK_AA) throws IOException {
		// Sent by server to notify client A has started a chat session with client B.
		// SessionID is the ID for the session
		// Search for ClientA and notify user that they are connected in a chat.
		for (int i = 0; i < listClient.size(); i++) {
			if (listClient.get(i).getClient_ID().equals(ClientIDA)) {
				listClient.get(i).setSessionID(sessionID);
				listClient.get(i).getOut().println(AES.encrypt("You are now chatting with " + ClientIDB + "!", CK_AA));
				listClient.get(i).getOut().println(AES.encrypt("You are in session " + sessionID + ".", CK_AA));
				listClient.get(i).inChat = true;
				break;
			}
		}
		
		// Search for ClientB and notify user that they are connected in a chat
		for (int i = 0; i < listClient.size(); i++) {
			if (listClient.get(i).getClient_ID().equals(ClientIDB)) {
				listClient.get(i).setSessionID(sessionID);
				listClient.get(i).getOut().println(AES.encrypt("You are now chatting with " + ClientIDA + "!", listClient.get(i).getCK_A()));
				listClient.get(i).getOut().println(AES.encrypt("You are in session " + sessionID + ".", listClient.get(i).getCK_A()));
				listClient.get(i).inChat = true;
				break;
				// listClient.get(i).getIn().read(AES.encrypt("You are now chatting with " + ClientIDA + "!", CK_AA).toCharArray());
			}
		}
	}
	
	public static void UNREACHABLE(String clientID, PrintWriter out, String CK_A) {
		// Sent by the server to client A to notify client B is not available
		// for a chat
		out.println(AES.encrypt(clientID + " is not available.", CK_A));
	}
	
	public static void END_NOTIF(String sessionID) {
		// Sent by server to notify a client involved in the session that
		// the session has been terminated by a client
		for (int i = 0; i < listClient.size(); i++) {
			if (listClient.get(i).getSessionID() != null &&
					listClient.get(i).getSessionID().equals(sessionID) && listClient.get(i).inChat == true) {
				listClient.get(i).getOut().println(AES.encrypt("Chat ended", listClient.get(i).getCK_A()));
				listClient.get(i).inChat = false;
			}
		}
	}
	
	public static void HISTORY_RESP(String clientIDA, String clientIDB, PrintWriter out, String CK_A) throws IOException {
		// Sent by server to client who requested the history. Sending clientID of
		// the ID of the client who sent the chat message, and chat message is the chat
		// message in history. 
		// One HISTORY_RESP for every message in the chat history. 
		History.load(clientIDA, clientIDB, out, CK_A);
	}
	
	/*Everything Else*/
	
	// Test subscriber fill
	private static void subscriberFillTest() {
		// Test list of subscribers
		for (int i = 0; i < listClient.size(); i++) {
			System.out.println(listClient.get(i).getClient_ID() + " " + listClient.get(i).getKey());
		}
	}
	
	// Utility function for filling subscriber list
	public static ArrayList<Client> fillSubscriberList() throws IOException, Exception {
		ArrayList<Client> clientList;
		clientList = new ArrayList<Client>();
		String temp;
		String[] tokens;
		
		FileInputStream in = null;
		BufferedReader reader = null;
		
		// Open input stream and fill subscriber list
		try {
			// Place "subscribers.txt" in project folder.
			in = new FileInputStream("subscribers.txt"); // Read in from file subscribers.txt
			reader = new BufferedReader(new InputStreamReader(in));
			
			temp = reader.readLine();
			while (temp != null) {
				// Instantiate new client to add to ArrayList of Clients
				Client client = new Client();
				
				// Add information about client.
				tokens = temp.split("\\W+");
				client.setClient_ID(tokens[0]);
				client.setKey(Integer.parseInt(tokens[1]));
				
				// Add client to list
				clientList.add(client);
				
				// Go on to next line
				temp = reader.readLine();
			}
		}
		finally {
			if (in != null) {
				in.close();
				reader.close();
			}
		}
		
		return clientList;
	}
	
	// Utility method for converting byte 
	// array data into String representation.
	public static StringBuilder data(byte[] buf) {
		if (buf == null) {
			return null;
		}
		StringBuilder array = new StringBuilder();
		int i = 0;
		while (i < buf.length && buf[i] != 0) {
			array.append((char) buf[i]);
			i++;
		}
		return array;
	}
	
	// Random number utility function for challenges
	public static int randNum() {
		int rand = (int)(Math.random()*((100)+1));  // Num generated between [0, 100]
		return rand;
	}
	
	// Random port number utility function for AUTH_SUCCESS
	public static int randNum(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}
}
