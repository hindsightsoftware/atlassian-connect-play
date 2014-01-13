package com.atlassian.connect.play.java.service;

import com.atlassian.connect.play.java.AcHost;
import com.atlassian.connect.play.java.auth.InvalidAuthenticationRequestException;
import com.atlassian.connect.play.java.auth.MismatchPublicKeyException;
import com.atlassian.connect.play.java.auth.PublicKeyVerificationFailureException;
import com.atlassian.connect.play.java.model.AcHostModel;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import static org.apache.commons.lang.StringUtils.stripToNull;
import static play.libs.F.Function;
import static play.libs.F.Promise;
import static play.libs.WS.Response;
import static play.mvc.Http.Status.OK;

public class AcHostServiceImpl implements AcHostService {
    @VisibleForTesting
    static final String CONSUMER_INFO_URL = "/plugins/servlet/oauth/consumer-info";

    private static final String PUBLIC_KEY_ELEMENT_NAME = "publicKey";
    private final AcHostHttpClient httpClient;
    private final AcHostRepository acHostRepository;

    public AcHostServiceImpl(AcHostHttpClient httpClient, AcHostRepository acHostRepository) {
        this.httpClient = httpClient;
        this.acHostRepository = acHostRepository;
    }

    public AcHostServiceImpl(AcHostHttpClient httpClient) {
        this(httpClient, new DefaultAcHostRepository());
    }

    @Override
    public Promise<String> fetchPublicKeyFromRemoteHost(AcHost acHost) {
        Promise<Response> responsePromise = httpClient.url(CONSUMER_INFO_URL, acHost, false).get();

        Promise<String> publicKeyPromise = responsePromise.map(new Function<Response, String>() {
            @Override
            public String apply(Response response) throws Throwable {
                if (response.getStatus() == OK) {
                    Document consumerInfoDoc = response.asXml();
                    NodeList publicKeyElements = consumerInfoDoc.getElementsByTagName(PUBLIC_KEY_ELEMENT_NAME);
                    if (publicKeyElements.getLength() == 1) {
                        return publicKeyElements.item(0).getTextContent();
                    }
                }
                throw new PublicKeyVerificationFailureException("Failed to fetch public key for verification. Response status: " + response.getStatus());
            }
        });

        return publicKeyPromise;
    }

    @Override
    public Promise<Boolean> registerHost(final AcHost acHost) {
        if (stripToNull(acHost.getPublicKey()) == null) {
            throw new InvalidAuthenticationRequestException("No public key provided in registration request");
        }
        Promise<Boolean> hostRegistered = fetchPublicKeyFromRemoteHost(acHost).map(new Function<String, Boolean>() {
            @Override
            public Boolean apply(String fetchedPublicKey) throws Throwable {
                fetchedPublicKey = stripToNull(fetchedPublicKey);
                String providedPublicKey = stripToNull(acHost.getPublicKey());
                boolean keysMatch = Objects.equal(fetchedPublicKey, providedPublicKey);
                if (!keysMatch) {
                    throw new MismatchPublicKeyException(providedPublicKey, fetchedPublicKey);
                }
                acHostRepository.save(acHost);
                return true;
            }
        });

        return hostRegistered;
    }

}
