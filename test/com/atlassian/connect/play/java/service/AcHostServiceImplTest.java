package com.atlassian.connect.play.java.service;

import com.atlassian.connect.play.java.AcHost;
import com.atlassian.connect.play.java.model.AcHostModel;
import com.google.common.base.Charsets;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Document;
import play.libs.F;
import play.libs.XML;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang.StringUtils.stripToEmpty;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static play.libs.F.Promise;
import static play.libs.WS.Response;
import static play.libs.WS.WSRequestHolder;

@RunWith(MockitoJUnitRunner.class)
public class AcHostServiceImplTest {
    private static Document testClientInfoDocument;

    @Mock
    private AcHostHttpClient httpClient;

    @Mock
    private WSRequestHolder requestHolder;

    @Mock
    private Response response;

    private AcHostServiceImpl acHostService;
    private AcHostModel acHostModel;

    @BeforeClass
    public static void loadTestData() throws FileNotFoundException {
        testClientInfoDocument = XML.fromInputStream(new FileInputStream("test/resources/consumer-info.xml"), Charsets.UTF_8.name());
    }


    @Before
    public void init() {
        acHostService = new AcHostServiceImpl(httpClient);
        acHostModel = new AcHostModel();
        when(httpClient.url(anyString(), any(AcHost.class))).thenReturn(requestHolder);
        when(requestHolder.get()).thenReturn(Promise.pure(response));
        when(response.getStatus()).thenReturn(200);
        when(response.asXml()).thenReturn(testClientInfoDocument);
    }

    @Test
    public void sendsCorrectHttpRequest() {
        acHostService.fetchPublicKeyFromRemoteHost(acHostModel);
        verify(httpClient).url(AcHostServiceImpl.CONSUMER_INFO_URL, acHostModel);
        verify(requestHolder).get();
    }

    @Test
    public void extractsCorrectPublicKey() {
        Promise<String> publicKeyPromise = acHostService.fetchPublicKeyFromRemoteHost(acHostModel);
        String publicKey = stripToEmpty(publicKeyPromise.get(1, TimeUnit.SECONDS));
        assertThat(publicKey, startsWith("FOO"));
        assertThat(publicKey, endsWith("BAR"));
    }

    // TODO: negative test cases for fetchPublicKeyFromRemoteHost

}
