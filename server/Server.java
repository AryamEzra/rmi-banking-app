package server;

import common.BankService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    public static void main(String[] args) {
        try {
            BankService service = new BankServiceImpl();

            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("BankService", service);

            System.out.println("Server running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}