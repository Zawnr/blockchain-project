package com.example.tapetrove.Activity;

import com.google.gson.annotations.SerializedName;

/**
 * Model untuk data yang dikirim ke backend (Express JS) untuk meminta Snap Token.
 * Namanya diganti jadi 'BackendTransactionRequest' agar tidak bentrok dengan
 * class 'TransactionRequest' milik library Midtrans.
 */
public class BackendTransactionRequest {

    @SerializedName("order_id")
    private String orderId;

    @SerializedName("gross_amount")
    private long grossAmount;

    public BackendTransactionRequest(String orderId, long grossAmount) {
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