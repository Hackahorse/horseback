package com.hackahorse.horseback.dto;

public class DepositDTO {
    private int amount;
    private String currency;
    private String accountId;

    public DepositDTO(int amount, String currency, String accountId) {
        this.amount = amount;
        this.currency = currency;
        this.accountId = accountId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
