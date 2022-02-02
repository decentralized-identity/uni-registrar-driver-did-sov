package uniregistrar.driver.did.sov.mode.internal;

import com.danubetech.keyformats.PrivateKey_to_JWK;
import com.danubetech.keyformats.jose.JWK;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DIDDocument;
import foundation.identity.did.Service;
import io.leonard.Base58;
import org.abstractj.kalium.NaCl;
import org.apache.commons.lang3.RandomStringUtils;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidJSONParameters;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.RegistrationException;
import uniregistrar.driver.did.sov.libindy.IndyConnection;
import uniregistrar.driver.did.sov.util.Ed25519Util;
import uniregistrar.driver.did.sov.util.Taa;
import uniregistrar.state.CreateState;
import uniregistrar.state.SetStateFinished;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Create {

    private static final Logger log = LoggerFactory.getLogger(Create.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static CreateState create(IndyConnection indyConnection, String seed, DIDDocument didDocument) throws RegistrationException {

        // create USER SEED

        String newSeed = seed != null ? seed : RandomStringUtils.randomAlphanumeric(32);

        // create

        String indyDid;
        String indyVerkey;

        try {

            synchronized(indyConnection) {

                Pool.setProtocolVersion(indyConnection.getPoolVersion());

                // create USER DID

                Wallet walletUser = indyConnection.getWallet();

                if (log.isDebugEnabled()) log.debug("=== CREATE AND STORE DID ===");
                DidJSONParameters.CreateAndStoreMyDidJSONParameter createAndStoreMyDidJSONParameter = new DidJSONParameters.CreateAndStoreMyDidJSONParameter(null, newSeed, null, null);
                if (log.isDebugEnabled()) log.debug("CreateAndStoreMyDidJSONParameter: " + createAndStoreMyDidJSONParameter);
                DidResults.CreateAndStoreMyDidResult createAndStoreMyDidResult = Did.createAndStoreMyDid(walletUser, createAndStoreMyDidJSONParameter.toJson()).get();
                if (log.isDebugEnabled()) log.debug("CreateAndStoreMyDidResult: " + createAndStoreMyDidResult);

                indyDid = createAndStoreMyDidResult.getDid();
                indyVerkey = createAndStoreMyDidResult.getVerkey();

                // create NYM request

                if (log.isDebugEnabled()) log.debug("=== CREATE NYM REQUEST ===");
                String nymRequest = Ledger.buildNymRequest(indyConnection.getSubmitterDid(), indyDid, indyVerkey, /*"{\"alias\":\"b\"}"*/ null, null).get();
                if (log.isDebugEnabled()) log.debug("nymRequest: " + nymRequest);

                // agree

                if (indyConnection.getTaa() != null) {
                    nymRequest = Taa.agree(nymRequest, indyConnection.getTaa());
                    if (log.isDebugEnabled()) log.debug("agreed nymRequest: " + nymRequest);
                }

                // sign request

                if (log.isDebugEnabled()) log.debug("=== SIGN 1 ===");
                String signRequestResult1 = Ledger.signRequest(indyConnection.getWallet(), indyConnection.getSubmitterDid(), nymRequest).get();
                if (log.isDebugEnabled()) log.debug("signRequestResult1: " + signRequestResult1);

                // submit request to ledger

                if (log.isDebugEnabled()) log.debug("=== SUBMIT 1 ===");
                String submitRequestResult1 = Ledger.submitRequest(indyConnection.getPool(), signRequestResult1).get();
                if (log.isDebugEnabled()) log.debug("submitRequestResult1: " + submitRequestResult1);

                // service endpoints

                if (didDocument != null) {

                    if (didDocument.getServices() != null) {

                        Map<String, Object> jsonObject = new HashMap<String, Object>();
                        Map<String, Object> endpointJsonObject = new HashMap<String, Object> ();

                        for (Service service : didDocument.getServices()) {

                            endpointJsonObject.put(service.getType(), service.getServiceEndpoint());
                        }

                        jsonObject.put("endpoint", endpointJsonObject);

                        String jsonObjectString;

                        try {

                            StringWriter stringWriter = new StringWriter();
                            new ObjectMapper().writeValue(stringWriter, jsonObject);
                            jsonObjectString = stringWriter.toString();
                        } catch (IOException ex) {

                            throw new RegistrationException("Invalid endpoints: " + endpointJsonObject);
                        }

                        if (log.isDebugEnabled()) log.debug("Raw: " + jsonObjectString);

                        // create ATTRIB request

                        if (log.isDebugEnabled()) log.debug("=== CREATE ATTRIB REQUEST ===");
                        String attribRequest = Ledger.buildAttribRequest(indyDid, indyDid, null, jsonObjectString, null).get();
                        if (log.isDebugEnabled()) log.debug("attribRequest: " + attribRequest);

                        // agree

                        if (indyConnection.getTaa() != null) {
                            attribRequest = Taa.agree(attribRequest, indyConnection.getTaa());
                            if (log.isDebugEnabled()) log.debug("agreed attribRequest: " + attribRequest);
                        }

                        // sign request

                        if (log.isDebugEnabled()) log.debug("=== SIGN 2 ===");
                        String signRequestResult2 = Ledger.signRequest(walletUser, indyDid, attribRequest).get();
                        if (log.isDebugEnabled()) log.debug("signRequestResult2: " + signRequestResult2);

                        // submit request to ledger

                        if (log.isDebugEnabled()) log.debug("=== SUBMIT 2 ===");
                        String submitRequestResult2 = Ledger.submitRequest(indyConnection.getPool(), signRequestResult2).get();
                        if (log.isDebugEnabled()) log.debug("submitRequestResult2: " + submitRequestResult2);
                    }
                }
            }
        } catch (InterruptedException | ExecutionException | IndyException ex) {

            throw new RegistrationException("Problem connecting to Indy: " + ex.getMessage(), ex);
        }

        // REGISTRATION STATE FINISHED: DID

        String did = "did:sov:" + indyConnection.getDidNetworkPrefix() + indyDid;

        // REGISTRATION STATE FINISHED: SECRET

        byte[] publicKeyBytesBuffer = new byte[NaCl.Sodium.CRYPTO_SIGN_ED25519_PUBLICKEYBYTES];
        byte[] privateKeyBytesBuffer = new byte[NaCl.Sodium.CRYPTO_SIGN_ED25519_SECRETKEYBYTES];
        Ed25519Util.generateKeypair(publicKeyBytesBuffer, privateKeyBytesBuffer, newSeed.getBytes());
        byte[] publicKeyBytes = publicKeyBytesBuffer;
        byte[] privateKeyBytes = privateKeyBytesBuffer;
        byte[] didBytes = Arrays.copyOf(publicKeyBytes, 16);
        String base58EncodedPublicKey = Base58.encode(didBytes);
        String keyUrl = identifierToKeyUrl(did);
        JWK jsonWebKey = privateKeyToJWK(privateKeyBytes, publicKeyBytes, keyUrl);

        if (! base58EncodedPublicKey.equals(indyDid)) throw new RegistrationException("Encoded public key does not match created DID: " + base58EncodedPublicKey + " != " + indyDid);

        List<Map<String, Object>> jsonKeys = new ArrayList<>();
        jsonKeys.add(jsonWebKey.toMap());

        Map<String, Object> secret = new LinkedHashMap<>();
        secret.put("seed", newSeed);
        secret.put("keys", jsonKeys);

        // REGISTRATION STATE FINISHED: DID DOCUMENT METADATA

        Map<String, Object> didDocumentMetadata = new LinkedHashMap<>();
        didDocumentMetadata.put("network", indyConnection.getPoolConfigName());
        didDocumentMetadata.put("poolVersion", indyConnection.getPoolVersion());
        didDocumentMetadata.put("submitterDid", indyConnection.getSubmitterDid());

        // done

        CreateState createState = CreateState.build();
        SetStateFinished.setStateFinished(createState, did, secret);
        createState.setDidDocumentMetadata(didDocumentMetadata);

        return createState;
    }

    /*
     * Helper methods
     */

    private static JWK privateKeyToJWK(byte[] privateKeyBytes, byte[] publicKeyBytes, String keyUrl) {
        String kid = keyUrl;
        String use = null;
        return PrivateKey_to_JWK.Ed25519PrivateKeyBytes_to_JWK(privateKeyBytes, publicKeyBytes, kid, use);
    }

    private static String identifierToKeyUrl(String identifier) {
        return identifier + "#key-1";
    }
}
