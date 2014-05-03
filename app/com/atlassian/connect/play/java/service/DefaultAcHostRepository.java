package com.atlassian.connect.play.java.service;

import com.atlassian.connect.play.java.AcHost;
import com.atlassian.connect.play.java.model.AcHostModel;
import com.atlassian.fugue.Option;
import play.db.jpa.JPA;
import play.libs.F;

import java.util.List;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;

public class DefaultAcHostRepository implements AcHostRepository {

    private static final String BASE_URL = "baseUrl";

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
    public List<? extends AcHost> all() throws Throwable {
        return JPA.withTransaction("findAll", true, new F.Function0<List<? extends AcHost>>() {
            @Override
            public List<? extends AcHost> apply() throws Throwable {
                return JPA.em().createNamedQuery("AcHostModel.findAll", AcHostModel.class).getResultList();
            }
        });
    }

    @Override
    public Option<AcHost> findByKey(final String key) throws Throwable {
        return JPA.withTransaction("findByKey", true, new F.Function0<Option<AcHost>>()
        {
            @Override
            public Option<AcHost> apply() throws Throwable
            {
                final List<AcHostModel> resultList = JPA.em().createNamedQuery("AcHostModel.findByKey", AcHostModel.class).
                        setParameter("key", key).
                        getResultList();
                return resultList.isEmpty() ? none(AcHost.class) : option((AcHost)resultList.get(0));
            }
        });
    }

    @Override
    public Option<AcHost> findByUrl(final String baseUrl) throws Throwable {
        return JPA.withTransaction("findByUrl", true, new F.Function0<Option<AcHost>>()
        {
            @Override
            public Option<AcHost> apply() throws Throwable
            {
                final List<AcHostModel> resultList = JPA.em().createNamedQuery("AcHostModel.findByUrl", AcHostModel.class).
                        setParameter(BASE_URL, baseUrl).
                        getResultList();
                return resultList.isEmpty() ? none(AcHost.class) : option((AcHost)resultList.get(0));
            }
        });

    }

    @Override
    public void delete(final Long id) throws Throwable {
        JPA.withTransaction(new F.Function0<Void>() {
            @Override
            public Void apply() throws Throwable {
                final AcHostModel acHostModel = JPA.em().find(AcHostModel.class, id);

                if (acHostModel != null)
                {
                    JPA.em().remove(acHostModel);
                }
                return null;
            }
        });
    }

    @Override
    public AcHost create() {
        return new AcHostModel();
    }

}
