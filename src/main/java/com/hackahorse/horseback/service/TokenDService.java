package com.hackahorse.horseback.service;

import com.hackahorse.horseback.util.PropsLoader;
import org.tokend.sdk.api.TokenDApi;
import org.tokend.sdk.api.generated.resources.OfferResource;
import org.tokend.sdk.api.v3.TokenDApiV3;
import org.tokend.sdk.api.v3.assets.params.AssetsPageParams;
import org.tokend.sdk.api.v3.offers.params.OffersPageParamsV3;
import org.tokend.sdk.api.v3.signers.params.SignerRolesPageParamsV3;
import org.tokend.sdk.keyserver.KeyServer;
import org.tokend.sdk.keyserver.models.WalletCreateResult;
import org.tokend.sdk.signing.AccountRequestSigner;
import org.tokend.wallet.Account;
import org.tokend.wallet.Base32Check;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TokenDService {

    private static final Logger log = java.util.logging.Logger.getLogger(TokenDService.class.getName());

    private static Properties prop;
    private static KeyServer keyServer;
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
            TokenDApi api = new TokenDApi(prop.getProperty("tokend_base_url"), accountRequestSigner);
            v3api = api.getV3();
            log.log(Level.INFO, v3api.getSigners().getRoles(new SignerRolesPageParamsV3()).execute().get().getItems().toArray().toString());
            log.log(Level.INFO, "TokenD connection established...");
            keyServer = new KeyServer(api.getWallets());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<OfferResource> getOffers() {
        List<OfferResource> offers = v3api.getOffers().get(new OffersPageParamsV3()).execute().get().getItems();
        return offers;
    }

}
