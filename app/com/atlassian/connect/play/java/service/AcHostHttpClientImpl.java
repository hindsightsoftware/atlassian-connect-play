package com.atlassian.connect.play.java.service;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.AcHost;
import com.atlassian.fugue.Option;
import play.libs.WS;

public class AcHostHttpClientImpl implements AcHostHttpClient {

    @Override
    public WS.WSRequestHolder url(String url) {
        return AC.url(url);
    }

    @Override
    public WS.WSRequestHolder url(String url, AcHost acHost, boolean signRequest) {
        return AC.url(url, acHost, signRequest);
    }

    @Override
    public WS.WSRequestHolder url(String url, AcHost acHost, Option<String> userId) {
        return AC.url(url, acHost, userId);
    }

}
