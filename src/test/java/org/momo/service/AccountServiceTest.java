package org.momo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AccountServiceTest {
    private AccountService accountService;

    @BeforeEach
    public void setUp() {
        accountService = new AccountService();
    }

    @Test
    public void testDeposit() {
        accountService.deposit(100);
        org.junit.jupiter.api.Assertions.assertEquals(100, accountService.getBalance());
    }

    @org.junit.jupiter.api.Test
    public void testWithdraw() {
        accountService.deposit(100);
        boolean result = accountService.withdraw(50);
        org.junit.jupiter.api.Assertions.assertTrue(result);
        org.junit.jupiter.api.Assertions.assertEquals(50, accountService.getBalance());
    }

    @org.junit.jupiter.api.Test
    public void testWithdrawInsufficientFunds() {
        accountService.deposit(100);
        boolean result = accountService.withdraw(150);
        org.junit.jupiter.api.Assertions.assertFalse(result);
        org.junit.jupiter.api.Assertions.assertEquals(100, accountService.getBalance());
    }

    @org.junit.jupiter.api.Test
    public void testDepositNegativeAmount() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            accountService.deposit(-50);
        });
    }

    @org.junit.jupiter.api.Test
    public void testWithdrawNegativeAmount() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            accountService.withdraw(-50);
        });
    }
}
