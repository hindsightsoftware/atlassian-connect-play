package com.atlassian.connect.play.java.service;

import com.atlassian.connect.play.java.AcHost;
import com.atlassian.connect.play.java.auth.InvalidAuthenticationRequestException;
import com.atlassian.connect.play.java.auth.MismatchPublicKeyException;
import com.atlassian.connect.play.java.auth.PublicKeyVerificationFailureException;
import com.atlassian.connect.play.java.model.AcHostModel;
import com.google.common.base.Charsets;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Document;
import play.libs.XML;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang.StringUtils.stripToEmpty;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static play.libs.F.Promise;
import static play.libs.WS.Response;
import static play.libs.WS.WSRequestHolder;

@RunWith(MockitoJUnitRunner.class)
public class AcHostServiceImplTest {
    private static final String TEST_PUBLIC_KEY = "REAL-PK-GOES-HERE";
    private static Document testClientInfoDocument;
    private static Document testDodgyClientInfoDocument;
    private static Document testMismatchedPKClientInfoDocument;


    @Mock
    private AcHostHttpClient httpClient;

    @Mock
    private WSRequestHolder requestHolder;

    @Mock
    private Response response;

    @Mock
    private AcHostRepository acHostRepository;

    private AcHostServiceImpl acHostService;
    private AcHostModel acHostModel;

    @BeforeClass
    public static void loadTestData() throws FileNotFoundException {
        testClientInfoDocument = fetchTestDocument("consumer-info.xml");
        testDodgyClientInfoDocument = fetchTestDocument("dodgy-consumer-info.xml");
        testMismatchedPKClientInfoDocument = fetchTestDocument("mismatched-pk-consumer-info.xml");
    }

    private static Document fetchTestDocument(String filename) throws FileNotFoundException {
        return XML.fromInputStream(new FileInputStream("test/resources/" + filename), Charsets.UTF_8.name());
    }


    @Before
    public void init() {
        acHostService = new AcHostServiceImpl(httpClient, acHostRepository);
        acHostModel = new AcHostModel();
        acHostModel.publicKey = TEST_PUBLIC_KEY;

        when(httpClient.url(anyString(), any(AcHost.class), anyBoolean())).thenReturn(requestHolder);
        when(requestHolder.get()).thenReturn(Promise.pure(response));
        when(response.getStatus()).thenReturn(200);
        when(response.asXml()).thenReturn(testClientInfoDocument);
    }

    @Test
    public void sendsCorrectHttpRequest() {
        acHostService.fetchPublicKeyFromRemoteHost(acHostModel);
        verify(httpClient).url(AcHostServiceImpl.CONSUMER_INFO_URL, acHostModel, false);
        verify(requestHolder).get();
    }

    @Test
    public void extractsCorrectPublicKey() {
        Promise<String> publicKeyPromise = acHostService.fetchPublicKeyFromRemoteHost(acHostModel);
        String publicKey = stripToEmpty(publicKeyPromise.get(1, TimeUnit.SECONDS));

        assertThat(publicKey, is(TEST_PUBLIC_KEY));
    }

    @Test(expected = PublicKeyVerificationFailureException.class)
    public void returnsFailurePromiseWhenFailToFetchPublicKey() {
        when(response.getStatus()).thenReturn(401);
        Promise<String> publicKeyPromise = acHostService.fetchPublicKeyFromRemoteHost(acHostModel);
        publicKeyPromise.get(1, TimeUnit.SECONDS);
    }

    @Test(expected = RuntimeException.class)
    public void returnsFailurePromiseWhenFailToParseXml() {
        when(response.asXml()).thenThrow(new RuntimeException("blah"));
        Promise<String> publicKeyPromise = acHostService.fetchPublicKeyFromRemoteHost(acHostModel);
        publicKeyPromise.get(1, TimeUnit.SECONDS);
    }

    @Test(expected = PublicKeyVerificationFailureException.class)
    public void returnsFailurePromiseWhenPublicKeyNotFoundInXml() {
        when(response.asXml()).thenReturn(testDodgyClientInfoDocument);
        Promise<String> publicKeyPromise = acHostService.fetchPublicKeyFromRemoteHost(acHostModel);
        publicKeyPromise.get(1, TimeUnit.SECONDS);
    }


    @Test
    public void savesAcHostWhenPublicKeysMatch() throws Throwable {
        acHostService.registerHost(acHostModel).get(1, TimeUnit.SECONDS);
        verify(acHostRepository).save(acHostModel);
    }

    @Test(expected = InvalidAuthenticationRequestException.class)
    public void returnsFailurePromiseWhenNoPublicKeyProvided() {
        acHostModel.publicKey = "  ";
        acHostService.registerHost(acHostModel).get(1, TimeUnit.SECONDS);
    }

    @Test(expected = MismatchPublicKeyException.class)
    public void returnsFailurePromiseWhenPublicKeyMismatched() {
        when(response.asXml()).thenReturn(testMismatchedPKClientInfoDocument);
        acHostService.registerHost(acHostModel).get(1, TimeUnit.SECONDS);
    }

    @Test(expected = PublicKeyVerificationFailureException.class)
    public void returnsFailurePromiseWhenFailToFetchPublicKeyDuringRegistration() {
        when(response.getStatus()).thenReturn(401);
        acHostService.registerHost(acHostModel).get(1, TimeUnit.SECONDS);
    }
}
