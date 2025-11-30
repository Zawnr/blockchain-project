package com.example.tapetrove.Activity;

import com.example.tapetrove.BuildConfig;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Numeric;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;

public class BlockchainService {

    private static final String PRIVATE_KEY = BuildConfig.MY_SEPOLIA_KEY;
    private static final String INFURA_URL = "https://sepolia.infura.io/v3/" + BuildConfig.MY_INFURA_ID;

    private final Web3j web3j;
    private final Credentials credentials;

    public BlockchainService() {
        this.web3j = Web3j.build(new HttpService(INFURA_URL));
        this.credentials = Credentials.create(PRIVATE_KEY);
    }

    public CompletableFuture<String> logTransactionToBlockchain(String transactionId, String amount, String timestamp) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String transactionDetails = "ID:" + transactionId + ", Amount:" + amount + ", TS:" + timestamp;
                String dataHash = toSha256(transactionDetails);

                BigInteger nonce = web3j.ethGetTransactionCount(credentials.getAddress(), org.web3j.protocol.core.DefaultBlockParameterName.LATEST).send().getTransactionCount();
                BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
                BigInteger gasLimit = BigInteger.valueOf(21000); // Standard gas limit

                RawTransaction rawTransaction = RawTransaction.createTransaction(
                        nonce,
                        gasPrice,
                        gasLimit,
                        "0x0000000000000000000000000000000000000000", // To address (none)
                        BigInteger.ZERO, // Value
                        dataHash // Data
                );

                byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
                String hexValue = Numeric.toHexString(signedMessage);

                return web3j.ethSendRawTransaction(hexValue).send().getTransactionHash();

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error logging transaction to blockchain", e);
            }
        });
    }

    private String toSha256(String data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return Numeric.toHexString(hash);
    }
}
