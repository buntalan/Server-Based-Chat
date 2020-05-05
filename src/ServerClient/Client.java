package ServerClient;

import java.net.*;
import java.io.*;
import java.util.*;

// Server and client will work with port 5180 for UDP.
// Port will be assigned by server for TCP.

public class Client extends Thread {
	private DatagramSocket socket;			// UDP Socket
	private DatagramPacket packet;			// UDP Packet
	private static int udpPort = 5180;		// Port of Server
	private InetAddress address;			// IP Address of Server
	
	private Socket clientSocket;			// TCP Client Socket
	private int tcpPort;					// TCP Port
	private PrintWriter out;				// Output PrintWriter
	private BufferedReader in;				// Read in from Buffered
											// from server
	
	// Flags to indicate if online and if in chat
	boolean online = false;
	boolean inChat = false;
	
	
	// client_ID and k will be stored in the server
	private String client_ID; 				// Client_ID
	private int key; 						// Secret Key K
	private String CK_A;					// CK_A to decrypt/encrypt messages
	private int cookie;						// Cookie sent from server
	
	// SessionID
	private String sessionID = null;		// sessionID of chat client is in
	private static int numChats = 0;		// Number of active chats and also the assigned
											// sessionID of new chats
	

	// Buffer
	private byte[] buf; 					// Buffer for sending/receiving datagrams
	
	

	// Getters and Setters
	// UDP
	public DatagramSocket getSocket() {
		return socket;
	}
	
	public DatagramPacket getPacket() {
		return packet;
	}

	public InetAddress getAddress() {
		return address;
	}
	
	public static int getUdpPort() {
		return udpPort;
	}
	
	public String getClient_ID() {
		return client_ID;
	}
	
	public int getKey() {
		return key;
	}
	
	public String getCK_A() {
		return CK_A;
	}

	public int getCookie() {
		return cookie;
	}
	
	public byte[] getBuf() {
		return buf;
	}

	
	public void setSocket(DatagramSocket socket) {
		this.socket = socket;
	}

	public void setPacket(DatagramPacket packet) {
		this.packet = packet;
	}
	
	public static void setUdpPort(int udpPort) {
		Client.udpPort = udpPort;
	}
	
	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public void setClient_ID(String client_ID) {
		this.client_ID = client_ID;
	}
	
	public void setKey(int key) {
		this.key = key;
	}
	
	public void setCK_A(String cK_A) {
		CK_A = cK_A;
	}
	
	public void setCookie(int cookie) {
		this.cookie = cookie;
	}
	
	public void setBuf(byte[] buf) {
		this.buf = buf;
	}
	
	// TCP
	public Socket getClientSocket() {
		return clientSocket;
	}

	public int getTcpPort() {
		return tcpPort;
	}

	public PrintWriter getOut() {
		return out;
	}

	public BufferedReader getIn() {
		return in;
	}

	public String getSessionID() {
		return sessionID;
	}
	
	public void setClientSocket(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public void setTcpPort(int tcpPort) {
		this.tcpPort = tcpPort;
	}

	public void setOut(PrintWriter out) {
		this.out = out;
	}

	public void setIn(BufferedReader in) {
		this.in = in;
	}
	
	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}
	
	// Client Constructor
	public Client() throws Exception {
	}
	
