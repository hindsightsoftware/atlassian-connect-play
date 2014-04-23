package com.atlassian.connect.play.java.service;

import com.atlassian.connect.play.java.AcHost;
import com.atlassian.connect.play.java.model.AcHostModel;
import com.atlassian.fugue.Option;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;
import play.db.jpa.JPA;
import play.libs.F;

import java.util.List;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;

public class DefaultAcHostRepository implements AcHostRepository {

    private static final String BASE_URL = "baseUrl";

    @Override
    public void save(AcHost acHost) throws Throwable {
        final AcHostModel acHostModel = fromAcHost(acHost);

        // TODO: Not sure how to do this with annotations in play. Currently this is not a container managed repo
        JPA.withTransaction(new F.Function0<Void>() {
            @Override
            public Void apply() throws Throwable {
                if (acHostModel.getId() != null) {
                    JPA.em().merge(acHostModel);
                }
                else {
                    create(acHostModel);
                }
                return null;
            }
        });
    }

    @Override
    public List<AcHostModel> all() {
        return JPA.em().createNamedQuery("AcHostModel.findAll", AcHostModel.class).getResultList();
    }

    @Override
    public Option<AcHostModel> findByKey(String key) {
        final List<AcHostModel> resultList = JPA.em().createNamedQuery("AcHostModel.findByKey", AcHostModel.class).
                setParameter("key", key).
                getResultList();
        return resultList.isEmpty() ? none(AcHostModel.class) : option(resultList.get(0));
    }

    @Override
    public Option<AcHostModel> findByUrl(String baseUrl) {
        final List<AcHostModel> resultList = JPA.em().createNamedQuery("AcHostModel.findByUrl", AcHostModel.class).
                setParameter(BASE_URL, baseUrl).
                getResultList();
        return resultList.isEmpty() ? none(AcHostModel.class) : option(resultList.get(0));
    }

    @Override
    public void create(AcHostModel hostModel) {
        JPA.em().persist(hostModel);
    }

    @Override
    public void delete(Long id) {
        final AcHostModel acHostModel = JPA.em().find(AcHostModel.class, id);

        if (acHostModel != null)
        {
            JPA.em().remove(acHostModel);
        }
    }

    @Override
    public AcHostModel fromAcHost(AcHost acHost) {
        if (acHost instanceof AcHostModel) {
            return (AcHostModel) acHost;
        }

        throw new IllegalStateException("Not implemented yet");
    }

}
