// Test for requirements:
// He often checks payment transaction history to
// ensure that there is nothing wrong
// with his fund as well.
package org.momo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.momo.model.Payment;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentHistoryTest {
    private AccountService account;
    private PaymentService paymentService;

    @BeforeEach
    public void setup() {
        account = new AccountService();
        BillService billService = new BillService();
        paymentService = new PaymentService(account, billService);

        billService.addBill(new org.momo.model.Bill("TEST", 20L, "P1", 150L, LocalDate.now().plusDays(1), "NOT_PAID"));
        billService.addBill(new org.momo.model.Bill("TEST", 21L, "P2", 250L, LocalDate.now().plusDays(2), "NOT_PAID"));
    }

    @Test
    public void immediatePaymentCreatesProcessedRecord() {
        account.deposit(200);
        boolean ok = paymentService.payBills(List.of(20L));
        assertTrue(ok);

        List<Payment> payments = paymentService.listPayments();
        Optional<Payment> pOpt = payments.stream().filter(p -> p.getBillId() == 20L).findFirst();
        assertTrue(pOpt.isPresent());
        Payment p = pOpt.get();
        assertEquals("PROCESSED", p.getState());
        assertEquals(LocalDate.now(), p.getPaymentDate());
    }

    @Test
    public void multiplePaymentsCreateSeparateRecords() {
        account.deposit(500);
        boolean ok = paymentService.payBills(List.of(21L, 20L));
        assertTrue(ok);

        List<Payment> payments = paymentService.listPayments();
        // both bill ids should have corresponding processed records
        assertTrue(payments.stream().anyMatch(p -> p.getBillId() == 20L && "PROCESSED".equals(p.getState())));
        assertTrue(payments.stream().anyMatch(p -> p.getBillId() == 21L && "PROCESSED".equals(p.getState())));
    }

    @Test
    public void scheduledPaymentAppearsAndIsProcessedWhenDueAndFunded() {
        LocalDate past = LocalDate.now().minusDays(1);
        boolean scheduled = paymentService.schedulePayment(21L, past);
        assertTrue(scheduled);

        // before funding, should be PENDING
        Optional<Payment> before = paymentService.listPayments().stream().filter(p -> p.getBillId() == 21L).findFirst();
        assertTrue(before.isPresent());
        assertEquals("PENDING", before.get().getState());

        // fund and process
        account.deposit(250);
        paymentService.processDueScheduledPayments();

        Optional<Payment> after = paymentService.listPayments().stream().filter(p -> p.getBillId() == 21L).findFirst();
        assertTrue(after.isPresent());
        assertEquals("PROCESSED", after.get().getState());
        assertEquals(LocalDate.now(), after.get().getPaymentDate());
    }

    @Test
    public void payingNonExistentBillDoesNotCreateRecord() {
        account.deposit(1000);
        boolean ok = paymentService.payBills(List.of(999L)); // non-existent
        assertFalse(ok);

        List<Payment> payments = paymentService.listPayments();
        assertTrue(payments.stream().noneMatch(p -> p.getBillId() == 999L));
    }
}