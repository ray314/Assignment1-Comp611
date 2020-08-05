package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JOptionPane;

import java.io.InputStreamReader;


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
        // Input/Output streams
        PrintWriter pw;
        BufferedReader br;

        try {
            // Create output stream
            pw = new PrintWriter(socket.getOutputStream(), true);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            pw.println("You can start chatting");
            do {
                String clientResponse = br.readLine();
                pw.println(clientResponse);
            } while (false);
        } catch (IOException e) {
            System.err.println("Error: " + e);
        }
    }
}