package com.hackahorse.horseback.dto;

public class DepositDTO {
    private Double amount;
    private String currency;
    private String accountId;

    public DepositDTO(Double amount, String currency, String accountId) {
        this.amount = amount;
        this.currency = currency;
        this.accountId = accountId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
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
