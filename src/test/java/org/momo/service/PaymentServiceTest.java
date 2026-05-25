package org.momo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.momo.model.Payment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentServiceTest {
    private AccountService account;
    private BillService billService;
    private PaymentService paymentService;
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @BeforeEach
    public void setup() {
        account = new AccountService();
        billService = new BillService();
        paymentService = new PaymentService(account, billService);
        // two test bills
        billService.addBill(new org.momo.model.Bill("TEST", 10L, "P1", 100L, LocalDate.parse("01/01/2025", F), "NOT_PAID"));
        billService.addBill(new org.momo.model.Bill("TEST", 11L, "P2", 200L, LocalDate.parse("02/01/2025", F), "NOT_PAID"));
    }

    @Test
    public void depositAndPaySingleBill() {
        account.deposit(150);
        boolean ok = paymentService.payBills(List.of(10L));
        assertTrue(ok);
        assertEquals(50, account.getBalance());
        assertEquals("PAID", billService.findById(10L).get().getState());
    }

    @Test
    public void payMultipleBillsSuccess() {
        account.deposit(350);
        boolean ok = paymentService.payBills(List.of(11L, 10L)); // order shouldn't matter; prioritized by due date
        assertTrue(ok);
        // total 300 withdrawn
        assertEquals(50, account.getBalance());
        assertEquals("PAID", billService.findById(10L).get().getState());
        assertEquals("PAID", billService.findById(11L).get().getState());
    }

    @Test
    public void insufficientFundsForMultipleIsAtomic() {
        account.deposit(250);
        // trying to pay both 10 and 11 needs 300, so should fail and not change anything
        boolean ok = paymentService.payBills(List.of(10L, 11L));
        assertFalse(ok);
        assertEquals(250, account.getBalance());
        assertEquals("NOT_PAID", billService.findById(10L).get().getState());
        assertEquals("NOT_PAID", billService.findById(11L).get().getState());
    }

    @Test
    public void payNonExistentBillFails() {
        account.deposit(1000);
        boolean ok = paymentService.payBills(List.of(99L));
        assertFalse(ok);
        // ensure no side-effects on existing bills
        assertEquals("NOT_PAID", billService.findById(10L).get().getState());
        assertEquals("NOT_PAID", billService.findById(11L).get().getState());
    }

    @Test
    public void schedulePaymentCreatesPendingEntry() {
        LocalDate when = LocalDate.now().plusDays(2);
        boolean scheduled = paymentService.schedulePayment(11L, when);
        assertTrue(scheduled);

        List<Payment> payments = paymentService.listPayments();
        assertFalse(payments.isEmpty());
        Optional<Payment> pOpt = payments.stream().filter(p -> p.getBillId() == 11L).findFirst();
        assertTrue(pOpt.isPresent());
        Payment p = pOpt.get();
        assertEquals("PENDING", p.getState());
        assertEquals(when, p.getScheduledOn());
    }

    @Test
    public void processDueScheduledPaymentsWithSufficientFundsProcessesAndMarksPaid() {
        LocalDate due = LocalDate.now().minusDays(1); // already due
        boolean scheduled = paymentService.schedulePayment(11L, due);
        assertTrue(scheduled);

        // deposit enough to cover scheduled payment
        account.deposit(200);
        // process directly
        paymentService.processDueScheduledPayments();

        // scheduled payment should be processed
        List<Payment> payments = paymentService.listPayments();
        Optional<Payment> pOpt = payments.stream().filter(p -> p.getBillId() == 11L).findFirst();
        assertTrue(pOpt.isPresent());
        Payment p = pOpt.get();
        assertEquals("PROCESSED", p.getState());
        assertEquals(LocalDate.now(), p.getPaymentDate());
        // bill should be marked paid and balance reduced
        assertEquals("PAID", billService.findById(11L).get().getState());
        assertEquals(0, account.getBalance());
    }

    @Test
    public void processDueScheduledPaymentsInsufficientFundsKeepsPending() {
        LocalDate due = LocalDate.now().minusDays(1); // already due
        boolean scheduled = paymentService.schedulePayment(11L, due);
        assertTrue(scheduled);

        // deposit less than required
        account.deposit(50);
        paymentService.processDueScheduledPayments();

        List<Payment> payments = paymentService.listPayments();
        Optional<Payment> pOpt = payments.stream().filter(p -> p.getBillId() == 11L).findFirst();
        assertTrue(pOpt.isPresent());
        Payment p = pOpt.get();
        // still pending because not enough funds
        assertEquals("PENDING", p.getState());
        assertEquals("NOT_PAID", billService.findById(11L).get().getState());
        // balance unchanged
        assertEquals(50, account.getBalance());
    }

    @Test
    public void schedulePaymentForNonExistentOrAlreadyPaidBillFails() {
        // non-existent bill
        boolean scheduled = paymentService.schedulePayment(99L, LocalDate.now());
        assertFalse(scheduled);

        // already paid bill
        account.deposit(200);
        boolean paidOk = paymentService.payBills(List.of(11L));
        assertTrue(paidOk);
        boolean scheduledAgain = paymentService.schedulePayment(11L, LocalDate.now());
        assertFalse(scheduledAgain);
    }
}