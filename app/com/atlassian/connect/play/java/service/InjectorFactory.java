package com.atlassian.connect.play.java.service;

import com.atlassian.connect.play.java.AcHost;
import com.atlassian.fugue.Option;
import play.libs.WS;

public class InjectorFactory {

    public static AcHostService getAcHostService() {
        return new AcHostServiceImpl(new AcHostHttpClientImpl());
    }
}
