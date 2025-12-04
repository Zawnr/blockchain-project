package com.example.tapetrove.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tapetrove.BuildConfig;
import com.example.tapetrove.R;
// Import lengkap Midtrans Corekit
import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback;
import com.midtrans.sdk.corekit.core.MidtransSDK;
// PERBAIKAN: Gunakan TransactionRequest dari package 'core', bukan 'models'
import com.midtrans.sdk.corekit.core.TransactionRequest;
import com.midtrans.sdk.corekit.models.BillingAddress;
import com.midtrans.sdk.corekit.models.CustomerDetails;
import com.midtrans.sdk.corekit.models.ItemDetails;
import com.midtrans.sdk.corekit.models.ShippingAddress;
import com.midtrans.sdk.corekit.models.snap.TransactionResult;
import com.midtrans.sdk.uikit.SdkUIFlowBuilder;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CheckoutActivity extends AppCompatActivity implements TransactionFinishedCallback {

    private static final String TAG = "CheckoutActivity";
    private BlockchainService blockchainService;
    private ProgressBar progressBar;

    public static final String EXTRA_GROSS_AMOUNT = "extra_gross_amount";

    private String currentOrderId;
    private long currentGrossAmount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        progressBar = findViewById(R.id.progressBar);

        try {
            initMidtransSdk();
            fetchSnapToken();
        } catch (Exception e) {
            Log.e(TAG, "Initialization error", e);
            Toast.makeText(this, "Error inisialisasi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initMidtransSdk() {
        SdkUIFlowBuilder.init()
                .setClientKey(BuildConfig.MIDTRANS_CLIENT_KEY)
                .setContext(this)
                .setTransactionFinishedCallback(this)
                .setMerchantBaseUrl(BuildConfig.MERCHANT_BASE_URL)
                .enableLog(true)
                .buildSDK();
    }

    private void fetchSnapToken() {
        showLoading(true, "Memproses transaksi...");

        currentGrossAmount = getIntent().getLongExtra(EXTRA_GROSS_AMOUNT, 0);
        if (currentGrossAmount == 0) {
            showLoading(false, "");
            Toast.makeText(this, "Jumlah tidak valid", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        currentOrderId = "ORDER-" + System.currentTimeMillis();

        // Pastikan menggunakan BackendTransactionRequest (bukan TransactionRequest bawaan Midtrans)
        BackendTransactionRequest request = new BackendTransactionRequest(currentOrderId, currentGrossAmount);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.MERCHANT_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        apiService.createTransaction(request).enqueue(new Callback<SnapTokenResponse>() {
            @Override
            public void onResponse(Call<SnapTokenResponse> call, Response<SnapTokenResponse> response) {
                showLoading(false, "");
                if (response.isSuccessful() && response.body() != null) {
                    String snapToken = response.body().getToken();
                    Log.d(TAG, "Token received: " + snapToken);
                    startPayment(snapToken);
                } else {
                    Log.e(TAG, "Gagal dapat Token: " + response.message());
                    Toast.makeText(CheckoutActivity.this, "Gagal koneksi ke server.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<SnapTokenResponse> call, Throwable t) {
                showLoading(false, "");
                Log.e(TAG, "Network Error", t);
                Toast.makeText(CheckoutActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void startPayment(String snapToken) {
        // --- FIX "invalid customerDetails": Isi Data Selengkap-lengkapnya ---

        // 1. Siapkan Customer Details
        CustomerDetails customerDetails = getCustomerDetails();

        // 4. Siapkan Item Details (Dummy item agar validasi lolos)
        ArrayList<ItemDetails> itemDetailsList = new ArrayList<>();
        // ID, Harga, Jumlah, Nama
        ItemDetails item = new ItemDetails("ITEM-01", (double) currentGrossAmount, 1, "Top Up Balance");
        itemDetailsList.add(item);

        // 5. Buat Request Midtrans SDK
        // FIX: Gunakan class TransactionRequest yang sudah diimport dari 'core'
        // Jangan pakai com.midtrans.sdk.corekit.models.TransactionRequest
        TransactionRequest midtransRequest = new TransactionRequest(currentOrderId, currentGrossAmount);

        // Masukkan semua data ke request
        midtransRequest.setCustomerDetails(customerDetails);
        midtransRequest.setItemDetails(itemDetailsList);

        // 6. Set ke Instance SDK & Mulai
        MidtransSDK.getInstance().setTransactionRequest(midtransRequest);

        Log.d(TAG, "Starting Payment UI Flow with Token: " + snapToken);
        MidtransSDK.getInstance().startPaymentUiFlow(this, snapToken);
    }

    @NonNull
    private static CustomerDetails getCustomerDetails() {
        CustomerDetails customerDetails = new CustomerDetails();
        customerDetails.setFirstName("Pengguna");
        customerDetails.setLastName("TapeTrove");
        customerDetails.setEmail("user@tapetrove.com");
        customerDetails.setPhone("08123456789");

        // 2. Siapkan Billing Address (Wajib di beberapa versi SDK)
        BillingAddress billingAddress = new BillingAddress();
        billingAddress.setFirstName("Pengguna");
        billingAddress.setLastName("TapeTrove");
        billingAddress.setAddress("Jl. Contoh No. 123");
        billingAddress.setCity("Jakarta");
        billingAddress.setPostalCode("12345");
        billingAddress.setPhone("08123456789");
        billingAddress.setCountryCode("IDN");
        customerDetails.setBillingAddress(billingAddress);

        // 3. Siapkan Shipping Address (Wajib di beberapa versi SDK)
        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setFirstName("Pengguna");
        shippingAddress.setLastName("TapeTrove");
        shippingAddress.setAddress("Jl. Contoh No. 123");
        shippingAddress.setCity("Jakarta");
        shippingAddress.setPostalCode("12345");
        shippingAddress.setPhone("08123456789");
        shippingAddress.setCountryCode("IDN");
        customerDetails.setShippingAddress(shippingAddress);
        return customerDetails;
    }

    @Override
    public void onTransactionFinished(TransactionResult result) {
        if (result == null) {
            Log.e(TAG, "TransactionResult is null");
            Toast.makeText(this, "Terjadi kesalahan sistem.", Toast.LENGTH_LONG).show();
            return;
        }

        if (result.isTransactionCanceled()) {
            Toast.makeText(this, "Transaksi Dibatalkan", Toast.LENGTH_SHORT).show();
        } else if (result.getStatus().equalsIgnoreCase("success")) {
            String transactionId = (result.getResponse() != null) ? result.getResponse().getTransactionId() : currentOrderId;
            String amount = String.valueOf(currentGrossAmount);
            String timestamp = (result.getResponse() != null) ? result.getResponse().getTransactionTime() : String.valueOf(System.currentTimeMillis());

            showLoading(true, "Pembayaran Sukses. Mencatat ke Blockchain...");
            logToBlockchain(transactionId, amount, timestamp);
        } else if (result.getStatus().equalsIgnoreCase("pending")) {
            Toast.makeText(this, "Transaksi Pending", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Transaksi Gagal", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void logToBlockchain(String transactionId, String amount, String timestamp) {
        if (blockchainService == null) {
            blockchainService = new BlockchainService();
        }

        blockchainService.logTransactionToBlockchain(transactionId, amount, timestamp)
                .thenAccept(txHash -> {
                    runOnUiThread(() -> {
                        showLoading(false, "");
                        Toast.makeText(this, "Sukses! TxHash: " + txHash, Toast.LENGTH_LONG).show();
                        finish();
                    });
                })
                .exceptionally(ex -> {
                    runOnUiThread(() -> {
                        showLoading(false, "");
                        Log.e(TAG, "Gagal catat ke blockchain", ex);
                        Toast.makeText(this, "Pembayaran sukses, tapi gagal catat blockchain.", Toast.LENGTH_LONG).show();
                        finish();
                    });
                    return null;
                });
    }

    private void showLoading(boolean isLoading, String message) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (isLoading && message != null && !message.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
}