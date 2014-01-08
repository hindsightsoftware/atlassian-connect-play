package com.atlassian.connect.play.java.service;

import com.atlassian.connect.play.java.AcHost;

public interface AcHostRepository {

    // Throwable cause that's what play throws!!!!
    void save(AcHost acHost) throws Throwable;
}
