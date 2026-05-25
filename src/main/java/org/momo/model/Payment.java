package org.momo.model;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

public class Payment {
    private static final AtomicLong ID_GEN = new AtomicLong(1);

    private final long id;
    private final long billId;
    private final long amount;
    private LocalDate paymentDate;
    private String state; // PENDING, PROCESSED, FAILED, CANCELLED
    private LocalDate scheduledOn;

    public Payment(long billId, long amount, LocalDate paymentDate, String state) {
        this.id = ID_GEN.getAndIncrement();
        this.billId = billId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.state = state;
    }

    public long getId() {
        return id;
    }

    public long getBillId() {
        return billId;
    }

    public long getAmount() {
        return amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public LocalDate getScheduledOn() {
        return scheduledOn;
    }

    public void setScheduledOn(LocalDate scheduledOn) {
        this.scheduledOn = scheduledOn;
    }
}