package org.momo.service.impl;

import org.momo.model.Bill;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BillServiceImpl {
    void addBill(Bill bill);

    Optional<Bill> findById(long id);

    List<Bill> listBills();

    List<Bill> listUnpaidOrderedByDue();

    void markPaid(long id);

    void markNotPaid(long id);

    List<Bill> searchByProvider(String providerQuery);

    List<Bill> searchByType(String typeQuery);

    void updateDueDate(long id, LocalDate newDate);

    boolean removeBill(long id);

    boolean updateBill(long id, String type, String provider, long amount, LocalDate dueDate);
}
