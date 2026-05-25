package org.momo.model;

import java.time.LocalDate;

public class Bill {
    private Long id;
    private String type;
    private Long amount;
    private LocalDate dueDate;
    private String state; // "NOT_PAID", "PAID""
    private String provider;

    public Bill(String type, Long id, String provider, Long amount, LocalDate dueDate, String state) {
        this.type = type;
        this.id = id;
        this.provider = provider;
        this.amount = amount;
        this.dueDate = dueDate;
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
