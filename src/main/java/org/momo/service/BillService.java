package org.momo.service;

import org.momo.model.Bill;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BillService {
    private final Map<Long, Bill> bills = new ConcurrentHashMap<>();

    public void addBill(Bill bill) {
        if (bill == null) {
            throw new IllegalArgumentException("bill must not be null");
        }
        long id = bill.getId();
        Bill existing = bills.putIfAbsent(id, bill);
        //check if a bill with the same id already exists
        if (existing != null) {
            throw new IllegalArgumentException("Bill with id " + id + " already exists");
        }
    }

    public Optional<Bill> findById(long id) {
        return Optional.ofNullable(bills.get(id));
    }

    public List<Bill> listBills() {
        return bills.values().stream().sorted(Comparator.comparingLong(Bill::getId)).collect(Collectors.toList());
    }

    public List<Bill> listUnpaidOrderedByDue() {
        return bills.values().stream()
                .filter(b -> "NOT_PAID".equalsIgnoreCase(b.getState()))
                .sorted(Comparator.comparing(Bill::getDueDate))
                .collect(Collectors.toList());
    }

    public List<Bill> searchByProvider(String provider) {
        String p = provider == null ? "" : provider.trim().toLowerCase();
        return bills.values().stream()
                .filter(b -> b.getProvider() != null && b.getProvider().toLowerCase().contains(p))
                .collect(Collectors.toList());
    }

    // Search by bill type (case-insensitive, contains)
    public List<Bill> searchByType(String type) {
        String t = type == null ? "" : type.trim().toLowerCase();
        return bills.values().stream()
                .filter(b -> b.getType() != null && b.getType().toLowerCase().contains(t))
                .collect(Collectors.toList());
    }

    public void markPaid(long billId) {
        Bill b = bills.get(billId);
        if (b != null) {
            b.setState("PAID");
        }
    }

    public void markNotPaid(long billId) {
        Bill b = bills.get(billId);
        if (b != null) {
            b.setState("NOT_PAID");
        }
    }

    public void updateDueDate(long billId, LocalDate newDate) {
        Bill b = bills.get(billId);
        if (b != null) b.setDueDate(newDate);
    }

    // Remove a bill
    public boolean removeBill(long billId) {
        return bills.remove(billId) != null;
    }

    // Update multiple fields of a bill
    public boolean updateBill(long billId, String type, String provider, long amount, LocalDate dueDate) {
        Bill b = bills.get(billId);
        if (b == null) return false;
        if (type != null && !type.isEmpty()) b.setType(type);
        if (provider != null && !provider.isEmpty()) b.setProvider(provider);
        if (amount > 0) b.setAmount(amount);
        if (dueDate != null) b.setDueDate(dueDate);
        return true;
    }
}