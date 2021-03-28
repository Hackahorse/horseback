package com.hackahorse.horseback.service;

import com.google.gson.Gson;
import com.hackahorse.horseback.dto.BetDTO;
import com.hackahorse.horseback.dto.PedersenCommitment;
import com.hackahorse.horseback.dto.Random;
import com.hackahorse.horseback.util.PropsLoader;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Base64;
import org.tokend.sdk.api.TokenDApi;
import org.tokend.sdk.api.base.model.operations.IssuanceOperation;
import org.tokend.sdk.api.base.model.operations.PaymentOperation;
import org.tokend.sdk.api.generated.resources.BalanceResource;
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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.spec.ECPoint;
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
        witness.setAccountId(bet.getAccountId());
        return new Gson().toJson(witness);
    }

    public static String getCommitment(String dataId) {
        var jsonCommitment = new JSONObject(api.getCustomRequests().get("v3/data/" + dataId, String.class).execute().get())
                .getJSONObject("data")
                .getJSONObject("attributes")
                .getJSONObject("value");
        return jsonCommitment.toString();
    }

    public static void issue(double amount, String accountId) {

        var networkParams = api.getGeneral().getSystemInfo().execute().get().toNetworkParams();

        String balanceId = null;
        for (int i = 0; i < v3api.getAccounts().getBalances(accountId).execute().get().size(); i++) {
             balanceId = v3api.getAccounts().getBalances(accountId).execute().get().get(i)
                    .getAsset().getId();
            if (balanceId.equals("UAH")) {
                balanceId = v3api.getAccounts().getBalances(accountId).execute().get().get(i).getId();
                break;
            }
        }

        System.out.println(balanceId);

        var issuanceRequest = new IssuanceRequest(
                "UAH",
                api.getGeneral().getSystemInfo().execute().get().toNetworkParams().amountToPrecised(BigDecimal.valueOf(amount)),
                PublicKeyFactory.fromBalanceId(balanceId),
                "{}",
                new Fee(0, 0, new Fee.FeeExt.EmptyVersion()),
                new IssuanceRequest.IssuanceRequestExt.EmptyVersion()
        );
        var op = new CreateIssuanceRequestOp(
                issuanceRequest,
                Base64.toBase64String(Random.randomGen().toByteArray()),
                0,
                new CreateIssuanceRequestOp.CreateIssuanceRequestOpExt.EmptyVersion()
        );
        Transaction tx = new TransactionBuilder(
                api.getGeneral().getSystemInfo().execute().get().toNetworkParams(),
                defaultSignerRole.getAccountId()
        )
                .addOperation(new Operation.OperationBody.CreateIssuanceRequest(op))
                .build();
        tx.addSignature(defaultSignerRole);
        v3api.getTransactions().submit(tx, true).execute().get();
    }

    public static void payWin(String offerId, PedersenCommitment.Witness witness) throws UnirestException {

        JSONObject jsonCommitment = new JSONObject(getCommitment(witness.getContainerId()))
                .getJSONObject("commitment");
        PedersenCommitment.Commitment commitment =
                new PedersenCommitment.Commitment(new ECPoint(jsonCommitment.getBigInteger("x"),jsonCommitment.getBigInteger("y")));

        if (!PedersenCommitment.verify(commitment,witness)) {
            return;
        } else {
            var prizeFund = Double.valueOf(getPrizeFund());
            var teamSum = Double.valueOf(String.valueOf(marketplaceApi.getOffer(offerId).execute().get().getBaseAmount()));
            if (teamSum == 0) {
                return;
            }
            var sellerId = "GCKPXLM2FJQOG44MMXQIJVOIJQDFHHQZMM2ODYZVVDPA5EXIA6NPFJUW";
            var teamCoeff = (prizeFund/teamSum) + ((prizeFund/teamSum) * 0.1);
            System.out.println(teamCoeff);
            Unirest.setTimeouts(0, 0);
            HttpResponse<String> response = Unirest.get(
                    prop.getProperty("tokend_base_url") + "integrations/marketplace/buy_requests?include=request_details%2Crequest_details.quote_asset&filter[seller]="
                            + sellerId + "&filter[offer]=" + offerId + "&filter[status]=paid")
                    .header("signature", "keyId=\"GCKPXLM2FJQOG44MMXQIJVOIJQDFHHQZMM2ODYZVVDPA5EXIA6NPFJUW\",algorithm=\"ed25519-sha256\",headers=\"(request-target)\",signature=\"MpAqxDWWlHSBOWNOWVO9hfECloXYwYy/+N8W7n4MWN8xOZgBwf9KLUafbqAwQD+WNS1v2p4oGOOPIXExpCC4Dw==\"")
                    .asString();
            JSONObject jsonObject = new JSONObject(response.getBody());
            var iterator = jsonObject.getJSONArray("data").iterator();
            while (iterator.hasNext()) {
                var json = new JSONObject(iterator.next().toString()).getJSONObject("attributes");
                var userWin = (json.getDouble("amount") * teamCoeff) - json.getDouble("amount");
                var user = json.getString("sender_account_id");
                if (witness.getAccountId().equals(user)) {
                    issue(userWin, user);
                }
                log.log(Level.INFO, "Issued " + userWin + " to " + user);
            }
        }
    }

    public static String getPrizeFund() {
        return String.valueOf(v3api.getAssets().getById("UAH", null).execute().get().getIssued().doubleValue());
    }
}
