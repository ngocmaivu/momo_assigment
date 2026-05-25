package org.momo.service;

import org.momo.service.impl.AccountServiceImpl;

public class AccountService implements AccountServiceImpl {
    private long balance = 0;

    public synchronized void deposit(long amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        balance += amount;
    }

    public synchronized boolean withdraw(long amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (amount > balance) return false;
        balance -= amount;
        return true;
    }

    public synchronized long getBalance() {
        return balance;
    }
}