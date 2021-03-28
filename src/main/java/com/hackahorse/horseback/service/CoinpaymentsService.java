package com.hackahorse.horseback.service;

import com.hackahorse.horseback.dto.CryptoPaymentDTO;
import com.hackahorse.horseback.dto.DepositDTO;
import org.apache.http.impl.client.HttpClients;
import org.brunocvcunha.coinpayments.CoinPayments;
import org.brunocvcunha.coinpayments.model.BasicInfoResponse;
import org.brunocvcunha.coinpayments.model.CreateTransactionResponse;
import org.brunocvcunha.coinpayments.model.ResponseWrapper;
import org.brunocvcunha.coinpayments.model.TransactionDetailsResponse;
import org.brunocvcunha.coinpayments.requests.CoinPaymentsBasicAccountInfoRequest;
import org.brunocvcunha.coinpayments.requests.CoinPaymentsCreateTransactionRequest;
import org.brunocvcunha.coinpayments.requests.CoinPaymentsGetTransactionInfoRequest;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CoinpaymentsService {

    private static final Logger log = java.util.logging.Logger.getLogger(CoinpaymentsService.class.getName());
    private static CoinPayments api;
    private static ResponseWrapper<TransactionDetailsResponse> txDetails;

    static {
        api = CoinPayments.builder()
                .publicKey("ea384fc10229e715653628d2cbd14c171f54a411998c60a8213caf0413ff0f2d")
                .privateKey("586220EB9e6a621693cCF899fcab3E2053ef644104632b4B1969fba3B0bB09e2")
                .client(HttpClients.createDefault()).build();

        ResponseWrapper<BasicInfoResponse> accountInfo = null;
        try {
            accountInfo = api.sendRequest(new CoinPaymentsBasicAccountInfoRequest());
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("Account: " + accountInfo.getResult());
    }

    public static CryptoPaymentDTO deposit(DepositDTO depositDTO) throws IOException {
        ResponseWrapper<CreateTransactionResponse> txResponse = api.sendRequest(CoinPaymentsCreateTransactionRequest.builder()
                .amount(depositDTO.getAmount())
                .currencyPrice("USD")
                .currencyTransfer(depositDTO.getCurrency())
                .build());
        log.info(txResponse.getResult().getTransactionId() + " - " + txResponse.getResult().getStatusUrl());
        StatusChecker statusChecker = new StatusChecker(txResponse.getResult().getTransactionId(), txResponse, depositDTO);
        Thread statusWorker = new Thread(statusChecker);
        statusWorker.start();
        return new CryptoPaymentDTO(txResponse.getResult().getAmount(),
                txResponse.getResult().getAddress(), txResponse.getResult().getQrcodeUrl());
    }

    public static int getPaymentStatus(ResponseWrapper<CreateTransactionResponse> txResponse) throws IOException {
        txDetails = api.sendRequest(CoinPaymentsGetTransactionInfoRequest.builder()
                .txid(txResponse.getResult().getTransactionId())
                .build());
        return txDetails.getResult().getStatus();
    }

    public static void issue(DepositDTO depositDTO) {
        TokenDService.issue(depositDTO.getAmount(), depositDTO.getAccountId());
        log.log(Level.INFO, "Deposited!");
    }
}
