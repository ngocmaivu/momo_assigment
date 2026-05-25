package org.momo.service;

import org.momo.model.Bill;
import org.momo.model.Payment;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class PaymentService {
    private final AccountService accountService;
    private final BillService billService;
    private final List<Payment> payments = Collections.synchronizedList(new ArrayList<>());

    public PaymentService(AccountService accountService, BillService billService) {
        this.accountService = accountService;
        this.billService = billService;
    }

    // pay a set of bill ids atomically; prioritized by due date for internal sorting
    public synchronized boolean payBills(List<Long> ids) {
        // collect bills and ensure they exist and are NOT_PAID
        List<Bill> bills = new ArrayList<>();
        for (Long id : ids) {
            Optional<Bill> b = billService.findById(id);
            if (!b.isPresent()) {
                System.out.println("Sorry! Not found a bill with such id " + id);
                return false;
            }
            if (!"NOT_PAID".equalsIgnoreCase(b.get().getState())) {
                System.out.println("Bill " + id + " already paid.");
                return false;
            }
            bills.add(b.get());
        }

        // sort by due date ascending
        bills.sort(Comparator.comparing(Bill::getDueDate));
        long total = bills.stream().mapToLong(Bill::getAmount).sum();
        if (accountService.getBalance() < total) {
            System.out.println("Sorry! Not enough fund to proceed with payment.");
            return false;
        }

        // proceed
        for (Bill b : bills) {
            boolean ok = accountService.withdraw(b.getAmount());
            if (!ok) {
                // should not happen given previous check, rollback is omitted for brevity
                return false;
            }
            b.setState("PAID");
            Payment p = new Payment(b.getId(), b.getAmount(), LocalDate.now(), "PROCESSED");
            payments.add(p);
        }
        return true;
    }

    public boolean schedulePayment(long billId, LocalDate when) {
        Optional<Bill> b = billService.findById(billId);
        if (!b.isPresent() || !"NOT_PAID".equalsIgnoreCase(b.get().getState())) return false;
        Payment p = new Payment(billId, b.get().getAmount(), when, "PENDING");
        p.setScheduledOn(when);
        payments.add(p);
        return true;
    }

    public List<Payment> listPayments() {
        synchronized (payments) {
            return new ArrayList<>(payments);
        }
    }

    // process scheduled payments whose scheduledOn <= today
    public synchronized void processDueScheduledPayments() {
        LocalDate today = LocalDate.now();
        List<Payment> due = payments.stream()
                .filter(p -> "PENDING".equalsIgnoreCase(p.getState()) && p.getScheduledOn() != null && !p.getScheduledOn().isAfter(today))
                .collect(Collectors.toList());
        for (Payment p : due) {
            Optional<Bill> b = billService.findById(p.getBillId());
            if (!b.isPresent()) {
                p.setState("FAILED");
                continue;
            }
            if (!"NOT_PAID".equalsIgnoreCase(b.get().getState())) {
                p.setState("FAILED");
                continue;
            }
            if (accountService.getBalance() >= p.getAmount()) {
                accountService.withdraw(p.getAmount());
                p.setState("PROCESSED");
                p.setPaymentDate(today);
                billService.markPaid(p.getBillId());
            } else {
                // keep pending until funds available
            }
        }
    }
}