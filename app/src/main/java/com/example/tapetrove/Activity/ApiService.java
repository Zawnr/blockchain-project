package com.example.tapetrove.Activity;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    /**
     * Endpoint untuk meminta pembuatan transaksi baru dari backend.
     * Backend akan mengembalikan Snap Token dari Midtrans.
     * @param request Body dari request yang berisi detail transaksi (orderId, grossAmount, dll)
     */
    @POST("api/create-transaction")
    Call<SnapTokenResponse> createTransaction(@Body TransactionRequest request);
}
