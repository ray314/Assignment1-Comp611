package src;

import java.net.Socket;

/**
 * Class for representing the chatbox in server
 * @author fbb3628
 */
public class ChatBox implements Runnable{

    private Socket socket; // socket for client/server communication
    
    protected ChatBox(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub

    }
}