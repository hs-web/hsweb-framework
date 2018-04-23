package org.hswebframework.web.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author zhouhao
 * @since 3.0
 */
public interface FTPOperation {

    boolean delete(String fileName);

    boolean rename(String from, String to);

    boolean download(String fileName, OutputStream outputStream);

    boolean upload(String fileName, InputStream input);

    boolean upload(String fileName, String text);

    void list(String path, Consumer<FTPFile> consumer);

    <T> T doExecute(Function<FTPClient, T> command);

    interface HandleExceptionFunction<T>{
        T apply(FTPClient client) throws Exception;
    }

}
