package com.atlassian.connect.play.java.service;

import com.atlassian.connect.play.java.AcHost;
import com.atlassian.connect.play.java.auth.InvalidAuthenticationRequestException;
import com.atlassian.connect.play.java.auth.MismatchPublicKeyException;
import com.atlassian.connect.play.java.auth.PublicKeyVerificationFailureException;
import com.atlassian.fugue.Option;
import com.google.common.base.Objects;
import com.google.common.base.Supplier;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.List;

import static com.atlassian.fugue.Option.none;
import static org.apache.commons.lang.StringUtils.stripToNull;
import static play.libs.F.Function;
import static play.libs.F.Promise;
import static play.libs.WS.Response;
import static play.mvc.Http.Status.OK;

public class AcHostServiceImpl implements AcHostService {
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
    public Promise<Void> registerHost(final String clientKey, final String baseUrl, String publicKey, String sharedSecret, String name) {
        // TODO: The consequence of this is that we will overwrite registrations each time. Is that what we want?
        Option<AcHost> acHostOption;
        try {
            acHostOption = acHostRepository.findByKey(clientKey);
        } catch (Throwable throwable) {
            return Promise.throwing(throwable);
        }
        final AcHost acHost = acHostOption.orElse(new Supplier<Option<AcHost>>() {
            @Override
            public Option<AcHost> get() {
                try {
                    return acHostRepository.findByUrl(baseUrl);
                } catch (Throwable e) {
                    return none(AcHost.class);
                }
            }
        }).getOrElse(acHostRepository.create());

        acHost.setKey(clientKey);
        acHost.setBaseUrl(baseUrl);
        acHost.setPublicKey(publicKey);
        acHost.setSharedSecret(sharedSecret);
        acHost.setName(name);

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
    public Option<AcHost> findByKey(String consumerKey) throws Throwable {
        return acHostRepository.findByKey(consumerKey);
    }

    @Override
    public List<? extends AcHost> all() throws Throwable{
        return acHostRepository.all();
    }

}
