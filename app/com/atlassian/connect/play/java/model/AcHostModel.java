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

    private static final String CLIENT_KEY = "clientKey";
    private static final String BASE_URL = "baseUrl";
    private static final String PUBLIC_KEY = "publicKey";
    private static final String SHARED_SECRET = "sharedSecret";
    private static final String PRODUCT_TYPE = "productType";
    @Id
    @SequenceGenerator (name = "ac_host_gen", sequenceName = "ac_host_seq")
    @GeneratedValue (generator = "ac_host_gen")
    public Long id;

    @Required
    @Column (unique = true, nullable = false)
    public String key;

    @MaxLength (512)
    @Column (length = 512)
    public String publicKey;

    @MaxLength (512)
    @Column (length = 512) // TODO: may have to be nullable at least as we transition from oauth
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

    public static List<AcHostModel> all()
    {
        return JPA.em().createNamedQuery("AcHostModel.findAll", AcHostModel.class).getResultList();
    }

    public static Option<AcHostModel> findByKey(String key)
    {
        final List<AcHostModel> resultList = JPA.em().createNamedQuery("AcHostModel.findByKey", AcHostModel.class).
                setParameter("key", key).
                getResultList();
        return resultList.isEmpty() ? none(AcHostModel.class) : option(resultList.get(0));
    }

    public static Option<AcHostModel> findByUrl(String baseUrl)
    {
        final List<AcHostModel> resultList = JPA.em().createNamedQuery("AcHostModel.findByUrl", AcHostModel.class).
                setParameter(BASE_URL, baseUrl).
                getResultList();
        return resultList.isEmpty() ? none(AcHostModel.class) : option(resultList.get(0));
    }

    public static void create(AcHostModel hostModel)
    {
        JPA.em().persist(hostModel);
    }

    public static void delete(Long id)
    {
        final AcHostModel acHostModel = JPA.em().find(AcHostModel.class, id);

        if (acHostModel != null)
        {
            JPA.em().remove(acHostModel);
        }
    }

    public static AcHostModel fromAcHost(AcHost acHost) {
        if (acHost instanceof AcHostModel) {
            return (AcHostModel) acHost;
        }

        throw new IllegalStateException("Not implemented yet");
    }

    public static AcHostModel fromJson(final JsonNode json) {
        // TODO: The consequence of this is that we will overwrite registrations each time. Is that what we want?
        // TODO: don't like the looking up in the middle of the json unmarshalling. Pull out somewhere else
        final AcHostModel acHost = AcHostModel.findByKey(getAttributeAsText(json, CLIENT_KEY))
                .orElse(new Supplier<Option<AcHostModel>>() {
                    @Override
                    public Option<AcHostModel> get() {
                        return AcHostModel.findByUrl(getAttributeAsText(json, BASE_URL));
                    }
                })
                .getOrElse(new AcHostModel());

        return fromJson(json, acHost);
    }

    @VisibleForTesting
    static AcHostModel fromJson(JsonNode json, AcHostModel acHost) {
//        // TODO check the key is the same as this app's
//        getAttributeAsText(json, "key");

        acHost.key = getAttributeAsText(json, CLIENT_KEY);
        acHost.baseUrl = getAttributeAsText(json, BASE_URL);
        acHost.publicKey = getAttributeAsText(json, PUBLIC_KEY);
        acHost.sharedSecret = getAttributeAsText(json, SHARED_SECRET);
        acHost.name = getAttributeAsText(json, PRODUCT_TYPE);
//        acHost.description = getAttributeAsText(json, "description");
        return acHost;
    }

    private static String getAttributeAsText(JsonNode json, String name) {
        JsonNode jsonNode = json.get(name);
        return jsonNode == null ? null : jsonNode.textValue();
    }

}
