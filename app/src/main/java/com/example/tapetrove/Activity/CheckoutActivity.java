package com.example.tapetrove.Activity;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tapetrove.BuildConfig;
import com.midtrans.sdk.uikit.api.model.PaymentMethod;
import com.midtrans.sdk.uikit.api.model.TransactionResult;
import com.midtrans.sdk.uikit.external.UiKitApi;
import com.midtrans.sdk.uikit.internal.util.UiKitConstants;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CheckoutActivity extends AppCompatActivity {

    private BlockchainService blockchainService;

    // The IP address 10.0.2.2 is a special alias to your host loopback interface (i.e., 127.0.0.1 on your development machine)
    // This is needed for the Android emulator to connect to a server running on localhost.
    private static final String BACKEND_BASE_URL = "http://10.0.2.2:3000/";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        blockchainService = new BlockchainService();
        initMidtrans();
        fetchSnapToken(); // Start the process by fetching the token
    }

    private void initMidtrans() {
        UiKitApi.Builder builder = new UiKitApi.Builder()
                .withContext(this)
                .withMerchantUrl(BuildConfig.MERCHANT_BASE_URL)
                .withMerchantClientKey(BuildConfig.MIDTRANS_CLIENT_KEY)
                .setTransactionFinishedCallback(this::handleTransactionResult);
        UiKitApi.Companion.setBuiltInTokenStorage(true);
        builder.build();
    }

    private void fetchSnapToken() {
        showToast("Requesting transaction from server...");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BACKEND_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        apiService.createTransaction().enqueue(new Callback<SnapTokenResponse>() {
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
        ArrayList<PaymentMethod> paymentMethods = new ArrayList<>();
        paymentMethods.add(new PaymentMethod(com.midtrans.sdk.uikit.api.model.PaymentType.QRIS, com.midtrans.sdk.uikit.api.model.PaymentType.QRIS));

        UiKitApi.getDefault().getPaymentApi().startPayment(this, paymentMethods, snapToken, null, null);
    }

    private void handleTransactionResult(TransactionResult result) {
        if (result.isTransactionCanceled()) {
            showToast("Transaction Canceled");
            finish();
        } else if (result.getStatus().equals(UiKitConstants.STATUS_SUCCESS)) {
            String transactionId = result.getResponse().getTransactionId();
            String amount = result.getResponse().getGrossAmount();
            String timestamp = result.getResponse().getTransactionTime();

            showToast("Payment Successful! ID: " + transactionId);
            logToBlockchain(transactionId, amount, timestamp);

        } else if (result.getStatus().equals(UiKitConstants.STATUS_PENDING)) {
            showToast("Transaction Pending");
            finish();
        } else if (result.getStatus().equals(UiKitConstants.STATUS_FAILED)) {
            showToast("Transaction Failed");
            finish();
        }
    }

    private void logToBlockchain(String transactionId, String amount, String timestamp) {
        blockchainService.logTransactionToBlockchain(transactionId, amount, timestamp)
                .thenAccept(txHash -> {
                    runOnUiThread(() -> {
                        showToast("Logged to Blockchain. TxHash: " + txHash);
                        finish(); // Finish activity after logging
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
