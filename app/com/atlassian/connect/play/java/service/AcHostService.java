package com.atlassian.connect.play.java.service;

import com.atlassian.connect.play.java.AcHost;
import com.atlassian.connect.play.java.model.AcHostModel;
import com.atlassian.fugue.Option;

import java.util.List;

import static play.libs.F.Promise;

/**
 * A service for activities related to an Atlassian application host
 */
public interface AcHostService {

    /**
     * Retrieves the public key from the remote host (via a REST url)
     *
     *
     * @param acHost
     * @return
     */
    Promise<String> fetchPublicKeyFromRemoteHost(AcHost acHost);

    /**
     * Registers a new host, or updates the details from an existing host
     *
     *
     *
     *
     *
     * @param clientKey
     * @param  baseUrl
     * @param publicKey
     * @param sharedSecret
     * @param name
     * @return
     */
    Promise<Void> registerHost(String clientKey, String baseUrl, String publicKey, String sharedSecret, String name);

    Option<? extends AcHost> findByKey(String consumerKey);

    List<AcHostModel> all();

}
