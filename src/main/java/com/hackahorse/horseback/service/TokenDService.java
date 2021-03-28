package com.hackahorse.horseback.service;

import com.google.gson.Gson;
import com.hackahorse.horseback.dto.BetDTO;
import com.hackahorse.horseback.dto.PedersenCommitment;
import com.hackahorse.horseback.dto.Random;
import com.hackahorse.horseback.util.PropsLoader;
import com.mashape.unirest.http.Unirest;
import org.json.JSONObject;
import org.tokend.sdk.api.TokenDApi;
import org.tokend.sdk.api.integrations.marketplace.MarketplaceApi;
import org.tokend.sdk.api.integrations.marketplace.model.MarketplaceOfferResource;
import org.tokend.sdk.api.integrations.marketplace.params.MarketplaceOffersPageParams;
import org.tokend.sdk.api.v3.TokenDApiV3;
import org.tokend.sdk.api.v3.signers.params.SignerRolesPageParamsV3;
import org.tokend.sdk.keyserver.KeyServer;
import org.tokend.sdk.keyserver.models.WalletCreateResult;
import org.tokend.sdk.signing.AccountRequestSigner;
import org.tokend.wallet.*;
import org.tokend.wallet.Transaction;
import org.tokend.wallet.xdr.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TokenDService {

    private static final Logger log = java.util.logging.Logger.getLogger(TokenDService.class.getName());

    private static Properties prop;
    private static KeyServer keyServer;
    private static TokenDApi api;
    private static TokenDApiV3 v3api;
    private static MarketplaceApi marketplaceApi;

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
            marketplaceApi = api.getIntegrations().getMarketplace();
            keyServer = new KeyServer(api.getWallets());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<MarketplaceOfferResource> getOffers() {
        return marketplaceApi.getOffers(new MarketplaceOffersPageParams()).execute().get().getItems();
    }

    public static String bet(BetDTO bet) throws Exception {
        var witness = new PedersenCommitment.Witness(BigInteger.valueOf(Long.valueOf(bet.getTeamId())), Random.randomGen());
        PedersenCommitment.Commitment commitment = PedersenCommitment.generate(witness);
        var operation = new Operation.OperationBody.CreateData(
                new CreateDataOp(
                        57,
                        new Gson().toJson(commitment),
                        new EmptyExt.EmptyVersion()
                )
        );
        Transaction tx = new TransactionBuilder(
                api.getGeneral().getSystemInfo().execute().get().toNetworkParams(),
                defaultSignerRole.getXdrPublicKey()
        )
                .addSigner(defaultSignerRole)
                .addOperation(operation)
                .build();
        v3api.getTransactions().submit(tx, true).execute().get();

        Unirest.setTimeouts(0, 0);
        String response = Unirest.get("http://localhost:8000/_/api/v3/data?page[order]=desc&page[limit]=1")
                .asString().getBody();
        String containerId = new JSONObject(response).getJSONArray("data")
                .getJSONObject(0)
                .getString("id");
        witness.setContainerId(containerId);
        return new Gson().toJson(witness);
    }

    public static String getCommitment(String dataId) {
        var jsonCommitment = new JSONObject(api.getCustomRequests().get("v3/data/" + dataId, String.class).execute().get())
                .getJSONObject("data")
                .getJSONObject("attributes")
                .getJSONObject("value");
        return jsonCommitment.toString();
    }

//    public static JSONObject getPrizeFund() {
//         balancesList = v3api.getAccounts().getBalances("GDSX2VTTBFS2PACKNPPUL4E5NUDSO6LG6WVP6AOG6ZWENZEXVT4JK2YW")
//                .execute().get();
//        JSONObject json = new JSONObject();
//        while (balancesList.hasNext()) {
//            json.put(balancesList.next().getAsset(), balancesList.next().getAsset().getIssued());
//        }
//        return json;
//    }
}
