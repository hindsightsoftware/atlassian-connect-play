package com.atlassian.connect.play.java.service;

import com.atlassian.connect.play.java.AcHost;
import com.atlassian.fugue.Option;
import play.libs.WS;

import static com.google.common.base.Preconditions.checkNotNull;
import static play.libs.WS.WSRequestHolder;

/**
 * Simple abstraction on the url methods of AC to facilitate testing
 */
public interface AcHostHttpClient {
    WSRequestHolder url(String url);

    WSRequestHolder url(String url, AcHost acHost, boolean signRequest);

    WSRequestHolder url(String url, AcHost acHost, Option<String> userId);
}
