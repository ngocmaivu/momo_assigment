package org.momo.service.impl;

import org.momo.model.Payment;

import java.time.LocalDate;
import java.util.List;

public interface PaymentServiceImp {
    boolean schedulePayment(long billId, LocalDate when);
    List<Payment> listPayments();
}
