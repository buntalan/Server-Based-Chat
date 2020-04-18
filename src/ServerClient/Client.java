package ServerClient;

import java.net.*;
import java.io.*;
import java.util.*;

// Server and client will work with port 5180 for UDP and 5380 for TCP

public class Client{
	private DatagramSocket socket;
	private static int udpPort = 5180;
	private InetAddress address;
	private String client_ID; // Client_ID
	private int key; // Secret Key K
	// client_ID and k will be stored in the server
	private byte[] buf; // Buffer for sending/receiving datagrams
	
	
	// Getters and Setters
	public InetAddress getAddress() {
		return address;
	}

	public String getClient_ID() {
		return client_ID;
	}
	
	public int getKey() {
		return key;
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
	
	// Client Constructor
	// FIXME: WIP, not sure if anything should be added right now. 
	public Client() throws Exception {
		randClientID();
		socket = new DatagramSocket();
		// Need to change address for anything thats not local transfer
		address = InetAddress.getLocalHost();
	}
	
	
	/*CONNECTION FUNCTIONS*/
	public void HELLO(String client_ID) throws Exception {
		// TODO: Say HELLO! Request for authentication. Will be UDP.
		// Buffer bytes of client_ID
		buf = client_ID.getBytes();
		
		// create packet with client_ID
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, udpPort);
		
		// Send hello to server
		socket.send(packet);
	}
	
	public void RESPONSE(String client_ID, int Res) {
		// TODO: Response to challenge from server, authenticates self
	}
	
	public void CONNECT(int rand_cookie) {
		// TODO: Sends rand_cookie to server. Way to choose between chats maybe? 
	}
	
	/*CHAT FUNCTIONS*/
	
	
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
}
