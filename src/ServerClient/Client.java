package ServerClient;
import java.net.*;
import java.io.*;

// Server and client will work with port 5180 for UDP and 5380 for TCP

public class Client {
	private DatagramSocket socket;
	// Might not need this
	private static int udpPort = 5180;
	private InetAddress address;
	private String client_ID; // Client_ID
	private int k; // Secret Key K
	// Both are stored in the server
	private boolean running;
	
	private byte[] buf;
	
	public InetAddress getAddress() {
		return address;
	}

	public String getClient_ID() {
		return client_ID;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public void setClient_ID(String client_ID) {
		this.client_ID = client_ID;
	}
	
	public Client() {
		try {
			socket = new DatagramSocket();
			
			// TODO: Need to change local host to IP of server when during real testing.
			address = InetAddress.getByName("DESKTOP-3MJBUKN");
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// FIXME: Test function to see if we are able to connect
	public String sendEcho(String msg) {
        buf = msg.getBytes();
        DatagramPacket packet 
          = new DatagramPacket(buf, buf.length, address, 5180);
        
        // Send packet
        try {
			socket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        // Create new packet
        packet = new DatagramPacket(buf, buf.length);
        
        // Receive paket
        try {
			socket.receive(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        String received = new String(packet.getData(), 0, packet.getLength());
        return received;
	}
	
	// FIXME: Test function to see if we are able to connect
	public void close() {
		socket.close();
	}
	
	// Make an establish connection function
	// Probably put this in driver class
	// TODO: Make this function. Then again, we may put all establish connection stuff in driver. 
	private void establishConnection(String client_ID) {
		HELLO(getClient_ID());
		
		running = true;
		
		while (running) {
			try {
				// FIXME: Definitely stuff to fix here. Let's not use this for the test for now.
				// Create response packet
				DatagramPacket response = new DatagramPacket(buf, buf.length);
				
				socket.receive(response);
				
				Thread.sleep(1000);		
				
				// FIXME: For testing
				String received = new String(response.getData(), 0, response.getLength());
				System.out.println(received);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (SocketTimeoutException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/*IMPORTANT FUNCTIONS*/
	public void HELLO(String client_ID) {
		// TODO: Say HELLO! Request for authentication. Will be UDP.
		// Buffer bytes of client_ID
		buf = client_ID.getBytes();
		
		// create packet with client_ID
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, udpPort);
		
		// Send hello to server
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void RESPONSE(String client_ID, int Res) {
		// TODO: Response to challenge from server, authenticates self
	}
	
	public void CONNECT(int rand_cookie) {
		// TODO: Sends rand_cookie to server. Way to choose between chats maybe? 
	}
	
	/*Everything else*/
	
}
