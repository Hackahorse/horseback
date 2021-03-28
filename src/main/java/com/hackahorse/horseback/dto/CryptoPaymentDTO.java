package com.hackahorse.horseback.dto;

public class CryptoPaymentDTO {
    private double amount;
    private String address;
    private String qrCodeUrl;

    public CryptoPaymentDTO(double amount, String address, String qrCodeUrl) {
        this.amount = amount;
        this.address = address;
        this.qrCodeUrl = qrCodeUrl;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }
}
