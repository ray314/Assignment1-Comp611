package src;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JLabel;

public class GUI {

	private JFrame frame;
	private JTextField textField;
	private JTextArea textArea;
	private JPanel southPanel;
	private JPanel eastPanel;
	private JPanel northPanel;
	private JLabel lblTitle;
	private JList list;
	private JButton btnConnect;
	private JButton btnSend;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		northPanel = new JPanel();
		frame.getContentPane().add(northPanel, BorderLayout.NORTH);
		// Title
		lblTitle = new JLabel("ChatBox");
		northPanel.add(lblTitle);
		
		eastPanel = new JPanel();
		frame.getContentPane().add(eastPanel, BorderLayout.EAST);
		eastPanel.setLayout(new GridLayout(0, 1, 0, 0));
		// List of connected clients
		list = new JList();
		eastPanel.add(list);
		
		btnConnect = new JButton("Connect");
		eastPanel.add(btnConnect);
		
		southPanel = new JPanel();
		frame.getContentPane().add(southPanel, BorderLayout.SOUTH);
		southPanel.setLayout(new GridLayout(0, 2, 0, 0));
		
		textField = new JTextField();
		southPanel.add(textField);
		textField.setColumns(10);
		
		btnSend = new JButton("Send");
		southPanel.add(btnSend);
		
		textArea = new JTextArea();
		frame.getContentPane().add(textArea, BorderLayout.CENTER);
	}

}