package org.hswebframework.web.bean;

import lombok.Getter;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class FastBeanCopierBeanTest {

    @Test
    public void testCopyDirectGenericListField() {
        Map<String, Object> source = Map.of(
            "fileData",
            List.of(Map.of("name", "image.jpg"))
        );

        DirectOutput output = FastBeanCopier.copy(source, new DirectOutput());

        Assert.assertNotNull(output.getFileData());
        Assert.assertEquals(1, output.getFileData().size());
        Assert.assertTrue(output.getFileData().get(0) instanceof FileItem);
        Assert.assertEquals("image.jpg", output.getFileData().get(0).getName());
    }

    @Test
    public void testCopyGenericSuperclassListField() {
        Map<String, Object> source = Map.of(
            "fileData",
            List.of(Map.of("name", "image.jpg"))
        );

        GenericOutput output = FastBeanCopier.copy(source, new GenericOutput());

        Assert.assertNotNull(output.getFileData());
        Assert.assertEquals(1, output.getFileData().size());
        Assert.assertTrue(output.getFileData().get(0) instanceof FileItem);
        Assert.assertEquals("image.jpg", output.getFileData().get(0).getName());
    }

    @Getter
    @Setter
    public static class DirectOutput {
        private List<FileItem> fileData;
    }

    @Getter
    @Setter
    public static class GenericBase<T> {
        private List<T> fileData;
    }

    public static class GenericOutput extends GenericBase<FileItem> {
    }

    @Getter
    @Setter
    public static class FileItem {
        private String name;
    }
}
