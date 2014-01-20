package com.atlassian.connect.play.java.util;

import com.atlassian.connect.play.java.AC;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;

public class DescriptorUtils {

    public static String substituteVariables(String sourceJson) {
        return StringUtils.replace(sourceJson, "${localBaseUrl}", AC.baseUrl.get());
    }

    public static String substituteVariablesInFile(File sourceJsonFile) throws IOException {
        return substituteVariables(Files.toString(sourceJsonFile, Charsets.UTF_8));
    }

    public static String substituteVariablesInDefaultFile() throws IOException {
        return substituteVariablesInFile(new File("atlassian-connect.json"));
    }
}
