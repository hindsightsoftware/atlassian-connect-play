package com.atlassian.connect.play.java.service;

import com.atlassian.connect.play.java.AcHost;
import com.atlassian.connect.play.java.model.AcHostModel;
import com.google.common.base.Objects;

import static play.libs.F.Function;
import static play.libs.F.Promise;

public class AcHostServiceImpl implements AcHostService {
    @Override
    public Promise<String> fetchPublicKeyFromRemoteHost(String hostBaseUrl) {
        // fetch from <host baseUrl>/plugins/servlet/oauth/consumer-info
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Promise<Boolean> registerHost(final AcHost acHost) {
        // TODO: validate host model against public key. This is not mentioned in https://developer.atlassian.com/static/connect/docs/pages/concepts/authentication.html
        // first check public key is correct by checking against <host baseUrl>/plugins/servlet/oauth/consumer-info
        // What other checks? Are there any holes here?
        Promise<Boolean> keysMatchPromise = fetchPublicKeyFromRemoteHost(acHost.getBaseUrl()).map(new Function<String, Boolean>() {
            @Override
            public Boolean apply(String fetchedPublicKey) throws Throwable {
                boolean keysMatch = Objects.equal(fetchedPublicKey, acHost.getPublicKey());
                if (!keysMatch) {
                    // TODO: log
                }
                return keysMatch;
            }
        });

//        keysMatchPromise.onRedeem(new F.Callback<Boolean>() {
//            @Override
//            public void invoke(Boolean keysMatch) throws Throwable {
//                if (keysMatch) {
//                    AcHostModel.create((AcHostModel) acHost); // TODO: Dodgy cast
//                }
//            }
//        });

        Promise<Boolean> hostRegistered = keysMatchPromise.map(new Function<Boolean, Boolean>() {
            @Override
            public Boolean apply(Boolean keysMatch) throws Throwable {
                if (!keysMatch) {
                    return false;
                }
                AcHostModel.create((AcHostModel) acHost); // TODO: Dodgy cast
                return true;
            }
        });


        return hostRegistered;
    }

//    private boolean isValidInstallRequest(final AcHost acHost, String fetchedPublicKey) {
//        boolean keysMatch = Objects.equal(fetchedPublicKey, acHost.getPublicKey());
//        if (!keysMatch) {
//            // TODO: log
//            return false;
//        }
//
//
////        return keysMatch;
//
//    }
}
