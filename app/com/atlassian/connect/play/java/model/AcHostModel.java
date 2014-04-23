package com.atlassian.connect.play.java.model;

import com.atlassian.connect.play.java.AcHost;
import com.atlassian.fugue.Option;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;
import play.db.jpa.JPA;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;
import static play.data.validation.Constraints.MaxLength;
import static play.data.validation.Constraints.Required;

/**
 * This represents the host application of the remote app plugin
 */
@Entity
@Table (name = "ac_host")
@NamedQueries ({
        @NamedQuery (name = "AcHostModel.findAll", query = "SELECT a FROM AcHostModel a"),
        @NamedQuery (name = "AcHostModel.findByKey", query = "SELECT a FROM AcHostModel a where a.key = :key"),
        @NamedQuery (name = "AcHostModel.findByUrl", query = "SELECT a FROM AcHostModel a where a.baseUrl = :baseUrl")
})
public final class AcHostModel implements AcHost
{
    @VisibleForTesting
    public static final String CONSUMER_INFO_URL = "/plugins/servlet/oauth/consumer-info";

    @Id
    @SequenceGenerator (name = "ac_host_gen", sequenceName = "ac_host_seq")
    @GeneratedValue (generator = "ac_host_gen")
    public Long id;

    @Required
    @Column (unique = true, nullable = false)
    public String key;

    @Required
    @MaxLength (512)
    @Column (nullable = false, length = 512)
    public String publicKey;

    @Required
    @MaxLength (512)
    @Column (nullable = false, length = 512) // TODO: may have to be nullable at least as we transition from oauth
    public String sharedSecret;

    @Required
    @MaxLength (512)
    @Column (unique = true, nullable = false, length = 512)
    public String baseUrl;

    public String name;
    public String description;

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

    @Override
    public String getBaseUrl()
    {
        return baseUrl;
    }

    @Override
    public String getPublicKey()
    {
        return publicKey;
    }

    @Override
    public String getSharedSecret() {
        return sharedSecret;
    }

    @Override
    public String getConsumerInfoUrl()
    {
        return baseUrl + CONSUMER_INFO_URL;
    }

}
