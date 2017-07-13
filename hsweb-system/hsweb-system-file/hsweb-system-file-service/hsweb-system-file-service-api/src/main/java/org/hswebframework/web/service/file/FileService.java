package org.hswebframework.web.service.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 */
public interface FileService {

    InputStream readFile(String fileId);

    FileInfo saveFile(InputStream fileStream,String fileName,String creatorId) throws IOException;

    void writeFile(String fileId, OutputStream out);

    String md5(String fileId);

}
