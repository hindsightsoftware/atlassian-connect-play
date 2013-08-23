package com.atlassian.connect.play.java;

import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

public final class SessionValidatorAction extends Action.Simple
{
    @Override
    public Result call(Http.Context context) throws Throwable
    {
        if (context.session().containsKey(AC.Session.AC_HOST_KEY))
        {
            final String hostKey = context.session().get(AC.Session.AC_HOST_KEY);
            final AcHost host = AC.getAcHost(hostKey).getOrNull();
            if (host == null)
            {
                return badRequest("Unknown host for consumer key: " + hostKey);
            }
        }
        else
        {
            return unauthorized("Unauthorised: No valid session was found.");
        }

        return delegate.call(context);
    }
}
