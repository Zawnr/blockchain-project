package com.example.tapetrove.Activity;

import com.google.gson.annotations.SerializedName;

/**
 * Model untuk data yang dikirim ke backend untuk membuat transaksi Midtrans.
 * Nama field disesuaikan dengan yang diharapkan oleh server.js (snake_case).
 */
public class TransactionRequest {

    @SerializedName("order_id")
    private String orderId;

    @SerializedName("gross_amount")
    private long grossAmount;

    public TransactionRequest(String orderId, long grossAmount) {
        this.orderId = orderId;
        this.grossAmount = grossAmount;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public long getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(long grossAmount) {
        this.grossAmount = grossAmount;
    }
}
