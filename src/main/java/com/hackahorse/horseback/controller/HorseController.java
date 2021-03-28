package com.hackahorse.horseback.controller;

import com.hackahorse.horseback.dto.BetDTO;
import com.hackahorse.horseback.service.TokenDService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tokend.sdk.api.integrations.marketplace.model.MarketplaceOfferResource;

import java.awt.image.BufferedImage;
import java.util.List;

@RestController
@RequestMapping(value = "/bet")
public class HorseController {

    @GetMapping("/offers")
    public List<MarketplaceOfferResource> getOffers() {
        return TokenDService.getOffers();
    }

//    @GetMapping("/prize-fund")
//    public String getPrizeFund() {
//        return TokenDService.getPrizeFund().toString();
//    }

    @PostMapping(value = "/")
    public String bet(@RequestBody BetDTO bet) throws Exception {
        return TokenDService.bet(bet);
    }

    @GetMapping("/commitment/{dataId:.+}")
    public String getWitness(@PathVariable String dataId) {
        return TokenDService.getCommitment(dataId);
    }

}
