package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;


/**
 * Represents the client
 * 
 * @author fbb3628
 */
public class Client {
    private static final String HOST_NAME = "192.168.1.78";
    private static final int HOST_PORT = 7777;
    private PrintWriter pw; // input stream to server
    private BufferedReader br; // output stream from server
    private Socket socket;
    private GUI gui;
    private boolean isOpen;

    public Client(GUI gui) {
        this.gui = gui;
    }

    public void send() {
        if (!gui.textField.getText().equals("")) {
            pw.println(gui.textField.getText());
        }
    }

    public void startClient() {
        try {
            // Try connect to server
            socket = new Socket(HOST_NAME, HOST_PORT);
            JOptionPane.showMessageDialog(gui, "Successfully connected", "Connected", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            // Show dialog if failed
            JOptionPane.showMessageDialog(gui, "Client cannot connect to server", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

        try {
            // Create an autoflush output stream for the socket
            pw = new PrintWriter(socket.getOutputStream(), true);
            // Create a buffered input stream for this socket
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Set open client to true
            isOpen = true;
            InnerReceive receive = new InnerReceive();
            Thread thread = new Thread(receive);
            thread.start();
            
        } catch (IOException e) {
            // Handle exceptions
            JOptionPane.showMessageDialog(gui, "Client error with chat: " + e, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    /**
     * Disconnects the client from the server
     */
    public void stopClient() {
        pw.close();
        isOpen = false;
        try {
            br.close();
            socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            JOptionPane.showMessageDialog(gui, "Client error with chat: " + e, "Error", JOptionPane.ERROR_MESSAGE);
        }
        
    }

    private class InnerReceive implements Runnable {

        @Override
        public void run() {
            String serverResponse;
            try {
                do {
                    serverResponse = br.readLine();
                    gui.textArea.setText(gui.textArea.getText() + "\n" + serverResponse);
                } while (isOpen);
                
            } catch (IOException e) {
                // TODO Auto-generated catch block
                JOptionPane.showMessageDialog(gui, "Client error with chat: " + e, "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        }
    }

    private class InnerSend implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub

        }
    
        
    }
}