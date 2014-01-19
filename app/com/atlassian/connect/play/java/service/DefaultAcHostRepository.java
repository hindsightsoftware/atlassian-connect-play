package com.atlassian.connect.play.java.service;

import com.atlassian.connect.play.java.AcHost;
import com.atlassian.connect.play.java.model.AcHostModel;
import play.db.jpa.JPA;
import play.libs.F;

public class DefaultAcHostRepository implements AcHostRepository {

    @Override
    public void save(AcHost acHost) throws Throwable {
        final AcHostModel acHostModel = AcHostModel.fromAcHost(acHost);

        // TODO: Not sure how to do this with annotations in play. Currently this is not a container managed repo
        JPA.withTransaction(new F.Function0<Void>() {
            @Override
            public Void apply() throws Throwable {
                if (acHostModel.getId() != null) {
                    JPA.em().merge(acHostModel);
                }
                else {
                    AcHostModel.create(acHostModel);
                }
                return null;
            }
        });
    }
}
