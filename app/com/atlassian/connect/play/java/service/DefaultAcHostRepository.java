package com.atlassian.connect.play.java.service;

import com.atlassian.connect.play.java.AcHost;
import com.atlassian.connect.play.java.model.AcHostModel;

public class DefaultAcHostRepository implements AcHostRepository {

    @Override
    public void save(AcHost acHost) {
        AcHostModel acHostModel = AcHostModel.fromAcHost(acHost);
        AcHostModel.create(acHostModel);
    }
}
