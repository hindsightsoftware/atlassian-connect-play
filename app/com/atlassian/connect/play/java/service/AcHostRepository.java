package com.atlassian.connect.play.java.service;

import com.atlassian.connect.play.java.AcHost;
import com.atlassian.connect.play.java.model.AcHostModel;
import com.atlassian.fugue.Option;

import java.util.List;

public interface AcHostRepository {

    // Throwable cause that's what play throws!!!!
    void save(AcHost acHost) throws Throwable;
    List<AcHostModel> all();
    Option<AcHostModel> findByKey(String key);
    Option<AcHostModel> findByUrl(String baseUrl);
    void create(AcHostModel hostModel);
    void delete(Long id);
    AcHostModel fromAcHost(AcHost acHost);
}
