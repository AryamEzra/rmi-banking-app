package bank;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String accountNo;
    private String name;
    private String phone;
    private String passwordHash;   // SHA-256 hex
    private double balance;

    public User(String accountNo, String name, String phone,
                String passwordHash, double balance) {
        this.accountNo    = accountNo;
        this.name         = name;
        this.phone        = phone;
        this.passwordHash = passwordHash;
        this.balance      = balance;
    }

    public String  getAccountNo()    { return accountNo; }
    public String  getName()         { return name; }
    public String  getPhone()        { return phone; }
    public String  getPasswordHash() { return passwordHash; }
    public double  getBalance()      { return balance; }
    public void    setBalance(double b) { this.balance = b; }
}
