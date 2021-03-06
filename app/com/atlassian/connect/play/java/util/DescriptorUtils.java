package com.atlassian.connect.play.java.util;

import com.atlassian.connect.play.java.AC;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.lang.StringUtils.replace;

public class DescriptorUtils {
    public static final String ATLASSIAN_CONNECT_JSON_FILE_NAME = "atlassian-connect.json";

    public static String substituteVariables(String sourceJson) {
        String s = replace(sourceJson, "${localBaseUrl}", AC.baseUrl.get());
        s = replace(s, "${addonName}", AC.PLUGIN_NAME);
        return replace(s, "${addonKey}", AC.PLUGIN_KEY); // ouch very hacky. TODO: implement a proper solution for this
    }

    public static String substituteVariablesInFile(File sourceJsonFile) throws IOException {
        return substituteVariables(Files.toString(sourceJsonFile, Charsets.UTF_8));
    }

    public static String substituteVariablesInDefaultFile() throws IOException {
        return substituteVariablesInFile(new File(ATLASSIAN_CONNECT_JSON_FILE_NAME));
    }
}
