package src;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import java.awt.event.*;
import java.awt.GridLayout;

/**
 * A custom dialog for inputting name
 */
public class SetNameDialog extends JDialog implements ActionListener {

    private GUI gui;
    private boolean nameSet;
    private JLabel label;
    private JTextField nameField;
    private CustomButton button;

    public SetNameDialog(GUI gui) {
        super(gui, "Enter name (required)", true);
        this.nameSet = false;
        this.gui = gui;
        
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        label = new JLabel("Name: ");
        nameField = new JTextField();
        button = new CustomButton("Ok");
        this.setLayout(new GridLayout(4, 2));
        this.add(label);
        this.add(nameField);
        this.add(button);
        button.addActionListener(button);
        setSize(400, 400);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub

    }

    private class CustomButton extends JButton implements ActionListener {

        public CustomButton(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!nameField.getText().equals("")) {
                gui.client.setName(nameField.getText());
                gui.lblTitle.setText("Name: " + nameField.getText());
                dispose();
            } else {
                JOptionPane.showMessageDialog(gui, "Please enter a name", "Enter name", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}