package ServerClient;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class udpTest {
	Client client;
	 
    @Before
    public void setup(){
        new Server().run();
        client = new Client();
    }
 
    @Test
    public void whenCanSendAndReceivePacket_thenCorrect() {
        String echo = client.sendEcho("hello server");
        assertEquals("hello server", echo);
        echo = client.sendEcho("server is working");
        assertFalse(echo.equals("hello server"));
    }
 
    @After
    public void tearDown() {
        client.sendEcho("end");
        client.close();
    }
}

