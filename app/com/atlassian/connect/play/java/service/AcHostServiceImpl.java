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
import play.libs.ws.WSResponse;

import java.util.List;

import static com.atlassian.fugue.Option.none;
import static org.apache.commons.lang.StringUtils.stripToNull;
import static play.libs.F.Function;
import static play.libs.F.Promise;
import static play.mvc.Http.Status.OK;

public class AcHostServiceImpl implements AcHostService {
    private final AcHostRepository acHostRepository;

    public AcHostServiceImpl(AcHostRepository acHostRepository) {
        this.acHostRepository = acHostRepository;
    }

    public AcHostServiceImpl() {
        this(new DefaultAcHostRepository());
    }

    @Override
    public Promise<Void> registerHost(final AcHost acHost) {
        return registerHost(acHost.getKey(), acHost.getBaseUrl(), acHost.getPublicKey(), acHost.getSharedSecret(),
                acHost.getName());
    }

    // TODO: Now that we have removed the public key check this function no longer needs to be async.
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
        }).getOrElse(new AcHost());

        acHost.setKey(clientKey);
        acHost.setBaseUrl(baseUrl);
        acHost.setPublicKey(publicKey);
        acHost.setSharedSecret(sharedSecret);
        acHost.setName(name);

        try {
            acHostRepository.save(acHost);
        } catch (Throwable throwable) {
            return Promise.throwing(throwable);
        }

        return Promise.pure(null);
    }

    @Override
    public Option<AcHost> findByKey(String consumerKey) throws Throwable {
        return acHostRepository.findByKey(consumerKey);
    }

    @Override
    public List<AcHost> all() throws Throwable {
        return acHostRepository.all();
    }

}
