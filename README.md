# DsBank — Simple RMI Banking Application

## Project Structure

```
BankRMI/
├── src/
│   └── bank/
│       ├── BankService.java        ← Remote interface (RMI contract)
│       ├── BankServiceImpl.java    ← Server-side implementation
│       ├── BankServer.java         ← Starts RMI registry + binds service
│       ├── BankClient.java         ← Launches the GUI client
│       ├── User.java               ← Serializable user model
│       └── gui/
│           ├── LoginFrame.java     ← Login / Sign-up UI
│           └── BankingFrame.java   ← Banking dashboard UI
├── LogicTest.java                  ← Standalone logic test (no RMI/GUI needed)
└── README.md
```

---

## How to Build

Requires **Java JDK 11+** with `javac` available.

```bash
mkdir -p out
javac -d out -sourcepath src src/bank/*.java src/bank/gui/*.java
```

---

## How to Run

**Terminal 1 — Start the server:**
```bash
java -cp out bank.BankServer
```

**Terminal 2 — Start the client:**
```bash
java -cp out bank.BankClient
```

---

## How to Run Logic Tests (no GUI needed)

```bash
java LogicTest.java
```

---

## Design Decisions

| Feature | Decision |
|---|---|
| Account numbers | Sequential integers starting at **1000001** |
| Login credential | **Account number** + **password** |
| Password storage | **SHA-256 hash** only |
| Transfer validation | Destination account must exist; transfer is atomic |
| Overdraft protection | Withdraw/transfer rejected if balance insufficient |
