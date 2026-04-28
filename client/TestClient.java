package client;

import common.BankService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestClient {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            BankService svc = (BankService) registry.lookup("BankService");

            String phone = "5550000";
            String acc = svc.register("Test User", phone, "pass123", 100.0);
            System.out.println("Registered account: " + acc);

            boolean ok = svc.login(phone, "pass123");
            System.out.println("Login ok: " + ok);

            double bal = svc.getBalance(phone);
            System.out.println("Balance: " + bal);

            double after = svc.deposit(phone, 50.0);
            System.out.println("After deposit: " + after);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
