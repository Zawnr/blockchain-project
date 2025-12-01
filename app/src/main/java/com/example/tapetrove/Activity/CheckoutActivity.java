package com.example.tapetrove.Activity;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tapetrove.BuildConfig;
import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback;
import com.midtrans.sdk.corekit.core.MidtransSDK;
import com.midtrans.sdk.corekit.models.snap.TransactionResult;
import com.midtrans.sdk.uikit.SdkUIFlowBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CheckoutActivity extends AppCompatActivity implements TransactionFinishedCallback {

    private BlockchainService blockchainService;

    private static final String BACKEND_BASE_URL = "http://10.0.2.2:3000/";
    public static final String EXTRA_GROSS_AMOUNT = "extra_gross_amount";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        blockchainService = new BlockchainService();
        initMidtransSdk();
        fetchSnapToken();
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
        showToast("Requesting transaction from server...");

        long grossAmount = getIntent().getLongExtra(EXTRA_GROSS_AMOUNT, 0);
        if (grossAmount == 0) {
            showToast("Invalid amount");
            finish();
            return;
        }

        String orderId = "ORDER-" + System.currentTimeMillis();
        TransactionRequest request = new TransactionRequest(orderId, grossAmount);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BACKEND_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        apiService.createTransaction(request).enqueue(new Callback<SnapTokenResponse>() {
            @Override
            public void onResponse(Call<SnapTokenResponse> call, Response<SnapTokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String snapToken = response.body().getToken();
                    showToast("Snap Token received. Starting payment...");
                    startPayment(snapToken);
                } else {
                    showToast("Failed to get Snap Token from server.");
                    finish();
                }
            }

            @Override
            public void onFailure(Call<SnapTokenResponse> call, Throwable t) {
                showToast("Network Error: " + t.getMessage());
                finish();
            }
        });
    }

    private void startPayment(String snapToken) {
        MidtransSDK.getInstance().setTransactionRequest(null); // Clear previous transaction
        MidtransSDK.getInstance().startPaymentUiFlow(this, snapToken);
    }

    @Override
    public void onTransactionFinished(TransactionResult result) {
        if (result.isTransactionCanceled()) {
            showToast("Transaction Canceled");
            finish();
        } else if (result.getStatus().equalsIgnoreCase("success")) {
            String transactionId = result.getResponse().getTransactionId();
            String amount = result.getResponse().getGrossAmount();
            String timestamp = result.getResponse().getTransactionTime();

            showToast("Payment Successful! ID: " + transactionId);
            logToBlockchain(transactionId, amount, timestamp);

        } else if (result.getStatus().equalsIgnoreCase("pending")) {
            showToast("Transaction Pending");
            finish();
        } else if (result.getStatus().equalsIgnoreCase("failed")) {
            showToast("Transaction Failed");
            finish();
        }
    }

    private void logToBlockchain(String transactionId, String amount, String timestamp) {
        blockchainService.logTransactionToBlockchain(transactionId, amount, timestamp)
                .thenAccept(txHash -> {
                    runOnUiThread(() -> {
                        showToast("Logged to Blockchain. TxHash: " + txHash);
                        finish();
                    });
                })
                .exceptionally(ex -> {
                    runOnUiThread(() -> {
                        showToast("Blockchain logging failed: " + ex.getMessage());
                        finish();
                    });
                    return null;
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
