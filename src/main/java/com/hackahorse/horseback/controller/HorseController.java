package com.hackahorse.horseback.controller;

import com.hackahorse.horseback.service.TokenDService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tokend.sdk.api.generated.resources.OfferResource;

import java.util.List;

@RestController
@RequestMapping(value = "/bet")
public class HorseController {

    @GetMapping("/offers")
    public List<OfferResource> getOffers() {
        return TokenDService.getOffers();
    }

}
