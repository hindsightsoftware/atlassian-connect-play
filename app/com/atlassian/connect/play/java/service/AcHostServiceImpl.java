package com.atlassian.connect.play.java.service;

import com.atlassian.connect.play.java.AcHost;
import com.atlassian.connect.play.java.auth.InvalidAuthenticationRequestException;
import com.atlassian.connect.play.java.auth.MismatchPublicKeyException;
import com.atlassian.connect.play.java.auth.PublicKeyVerificationFailureException;
import com.atlassian.connect.play.java.model.AcHostModel;
import com.atlassian.fugue.Option;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Supplier;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.List;

import static org.apache.commons.lang.StringUtils.stripToNull;
import static play.libs.F.Function;
import static play.libs.F.Promise;
import static play.libs.WS.Response;
import static play.mvc.Http.Status.OK;

public class AcHostServiceImpl implements AcHostService {
    private static final String CLIENT_KEY = "clientKey";
    private static final String BASE_URL = "baseUrl";
    private static final String SHARED_SECRET = "sharedSecret";
    private static final String PRODUCT_TYPE = "productType";
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
        Promise<Response> responsePromise = httpClient.url(acHost.getConsumerInfoUrl(), acHost, false).get();

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
    public Promise<Void> registerHost(final AcHost acHost) {
        if (stripToNull(acHost.getPublicKey()) == null) {
            throw new InvalidAuthenticationRequestException("No public key provided in registration request");
        }
        Promise<Void> hostRegistered = fetchPublicKeyFromRemoteHost(acHost).map(new Function<String, Void>() {
            @Override
            public Void apply(String fetchedPublicKey) throws Throwable {
                fetchedPublicKey = stripToNull(fetchedPublicKey);
                String providedPublicKey = stripToNull(acHost.getPublicKey());
                boolean keysMatch = Objects.equal(fetchedPublicKey, providedPublicKey);
                if (!keysMatch) {
                    throw new MismatchPublicKeyException(providedPublicKey, fetchedPublicKey);
                }
                acHostRepository.save(acHost);
                return null;
            }
        });

        return hostRegistered;
    }

    @Override
    public AcHostModel fromJson(final JsonNode json) {
        // TODO: The consequence of this is that we will overwrite registrations each time. Is that what we want?
        // TODO: don't like the looking up in the middle of the json unmarshalling. Pull out somewhere else
        final AcHostModel acHost = acHostRepository.findByKey(getAttributeAsText(json, CLIENT_KEY))
                .orElse(new Supplier<Option<AcHostModel>>() {
                    @Override
                    public Option<AcHostModel> get() {
                        return acHostRepository.findByUrl(getAttributeAsText(json, BASE_URL));
                    }
                })
                .getOrElse(new AcHostModel());

        return fromJson(json, acHost);
    }

    @VisibleForTesting
    static AcHostModel fromJson(JsonNode json, AcHostModel acHost) {
//        // TODO check the key is the same as this app's
//        getAttributeAsText(json, "key");

        acHost.setKey(getAttributeAsText(json, CLIENT_KEY));
        acHost.setBaseUrl(getAttributeAsText(json, BASE_URL));
        acHost.setPublicKey(getAttributeAsText(json, PUBLIC_KEY_ELEMENT_NAME));
        acHost.setSharedSecret(getAttributeAsText(json, SHARED_SECRET));
        acHost.setName(getAttributeAsText(json, PRODUCT_TYPE));
//        acHost.description = getAttributeAsText(json, "description");
        return acHost;
    }

    private static String getAttributeAsText(JsonNode json, String name) {
        JsonNode jsonNode = json.get(name);
        return jsonNode == null ? null : jsonNode.textValue();
    }

    @Override
    public Option<AcHostModel> findByUrl(String baseUrl) {
        return acHostRepository.findByUrl(baseUrl);
    }

    @Override
    public Option<? extends AcHost> findByKey(String consumerKey) {
        return acHostRepository.findByKey(consumerKey);
    }

    @Override
    public List<AcHostModel> all() {
        return acHostRepository.all();
    }

}
