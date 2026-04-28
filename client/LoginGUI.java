package client;

import javax.swing.*;

public class LoginGUI {

    public LoginGUI() {
        JFrame frame = new JFrame("Login");

        JTextField phone = new JTextField();
        JPasswordField pass = new JPasswordField();

        JButton loginBtn = new JButton("Login");

        frame.setLayout(new java.awt.GridLayout(3,2));
        frame.add(new JLabel("Phone:"));
        frame.add(phone);
        frame.add(new JLabel("Password:"));
        frame.add(pass);
        frame.add(loginBtn);

        loginBtn.addActionListener(e -> {
            try {
                if (Client.service == null) {
                    JOptionPane.showMessageDialog(frame, "Not connected to server");
                    return;
                }
                boolean ok = Client.service.login(phone.getText(), new String(pass.getPassword()));
                if (ok) {
                    frame.dispose();
                    new BankGUI(phone.getText());
                } else {
                    JOptionPane.showMessageDialog(frame, "Login failed");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        frame.setSize(300,200);
        frame.setVisible(true);
    }
}