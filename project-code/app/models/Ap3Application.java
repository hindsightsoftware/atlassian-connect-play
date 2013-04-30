package models;

import com.atlassian.fugue.Option;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import static com.atlassian.fugue.Option.option;
import static play.data.validation.Constraints.MaxLength;
import static play.data.validation.Constraints.Required;

/**
 * This represents the host application of the remote app plugin
 */
@Entity
@Table(name = "ap3_application")
public final class Ap3Application extends Model
{
    @Id
    public Long id;

    @Required
    @Column(unique = true, nullable = false)
    public String key;

    @Required
    @MaxLength(512)
    @Column(nullable = false, length = 512)
    public String publicKey;

    @Required
    @MaxLength(512)
    @Column(unique = true, nullable = false, length = 512)
    public String baseUrl;

    public String name;
    public String description;

    public String getConsumerInfoUrl()
    {
        return baseUrl + "/plugins/servlet/oauth/consumer-info";
    }

    public static void create(Ap3Application c)
    {
        c.save();
    }

    public static Option<Ap3Application> findByKey(String key)
    {
        return option(find.where().eq("key", key).findUnique());
    }

    public static void delete(Long id)
    {
        find.ref(id).delete();
    }

    public static Option<Ap3Application> findByUrl(String baseUrl)
    {
        return option(find.where().eq("baseUrl", baseUrl).findUnique());
    }

    private static Finder<Long, Ap3Application> find = new Finder<Long, Ap3Application>(Long.class, Ap3Application.class);
}
