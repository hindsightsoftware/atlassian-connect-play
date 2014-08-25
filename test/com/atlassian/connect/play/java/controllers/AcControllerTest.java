package com.atlassian.connect.play.java.controllers;

import com.atlassian.connect.play.java.service.AcHostService;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Result;

import java.io.File;
import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.test.Helpers.*;

public class AcControllerTest {

    @Test
    public void testRegisterHost() throws IOException {
        final JsonNode installJson = readJsonFromTestFile("installEvent.json");
        final AcHostService hostService = mock(AcHostService.class);
        when(hostService.registerHost(eq("1234567890"), eq("http://jira.atlassian.com:2990/jira"), eq("PK GOES HERE"),
                eq("SHARED SECRET"), eq("jira"))).thenReturn(
                Promise.promise(new Function0<Void>() {
                    @Override
                    public Void apply() throws Throwable {
                        return null;
                    }
                }));

        running(fakeApplication(), new Runnable() {
            public void run() {
                AcController.acHostService = hostService;
                Result result = callAction(
                        com.atlassian.connect.play.java.controllers.routes.ref.AcController.registration(),
                        fakeRequest().withJsonBody(installJson)
                );
                assertThat(status(result)).isEqualTo(OK);
            }
        });
    }

    private JsonNode readJsonFromTestFile(String filename) throws IOException {
        return Json.parse(FileUtils.readFileToString(new File("test/resources/" + filename)));
    }
}
