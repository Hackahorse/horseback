package com.hackahorse.horseback.controller;

import com.hackahorse.horseback.dto.CryptoPaymentDTO;
import com.hackahorse.horseback.dto.DepositDTO;
import com.hackahorse.horseback.service.CoinpaymentsService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(value = "/")
public class CoinpaymentsController {
    @PostMapping("/deposit")
    public CryptoPaymentDTO deposit(@RequestBody DepositDTO depositDTO) throws IOException {
        return CoinpaymentsService.deposit(depositDTO);
    }
    @PostMapping("/issue")
    public void issue(@RequestBody DepositDTO depositDTO) {
        CoinpaymentsService.issue(depositDTO);
    }
}
