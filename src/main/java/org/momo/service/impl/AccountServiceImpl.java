package org.momo.service.impl;

public interface AccountServiceImpl {
        void deposit(long amount);
        boolean withdraw(long amount);
        long getBalance();
}
