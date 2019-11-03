package org.hswebframework.web.file;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.utils.time.DateFormatter;
import org.hswebframework.web.id.IDGenerator;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;
import java.util.Date;

@Getter
@Setter
@ConfigurationProperties(prefix = "hsweb.file.upload")
public class FileUploadProperties {

    private String staticFilePath = "./static";

    private String staticLocation = "/static";


    public StaticFileInfo createStaticSavePath(String name) {
        String fileName = IDGenerator.SNOW_FLAKE_STRING.generate();
        String filePath = DateFormatter.toString(new Date(), "yyyyMMdd");
        String absPath = staticFilePath.concat("/").concat(filePath);
        //文件后缀
        String suffix = name.contains(".") ?
                name.substring(name.lastIndexOf(".")) : "";

        new File(absPath).mkdirs();
        StaticFileInfo info = new StaticFileInfo();

        info.location = staticLocation + "/" + filePath + "/" + fileName + suffix;
        info.savePath = absPath + "/" + fileName + suffix;

        return info;
    }

    @Getter
    @Setter
    public class StaticFileInfo {
        private String savePath;

        private String location;
    }
}
