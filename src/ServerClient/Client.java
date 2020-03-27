package ServerClient;

public class Client {
	String client_ID;
	
	/*IMPORTANT FUNCTIONS*/
	public void HELLO(String client_ID) {
		// TODO: Say HELLO! Request for authentication. Will be UDP
	}
	
	public void RESPONSE(String client_ID, int Res) {
		// TODO: Response to challenge from server, authenticates self
	}
	
	public void CONNECT(int rand_cookie) {
		// TODO: Sends rand_cookie to server. Way to choose between chats maybe? 
	}
	
	/*Everything else*/
	
}
