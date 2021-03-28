package com.hackahorse.horseback.service;

import com.hackahorse.horseback.dto.DepositDTO;
import org.brunocvcunha.coinpayments.model.CreateTransactionResponse;
import org.brunocvcunha.coinpayments.model.ResponseWrapper;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StatusChecker implements Runnable {

    private static final Logger log = java.util.logging.Logger.getLogger(StatusChecker.class.getName());
    int status;
    Thread worker;
    private String workerName;
    private DepositDTO deposit;
    ResponseWrapper<CreateTransactionResponse> response;
    private AtomicBoolean running = new AtomicBoolean(false);

    StatusChecker(String name, ResponseWrapper<CreateTransactionResponse> txResponse, DepositDTO depositDTO) {
        workerName = name;
        response = txResponse;
        deposit = depositDTO;
    }

    @Override
    public void run() {
        log.log(Level.INFO,"Thread [" + workerName + "] is running...");
        try {
            while (true) {
                status = CoinpaymentsService.getPaymentStatus(response);
                if (status >= 100) {
                    log.log(Level.INFO, "Payment completed!");
                    CoinpaymentsService.issue(deposit);
                    Thread.currentThread().interrupt();
                }
                else if (status < 0) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Payment refused!");
                } else {
                    log.log(Level.INFO, "Waiting confirmation... " + status);
                    Thread.sleep(100000); // Add one zero in prod!
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            log.log(Level.INFO, "Thread [" + workerName + "] interrupted!");
        }
    }
}
