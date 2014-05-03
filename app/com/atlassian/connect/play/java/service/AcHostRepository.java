package com.atlassian.connect.play.java.service;

import com.atlassian.connect.play.java.AcHost;
import com.atlassian.fugue.Option;

import java.util.List;

public interface AcHostRepository {

    // Throwable cause that's what play throws!!!!
    void save(AcHost acHost) throws Throwable;
    List<? extends AcHost> all() throws Throwable;
    Option<AcHost> findByKey(String key) throws Throwable;
    Option<AcHost> findByUrl(String baseUrl) throws Throwable;
    void delete(Long id) throws Throwable;
    AcHost create();
}
