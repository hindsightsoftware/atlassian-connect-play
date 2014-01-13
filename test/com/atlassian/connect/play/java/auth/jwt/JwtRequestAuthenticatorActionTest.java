package com.atlassian.connect.play.java.auth.jwt;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.SimpleResult;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static play.libs.F.Promise;
import static play.mvc.Http.Context;

@Ignore // need to do the registration first
@RunWith(MockitoJUnitRunner.class)
public class JwtRequestAuthenticatorActionTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private Context context;

    @Mock
    private Action delegate;

    @Test
    public void foo() throws Throwable {
        Promise<SimpleResult> result = new JwtRequestAuthenticatorAction.AuthenticationHelper().authenticate(context, delegate);
        assertThat(result.get(1000).getWrappedSimpleResult().header().status(), equalTo(403));
    }
}
