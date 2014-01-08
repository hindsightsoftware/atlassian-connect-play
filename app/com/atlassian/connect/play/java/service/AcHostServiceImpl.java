package com.atlassian.connect.play.java.service;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.AcHost;
import com.atlassian.connect.play.java.model.AcHostModel;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import play.libs.F;
import play.libs.WS;
import play.mvc.Http;
import play.mvc.Results;

import static play.libs.F.Function;
import static play.libs.F.Promise;
import static play.libs.WS.Response;
import static play.libs.WS.WSRequestHolder;
import static play.mvc.Http.Status.OK;

public class AcHostServiceImpl implements AcHostService {
    @VisibleForTesting
    static final String CONSUMER_INFO_URL = "/plugins/servlet/oauth/consumer-info";

    private static final String PUBLIC_KEY_ELEMENT_NAME = "publicKey";
    private final AcHostHttpClient httpClient;

    public AcHostServiceImpl(AcHostHttpClient httpClient) {

        this.httpClient = httpClient;
    }

    @Override
    public Promise<String> fetchPublicKeyFromRemoteHost(AcHost acHost) {
        Promise<Response> responsePromise = httpClient.url(CONSUMER_INFO_URL, acHost).get();

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
                return null; // TODO: The play doco is so appalling that I'm not sure whether I should throw an exception to indicate failure or what
            }
        });

        return publicKeyPromise;
    }

    @Override
    public Promise<Boolean> registerHost(final AcHost acHost) {
        // TODO: validate host model against public key. This is not mentioned in https://developer.atlassian.com/static/connect/docs/pages/concepts/authentication.html
        // first check public key is correct by checking against <host baseUrl>/plugins/servlet/oauth/consumer-info
        // What other checks? Are there any holes here?
        Promise<Boolean> hostRegistered = fetchPublicKeyFromRemoteHost(acHost).map(new Function<String, Boolean>() {
            @Override
            public Boolean apply(String fetchedPublicKey) throws Throwable {
                boolean keysMatch = Objects.equal(fetchedPublicKey, acHost.getPublicKey());
                if (!keysMatch) {
                    // TODO: log
                    return false; // TODO: or throw?
                }
                AcHostModel.create((AcHostModel) acHost); // TODO: Dodgy cast
                return true;
            }
        });

        return hostRegistered;
    }

}
