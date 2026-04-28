package client;

import common.BankService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.swing.SwingUtilities;

public class Client {
    public static BankService service;

    public static void connect() throws Exception {
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        service = (BankService) registry.lookup("BankService");
    }

    public static void main(String[] args) {
        try {
            connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new LoginGUI());
    }
}