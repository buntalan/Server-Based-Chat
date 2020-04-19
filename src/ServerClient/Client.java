package ServerClient;

import java.net.*;
import java.io.*;
import java.util.*;
import java.security.*;

// Server and client will work with port 5180 for UDP and 5380 for TCP

public class Client{
	private DatagramSocket socket;			// UDP Socket
	private DatagramPacket packet;			// UDP Packet
	private static int udpPort = 5180;		// Port of Server
	private InetAddress address;			// IP Address of Server
	


	// client_ID and k will be stored in the server
	private String client_ID; 				// Client_ID
	private int key; 						// Secret Key K
	private String CK_A;					// CK_A to decrypt/encrypt messages


	// Buffer
	private byte[] buf; 					// Buffer for sending/receiving datagrams
	
	
	// Getters and Setters
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
	
	public void setBuf(byte[] buf) {
		this.buf = buf;
	}

	
	// Client Constructor
	public Client() throws Exception {
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
	
	/*CHAT FUNCTIONS*/
	public void CONNECT(int rand_cookie) {
		// TODO: Sends rand_cookie to server. Way to authenticate with server when connecting
	}
	
	
	
	/*Everything else*/
	
	// Random ID between A-Z. 
	private void randClientID() {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random rnd = new Random();
		
		// This returns String of randomly chosen character index of chars
		// TODO: There might be a better way of doing this. 
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
}
