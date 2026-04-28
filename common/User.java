
package common;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    public String name;
    public String phone;
    public String accountNumber;
    public String passwordHash;
    public double balance;

    public User(String name, String phone, String accountNumber, String passwordHash, double balance) {
        this.name = name;
        this.phone = phone;
        this.accountNumber = accountNumber;
        this.passwordHash = passwordHash;
        this.balance = balance;
    }
}