package org.hswebframework.web.dev.tools.writer;

import lombok.SneakyThrows;
import org.hswebframework.utils.file.FileUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.List;

/**
 * @author zhouhao
 * @since 3.0
 */
public class DefaultCodeWriter implements CodeWriter {

    @Value("${hsweb.dev.workspace:./}")
    private String workspace = "./";

    @SneakyThrows
    private void writeCode(String path, GeneratedCode code) {
        File file = new File(path);
        file.mkdir();
        String type = code.getType();

        String filePath = path + "/" + code.getFile();
        if ("dir".equals(type)) {
            code.getChildren()
                    .forEach(childrenCode -> writeCode(filePath, childrenCode));
        } else if ("file".equals(type)) {
            String template = code.getTemplate();
            String fileName = filePath;
            String replaceMod = code.getRepeat();
            File codeFile = new File(fileName);
            if (codeFile.exists() && replaceMod != null && !fileName.endsWith(".java")) {
                switch (replaceMod) {
                    case "ignore":
                        return;
                    case "append":
                        String old = FileUtils.reader2String(fileName);
                        template = old + template;
                        break;
                    default:
                        break;
                }
            }
            if (fileName.endsWith(".java")) {
                ClassWriter.writeClass(fileName, template);
            } else {
                FileUtils.writeString2File(template, fileName, "utf-8");
            }
        }
    }

    @Override
    public String write(List<GeneratedCode> codes) {

        codes.forEach(code -> writeCode(workspace, code));

        return workspace;
    }
}
