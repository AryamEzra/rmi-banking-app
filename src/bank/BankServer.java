package bank;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class BankServer {
    public static void main(String[] args) {
        try {
            BankServiceImpl service = new BankServiceImpl();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("BankService", service);
            System.out.println("=================================================");
            System.out.println("  Simple Banking RMI Server running on port 1099");
            System.out.println("=================================================");
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
