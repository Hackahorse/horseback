package com.hackahorse.horseback.service;

import com.hackahorse.horseback.util.PropsLoader;
import org.json.JSONObject;
import org.tokend.sdk.api.TokenDApi;
import org.tokend.sdk.api.generated.resources.BalanceResource;
import org.tokend.sdk.api.integrations.marketplace.MarketplaceApi;
import org.tokend.sdk.api.integrations.marketplace.model.MarketplaceOfferResource;
import org.tokend.sdk.api.integrations.marketplace.params.MarketplaceOffersPageParams;
import org.tokend.sdk.api.v3.TokenDApiV3;
import org.tokend.sdk.api.v3.signers.params.SignerRolesPageParamsV3;
import org.tokend.sdk.keyserver.KeyServer;
import org.tokend.sdk.keyserver.models.WalletCreateResult;
import org.tokend.sdk.signing.AccountRequestSigner;
import org.tokend.wallet.Account;
import org.tokend.wallet.Base32Check;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TokenDService {

    private static final Logger log = java.util.logging.Logger.getLogger(TokenDService.class.getName());

    private static Properties prop;
    private static KeyServer keyServer;
    private static TokenDApi api;
    private static TokenDApiV3 v3api;

    private static Account defaultSignerRole;
    private static WalletCreateResult wallet;

    private static AccountRequestSigner accountRequestSigner;

    static {
        try {
            prop = PropsLoader.loadConfigProps();
            log.log(Level.INFO, "Props loaded successfully...");
            defaultSignerRole = Account.fromSecretSeed(Base32Check
                    .decodeSecretSeed("SAMJKTZVW5UOHCDK5INYJNORF2HRKYI72M5XSZCBYAHQHR34FFR4Z6G4".toCharArray())
            );
            log.log(Level.INFO, "Default signer role created...");
            accountRequestSigner = new AccountRequestSigner(defaultSignerRole);
            api = new TokenDApi(prop.getProperty("tokend_base_url"), accountRequestSigner);
            v3api = api.getV3();
            log.log(Level.INFO, v3api.getSigners().getRoles(new SignerRolesPageParamsV3()).execute().get().getItems().toArray().toString());
            log.log(Level.INFO, "TokenD connection established...");
            keyServer = new KeyServer(api.getWallets());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<MarketplaceOfferResource> getOffers() {
        MarketplaceApi marketplaceApi = api.getIntegrations().getMarketplace();
        return marketplaceApi.getOffers(new MarketplaceOffersPageParams()).execute().get().getItems();
    }

    public static JSONObject getPrizeFund() {
         balancesList = v3api.getAccounts().getBalances("GDSX2VTTBFS2PACKNPPUL4E5NUDSO6LG6WVP6AOG6ZWENZEXVT4JK2YW")
                .execute().get();
        JSONObject json = new JSONObject();
        while (balancesList.hasNext()) {
            json.put(balancesList.next().getAsset(), balancesList.next().getAsset().getIssued());
        }
        return json;
    }
}
