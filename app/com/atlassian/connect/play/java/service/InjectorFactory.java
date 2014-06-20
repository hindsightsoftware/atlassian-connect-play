package com.atlassian.connect.play.java.service;

import com.atlassian.connect.play.java.AcHost;
import com.atlassian.fugue.Option;
import org.apache.commons.lang.StringUtils;
import play.Application;
import play.Play;
import play.libs.WS;

import static com.atlassian.connect.play.java.util.Utils.LOGGER;

public class InjectorFactory {

    public static AcHostService getAcHostService() {
        String acHostServiceClass = Play.application().configuration().getString("ac.hostrepository");

        if (!StringUtils.isEmpty(acHostServiceClass)) {
            try {
                AcHostRepository repository = (AcHostRepository) Class.forName(acHostServiceClass, false, Play.application().classloader()).newInstance();
                return new AcHostServiceImpl(new AcHostHttpClientImpl(), repository);
            } catch (Exception e) {
                LOGGER.error("Could not load " + acHostServiceClass + " as the AC Host Repository", e);
                throw new IllegalStateException("No AC Host Repository available", e);
            }
        } else {
            return new AcHostServiceImpl(new AcHostHttpClientImpl(),  new DefaultAcHostRepository());
        }
    }
}
