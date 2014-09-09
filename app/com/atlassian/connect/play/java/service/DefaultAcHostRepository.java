package com.atlassian.connect.play.java.service;

import com.atlassian.connect.play.java.AcHost;
import com.atlassian.fugue.Option;
import play.db.jpa.JPA;
import play.libs.F;

import java.util.List;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;

public class DefaultAcHostRepository implements AcHostRepository {

    private static final String BASE_URL = "baseUrl";
    public static final String DEFAULT_PERSISTENCE_UNIT = "default";

    @Override
    public void save(final AcHost acHostModel) throws Throwable {

        // TODO: Not sure how to do this with annotations in play. Currently this is not a container managed repo
        JPA.withTransaction(new F.Function0<Void>() {
            @Override
            public Void apply() throws Throwable {
                if (acHostModel.getId() != null) {
                    JPA.em().merge(acHostModel);
                }
                else {
                    JPA.em().persist(acHostModel);
                }
                return null;
            }
        });
    }

    @Override
    public List<AcHost> all() throws Throwable {
        return JPA.withTransaction(DEFAULT_PERSISTENCE_UNIT, true, new F.Function0<List<AcHost>>() {
            @Override
            public List<AcHost> apply() throws Throwable {
                return JPA.em().createNamedQuery("AcHost.findAll", AcHost.class).getResultList();
            }
        });
    }

    @Override
    public Option<AcHost> findByKey(final String key) throws Throwable {
        return JPA.withTransaction(DEFAULT_PERSISTENCE_UNIT, true, new F.Function0<Option<AcHost>>()
        {
            @Override
            public Option<AcHost> apply() throws Throwable
            {
                final List<AcHost> resultList = JPA.em().createNamedQuery("AcHost.findByKey", AcHost.class).
                        setParameter("key", key).
                        getResultList();
                return resultList.isEmpty() ? none(AcHost.class) : option(resultList.get(0));
            }
        });
    }

    @Override
    public Option<AcHost> findByUrl(final String baseUrl) throws Throwable {
        return JPA.withTransaction(DEFAULT_PERSISTENCE_UNIT, true, new F.Function0<Option<AcHost>>()
        {
            @Override
            public Option<AcHost> apply() throws Throwable
            {
                final List<AcHost> resultList = JPA.em().createNamedQuery("AcHost.findByUrl", AcHost.class).
                        setParameter(BASE_URL, baseUrl).
                        getResultList();
                return resultList.isEmpty() ? none(AcHost.class) : option(resultList.get(0));
            }
        });

    }

    @Override
    public void delete(final Long id) throws Throwable {
        JPA.withTransaction(new F.Function0<Void>() {
            @Override
            public Void apply() throws Throwable {
                final AcHost acHost = JPA.em().find(AcHost.class, id);

                if (acHost != null)
                {
                    JPA.em().remove(acHost);
                }
                return null;
            }
        });
    }

}
