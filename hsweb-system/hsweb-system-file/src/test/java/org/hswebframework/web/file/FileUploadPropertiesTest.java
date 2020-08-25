package org.hswebframework.web.file;

import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.*;

public class FileUploadPropertiesTest {


    @Test
    public void testNoSet(){
        FileUploadProperties uploadProperties=new FileUploadProperties();
        assertFalse(uploadProperties.denied("test.xls", MediaType.ALL));

        assertFalse(uploadProperties.denied("test.exe", MediaType.ALL));
    }

    @Test
    public void testDenyWithAllow(){
        FileUploadProperties uploadProperties=new FileUploadProperties();
        uploadProperties.setAllowFiles(new HashSet<>(Arrays.asList("xls","json")));

        assertFalse(uploadProperties.denied("test.xls", MediaType.ALL));

        assertTrue(uploadProperties.denied("test.exe", MediaType.ALL));
    }

    @Test
    public void testDenyWithAllowMediaType(){
        FileUploadProperties uploadProperties=new FileUploadProperties();
        uploadProperties.setAllowMediaType(new HashSet<>(Arrays.asList("application/xls","application/json")));

        assertFalse(uploadProperties.denied("test.json", MediaType.APPLICATION_JSON));

        assertTrue(uploadProperties.denied("test.exe", MediaType.ALL));
    }



    @Test
    public void testDenyWithDenyMediaType(){
        FileUploadProperties uploadProperties=new FileUploadProperties();
        uploadProperties.setDenyMediaType(new HashSet<>(Arrays.asList("application/json")));

        assertFalse(uploadProperties.denied("test.xls", MediaType.ALL));

        assertTrue(uploadProperties.denied("test.exe", MediaType.APPLICATION_JSON));

    }
    @Test
    public void testDenyWithDeny(){
        FileUploadProperties uploadProperties=new FileUploadProperties();
        uploadProperties.setDenyFiles(new HashSet<>(Arrays.asList("exe")));

        assertFalse(uploadProperties.denied("test.xls", MediaType.ALL));

        assertTrue(uploadProperties.denied("test.exe", MediaType.ALL));

    }


}