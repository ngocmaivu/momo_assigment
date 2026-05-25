package org.momo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.momo.model.Bill;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class BillServiceTest {
    private BillService billService;
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @BeforeEach
    public void setup() {
        billService = new BillService();
        // add three bills (ids intentionally out of order)
        billService.addBill(new Bill("ELECTRIC", 5L, "EVN HCMC", 200_000L, LocalDate.parse("25/10/2025", F), "NOT_PAID"));
        billService.addBill(new Bill("WATER", 2L, "SAVACO HCMC", 175_000L, LocalDate.parse("30/09/2025", F), "NOT_PAID"));
        billService.addBill(new Bill("INTERNET", 3L, "VNPT", 800_000L, LocalDate.parse("15/09/2025", F), "NOT_PAID"));
    }

    @Test
    public void addBillNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> billService.addBill(null));
    }

    @Test
    public void addBillDuplicateIdThrows() {
        Bill dup = new Bill("GAS", 5L, "GASCO", 120_000L, LocalDate.parse("01/11/2025", F), "NOT_PAID");
        assertThrows(IllegalArgumentException.class, () -> billService.addBill(dup));
    }

    @Test
    public void findByIdReturnsOptionalForExistingAndEmptyForMissing() {
        Optional<Bill> b = billService.findById(2L);
        assertTrue(b.isPresent());
        assertEquals(2L, b.get().getId());

        assertFalse(billService.findById(99L).isPresent());
    }

    @Test
    public void listBillsReturnsSortedById() {
        List<Bill> list = billService.listBills();
        assertEquals(List.of(2L, 3L, 5L), list.stream().map(Bill::getId).toList());
    }

    @Test
    public void listUnpaidOrderedByDueFiltersAndSortsByDueDate() {
        // mark one as paid so it won't appear
        billService.markPaid(2L);
        List<Bill> unpaid = billService.listUnpaidOrderedByDue();
        // remaining unpaid bills are ids 3 and 5; 3 has earlier due date (15/09) than 5 (25/10)
        assertEquals(2, unpaid.size());
        assertEquals(3L, unpaid.get(0).getId());
        assertEquals(5L, unpaid.get(1).getId());
    }

    @Test
    public void searchByProviderIsCaseInsensitiveAndContains() {
        List<Bill> res = billService.searchByProvider("vn");
        assertFalse(res.isEmpty());
        assertTrue(res.stream().anyMatch(b -> b.getProvider().equalsIgnoreCase("VNPT")));
    }

    @Test
    public void searchByTypeIsCaseInsensitiveAndContains() {
        List<Bill> res = billService.searchByType("elec");
        assertFalse(res.isEmpty());
        assertTrue(res.stream().anyMatch(b -> b.getType().equalsIgnoreCase("ELECTRIC")));
    }

    @Test
    public void markPaidAndMarkNotPaidToggleState() {
        billService.markPaid(3L);
        assertEquals("PAID", billService.findById(3L).get().getState());

        billService.markNotPaid(3L);
        assertEquals("NOT_PAID", billService.findById(3L).get().getState());
    }

    @Test
    public void updateDueDateUpdatesWhenExists() {
        LocalDate newDate = LocalDate.parse("01/12/2025", F);
        billService.updateDueDate(5L, newDate);
        assertEquals(newDate, billService.findById(5L).get().getDueDate());
    }

    @Test
    public void removeBillRemovesAndReturnsCorrectBoolean() {
        assertTrue(billService.removeBill(3L));
        assertFalse(billService.findById(3L).isPresent());
        assertFalse(billService.removeBill(99L));
    }

    @Test
    public void updateBillUpdatesFieldsAndReturnsFalseIfMissing() {
        LocalDate newDue = LocalDate.parse("31/12/2025", F);
        boolean ok = billService.updateBill(2L, "WATER_NEW", "SAVACO_UPDATED", 180_000L, newDue);
        assertTrue(ok);
        Bill updated = billService.findById(2L).get();
        assertEquals("WATER_NEW", updated.getType());
        assertEquals("SAVACO_UPDATED", updated.getProvider());
        assertEquals(180_000L, updated.getAmount());
        assertEquals(newDue, updated.getDueDate());

        boolean missing = billService.updateBill(99L, "X", "Y", 1L, LocalDate.now());
        assertFalse(missing);
    }
}