	// This is the part that talks to the client from the server! 
	@Override
	public void run() {
		String received;
		while (true) {
			try {
				// Received takes in a line from BufferedReader
				received = in.readLine();
					
				// Decrypt message
				received = AES.decrypt(received, this.CK_A);
				
				// Log off
				if (received.equals("Log off")) {
					this.online = false;
					this.inChat = false;
					getIn().close();
					getOut().close();
					getClientSocket().close();
					this.setSessionID(null);
					break;
				}
				// Send CONNECTED
				else if (received.equals(String.valueOf(this.cookie))){
					// Send user CONNECTED! and make online. 
					Server.CONNECTED(this.out, this.CK_A);
					this.online = true;
				}
				// Received END_NOTIF
				else if (received.matches("!!!! \\d+")) {
					String[] tokens = received.split("\\W+");
					
					Server.END_NOTIF(tokens[1]);
				}
				// Received CHAT_REQUEST, search for and start chat with Client B
				else if (received.matches("Chat [A-Za-z]+")) {
					// CHAT_STARTED and UNREACHABLE
					String[] tokens = received.split("\\W+");
					
					if (isFoundAndAvail(tokens[1])) {
						// Set inChat to true. The clientHandler is now set to online.
						this.inChat = true;
						
						// CHAT_STARTED
						Server.CHAT_STARTED(String.valueOf(numChats++), this.client_ID, tokens[1], this.CK_A);
					}
					else {
						// Send out UNREACHABLE if Client could not be found
						Server.UNREACHABLE(tokens[1], this.out, this.CK_A);
					}
				}
				// Received CHAT
				// Message broken into SessionID@!clientID@!message
				// Broken up with @!
				else if (received.matches("[0-9]+@!\\w+@!.*")){
					// Split message into tokens
					String[] tokens = received.split("@!+");
					
					// Search through listClients for other participating user
					for (int i = 0; i < Server.listClient.size(); i++) {
						// Find clientID of other user.
						
						// If client has matchingID, is NOT the client who just sent the message, is online, and is inChat, send message.
						if (Server.listClient.get(i).getSessionID() != null
								&& Server.listClient.get(i).getSessionID().equals(tokens[0]) 
								&& (Server.listClient.get(i).getClient_ID().equals(this.client_ID) == false)
								&& (Server.listClient.get(i).online == true) && (Server.listClient.get(i).inChat == true)) {
							// Encrypt and send message to other user
							Server.listClient.get(i).getOut().println(AES.encrypt(tokens[1] +
									": " + tokens[2], Server.listClient.get(i).getCK_A()));
							
							// Save to message history
							History.save(this.client_ID, Server.listClient.get(i).getClient_ID(), tokens[2]);
							break;
						}
					}
				}
				// HISTORY_RESP
				else if (received.matches("History [A-Za-z]+")) {
					String[] tokens = received.split("\\s+");
					Server.HISTORY_RESP(this.client_ID, tokens[1], this.out, this.CK_A);
				}
				
			} catch (IOException e) {
				// FIXME: Commenting out. Should imply that the connection is closed.
				// e.printStackTrace();
				break;
			}
			
		}
	}
	
	/*CONNECTION FUNCTIONS*/
	public void HELLO(String client_ID) throws Exception {
		// Say HELLO! Request for authentication. Will be UDP.
		// Buffer bytes of client_ID
		buf = client_ID.getBytes();
		
		// Create packet with client_ID
		packet = new DatagramPacket(buf, buf.length, address, udpPort);
		
		// Send hello to server
		socket.send(packet);
	}

	public void RESPONSE(String client_ID, String Res, InetAddress address, int port) throws Exception {
		// Response to challenge from server, authenticates self
		buf = Res.getBytes();
		packet = new DatagramPacket(buf, buf.length, address, port);
		
		// Send response to server
		socket.send(packet);
	}

	public void CONNECT(int rand_cookie) {
		// Sends rand_cookie to server. Way to authenticate with server when connecting
		out.println(AES.encrypt(String.valueOf(rand_cookie), this.CK_A));
	}
	
	/*CHAT FUNCTIONS*/
	public void CHAT_REQUEST (String clientID) {
		// Sent by client to the server to request a chat session with clientID
		// Caught bug "Chat" is supposed to be "Chat "
		// Double check sent Strings
		String temp = "Chat " + clientID;
		out.println(AES.encrypt(temp, this.CK_A));
	}
	
	public void END_REQUEST (String sessionID) {
		// Sent by client A to server to request a chat session with Client B
		// Turn off false flag.
		this.inChat = false;
		out.println(AES.encrypt("!!!! " + sessionID, this.CK_A));
	}
	
	public void CHAT (String sessionID, String message) {
		// Send message to client, carried by server
		if (sessionID != null) {
			// Create message template
			String temp = sessionID + "@!" + getClient_ID() + "@!" + message;
			
			// Send message to server
			out.println(AES.encrypt(temp, this.CK_A));
		}
	}
	
	public void HISTORY_REQ (String clientID) {
		// Request history of chat with clientID from server
		out.println(AES.encrypt("History " + clientID, this.CK_A));
	}
	
	/*Everything else*/
	
	// Random ID between A-Z. 
	private void randClientID() {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random rnd = new Random();
		
		// This returns String of randomly chosen character index of chars
		// Slight chance of UserID replicant. 
		setClient_ID(String.valueOf(chars.charAt(rnd.nextInt(chars.length()))));
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
	
	// For checking if the other client may be found in the listClient ArrayList
	public static boolean isFoundAndAvail (String clientID) {
		// Search through and return true if clientID is found in listClient
		for (int i = 0; i < Server.listClient.size(); i++) {
			if (clientID.equals(Server.listClient.get(i).getClient_ID()) && (Server.listClient.get(i).online == true)
					&& (Server.listClient.get(i).inChat == false)) {
				// Return true when found && online && not inChat
				return true;
			}
		}
		
		// Return false if not found
		return false;
	}
	
}
