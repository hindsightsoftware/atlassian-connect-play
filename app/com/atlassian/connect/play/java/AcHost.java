package com.atlassian.connect.play.java;

import com.google.common.annotations.VisibleForTesting;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import static play.data.validation.Constraints.MaxLength;
import static play.data.validation.Constraints.Required;

/**
 * This represents the host application of the remote app plugin
 */
@Entity
@Table (name = "ac_host")
@NamedQueries ({
        @NamedQuery (name = "AcHost.findAll", query = "SELECT a FROM AcHost a"),
        @NamedQuery (name = "AcHost.findByKey", query = "SELECT a FROM AcHost a where a.key = :key"),
        @NamedQuery (name = "AcHost.findByUrl", query = "SELECT a FROM AcHost a where a.baseUrl = :baseUrl")
})
public final class AcHost
{
    @VisibleForTesting
    public static final String CONSUMER_INFO_URL = "/plugins/servlet/oauth/consumer-info";

    @Id
    @SequenceGenerator (name = "ac_host_gen", sequenceName = "ac_host_seq")
    @GeneratedValue (generator = "ac_host_gen")
    private Long id;

    @Required
    @Column (unique = true, nullable = false)
    private String key;

    @MaxLength (512)
    @Column (length = 512)
    public String publicKey;

    @MaxLength (512)
    @Column (length = 512) // TODO: may have to be nullable at least as we transition from oauth
    public String sharedSecret;

    @Required
    @MaxLength (512)
    @Column (unique = true, nullable = false, length = 512)
    private String baseUrl;

    private String name;
    private String description;

    public Long getId()
    {
        return id;
    }

    public String getKey()
    {
        return key;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public String getPublicKey()
    {
        return publicKey;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public String getConsumerInfoUrl()
    {
        return baseUrl + CONSUMER_INFO_URL;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setSharedSecret(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setName(String name) {
        this.name = name;
    }
}
