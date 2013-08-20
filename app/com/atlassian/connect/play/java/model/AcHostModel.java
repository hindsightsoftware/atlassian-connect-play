package com.atlassian.connect.play.java.model;

import com.atlassian.connect.play.java.AcHost;
import com.atlassian.fugue.Option;
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
    @Column (unique = true, nullable = false, length = 512)
    public String baseUrl;

    public String name;
    public String description;

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

    public String getConsumerInfoUrl()
    {
        return baseUrl + "/plugins/servlet/oauth/consumer-info";
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
                setParameter("baseUrl", baseUrl).
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
}
