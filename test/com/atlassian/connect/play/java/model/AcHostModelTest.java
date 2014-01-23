package com.atlassian.connect.play.java.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import play.libs.Json;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AcHostModelTest {

    @Test
    public void unmarshalsFromInstallJson() throws IOException {
        AcHostModel acHostModel = AcHostModel.fromJson(readJsonFromTestFile("installEvent.json"), new AcHostModel());
        assertThat(acHostModel.getBaseUrl(), is("http://jira.atlassian.com:2990/jira"));
        assertThat(acHostModel.getKey(), is("1234567890"));
        assertThat(acHostModel.getName(), is("jira"));
        assertThat(acHostModel.getPublicKey(), is("PK GOES HERE"));
        assertThat(acHostModel.getSharedSecret(), is("SHARED SECRET"));
    }

    private JsonNode readJsonFromTestFile(String filename) throws IOException {
        return Json.parse(FileUtils.readFileToString(new File("test/resources/" + filename)));
    }
}
