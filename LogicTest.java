import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Standalone logic test — no RMI, no GUI, validates all core banking rules.
 * Run with: java LogicTest.java (Java 11+ single-file execution)
 */
public class LogicTest {

    // ── Inline model ─────────────────────────────────────────────────────────
    static class User {
        String accountNo, name, phone, passwordHash;
        double balance;
        User(String acc, String n, String ph, String h, double b) {
            accountNo=acc; name=n; phone=ph; passwordHash=h; balance=b;
        }
    }

    static final Map<String, User> accounts = new HashMap<>();
    static final AtomicInteger counter = new AtomicInteger(1000001);

    static String sha256(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] b = md.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }

    static String register(String name, String phone, String pass, double deposit) throws Exception {
        for (User u : accounts.values())
            if (u.phone.equals(phone)) throw new Exception("Phone already registered.");
        if (deposit < 0) throw new Exception("Negative deposit.");
        String acc = String.valueOf(counter.getAndIncrement());
        accounts.put(acc, new User(acc, name, phone, sha256(pass), deposit));
        return acc;
    }

    static User login(String acc, String pass) throws Exception {
        User u = accounts.get(acc);
        if (u == null || !u.passwordHash.equals(sha256(pass)))
            throw new Exception("Invalid credentials.");
        return u;
    }

    static double deposit(String acc, double amt) throws Exception {
        if (amt <= 0) throw new Exception("Amount must be positive.");
        User u = accounts.get(acc); if (u == null) throw new Exception("Not found.");
        u.balance += amt; return u.balance;
    }

    static double withdraw(String acc, double amt) throws Exception {
        if (amt <= 0) throw new Exception("Amount must be positive.");
        User u = accounts.get(acc); if (u == null) throw new Exception("Not found.");
        if (u.balance < amt) throw new Exception("Insufficient funds.");
        u.balance -= amt; return u.balance;
    }

    static double transfer(String from, String to, double amt) throws Exception {
        if (amt <= 0) throw new Exception("Amount must be positive.");
        if (from.equals(to)) throw new Exception("Cannot transfer to self.");
        User f = accounts.get(from); User t = accounts.get(to);
        if (f == null) throw new Exception("Source not found.");
        if (t == null) throw new Exception("Destination account does not exist.");
        if (f.balance < amt) throw new Exception("Insufficient funds.");
        f.balance -= amt; t.balance += amt;
        return f.balance;
    }

    // ── Tests ─────────────────────────────────────────────────────────────────
    static int passed = 0, failed = 0;

    static void test(String name, Runnable r) {
        try { r.run(); System.out.println("  ✓  " + name); passed++; }
        catch (Throwable e) { System.out.println("  ✗  " + name + " — " + e.getMessage()); failed++; }
    }

    static void expect(boolean cond, String msg) {
        if (!cond) throw new RuntimeException(msg);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("\n═══════════════════════════════════════════════");
        System.out.println("   NexBank RMI — Core Logic Test Suite");
        System.out.println("═══════════════════════════════════════════════\n");

        // 1. Registration & sequential account numbers
        test("Registration gives sequential account numbers", () -> {
            try {
                String a1 = register("Alice Smith", "0600000001", "pass123", 500.0);
                String a2 = register("Bob Jones",   "0600000002", "pass456", 1000.0);
                expect(a1.equals("1000001"), "First account should be 1000001, got " + a1);
                expect(a2.equals("1000002"), "Second account should be 1000002, got " + a2);
            } catch (Exception e) { throw new RuntimeException(e.getMessage()); }
        });

        // 2. Duplicate phone rejected
        test("Duplicate phone number rejected", () -> {
            try {
                register("Alice Duplicate", "0600000001", "anypass", 100.0);
                throw new RuntimeException("Should have been rejected");
            } catch (Exception e) {
                expect(e.getMessage().contains("Phone already registered"), "Wrong error: " + e.getMessage());
            }
        });

        // 3. Password is stored as hash (not plaintext)
        test("Password stored as SHA-256 hash, not plaintext", () -> {
            User u = accounts.get("1000001");
            expect(!u.passwordHash.equals("pass123"), "Password must not be stored plaintext");
            expect(u.passwordHash.length() == 64, "SHA-256 should be 64 hex chars");
        });

        // 4. Login with correct credentials
        test("Login succeeds with correct credentials", () -> {
            try {
                User u = login("1000001", "pass123");
                expect(u.name.equals("Alice Smith"), "Wrong user returned");
            } catch (Exception e) { throw new RuntimeException(e.getMessage()); }
        });

        // 5. Login fails with wrong password
        test("Login fails with wrong password", () -> {
            try {
                login("1000001", "wrongpassword");
                throw new RuntimeException("Should have failed");
            } catch (Exception e) {
                expect(e.getMessage().contains("Invalid"), "Wrong error: " + e.getMessage());
            }
        });

        // 6. Deposit increases balance
        test("Deposit increases balance correctly", () -> {
            try {
                double bal = deposit("1000001", 200.0);
                expect(bal == 700.0, "Expected 700.0 after deposit, got " + bal);
            } catch (Exception e) { throw new RuntimeException(e.getMessage()); }
        });

        // 7. Withdrawal decreases balance
        test("Withdrawal decreases balance correctly", () -> {
            try {
                double bal = withdraw("1000001", 100.0);
                expect(bal == 600.0, "Expected 600.0 after withdraw, got " + bal);
            } catch (Exception e) { throw new RuntimeException(e.getMessage()); }
        });

        // 8. Overdraft rejected
        test("Overdraft (withdraw more than balance) rejected", () -> {
            try {
                withdraw("1000001", 99999.0);
                throw new RuntimeException("Overdraft should have been rejected");
            } catch (Exception e) {
                expect(e.getMessage().contains("Insufficient"), "Wrong error: " + e.getMessage());
            }
        });

        // 9. Transfer to existing account succeeds
        test("Transfer to existing account succeeds", () -> {
            try {
                double newBal = transfer("1000002", "1000001", 200.0);
                expect(newBal == 800.0, "Bob should have 800 remaining, got " + newBal);
                expect(accounts.get("1000001").balance == 800.0,
                    "Alice should have 800 after receiving, got " + accounts.get("1000001").balance);
            } catch (Exception e) { throw new RuntimeException(e.getMessage()); }
        });

        // 10. Transfer to non-existent account REJECTED
        test("Transfer to non-existent account strictly rejected", () -> {
            try {
                transfer("1000001", "9999999", 50.0);
                throw new RuntimeException("Should have been rejected");
            } catch (Exception e) {
                expect(e.getMessage().contains("does not exist") || e.getMessage().contains("not found"),
                    "Wrong error: " + e.getMessage());
            }
        });

        // 11. Transfer to self rejected
        test("Transfer to own account rejected", () -> {
            try {
                transfer("1000001", "1000001", 50.0);
                throw new RuntimeException("Should have been rejected");
            } catch (Exception e) {
                expect(e.getMessage().contains("self") || e.getMessage().contains("own"),
                    "Wrong error: " + e.getMessage());
            }
        });

        // 12. Transfer with insufficient funds rejected
        test("Transfer with insufficient funds rejected", () -> {
            try {
                transfer("1000001", "1000002", 999999.0);
                throw new RuntimeException("Should have been rejected");
            } catch (Exception e) {
                expect(e.getMessage().contains("Insufficient"), "Wrong error: " + e.getMessage());
            }
        });

        // Summary
        System.out.println("\n───────────────────────────────────────────────");
        System.out.printf("  Results: %d passed, %d failed%n", passed, failed);
        System.out.println("───────────────────────────────────────────────\n");
        if (failed > 0) System.exit(1);
    }
}
