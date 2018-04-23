package org.hswebframework.web.ftp;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.hswebframework.web.ftp.pool.FTPClientPool;
import org.jooq.lambda.Unchecked;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author zhouhao
 * @since 3.0
 */
@AllArgsConstructor
public class DefaultFTPOperation implements FTPOperation {

    private FTPClientPool pool;

    @SneakyThrows
    protected FTPClient getClient() {
        return pool.borrowObject();
    }

    protected void returnClient(FTPClient client) {
        pool.returnObject(client);
    }

    public <T> T doExecute(Function<FTPClient, T> function) {
        FTPClient client = getClient();
        try {
            return function.apply(client);
        } finally {
            returnClient(client);
        }
    }

    @Override
    public boolean delete(String fileName) {
        return doExecute(Unchecked.function(client -> client.deleteFile(fileName)));
    }

    @Override
    public boolean rename(String from, String to) {
        return doExecute(Unchecked.function(client -> client.rename(from, to)));
    }

    @Override
    public boolean download(String fileName, OutputStream outputStream) {
        return doExecute(Unchecked.function(client -> client.retrieveFile(fileName, outputStream)));
    }

    @Override
    public boolean upload(String fileName, InputStream input) {
        return doExecute(Unchecked.function(client -> client.storeFile(fileName, input)));
    }

    @Override
    public boolean upload(String fileName, String text) {
        return doExecute(Unchecked.function(client -> {
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(text.getBytes())) {
                return client.storeFile(fileName, inputStream);
            }
        }));
    }

    @Override
    public void list(String path, Consumer<FTPFile> consumer) {
        doExecute(Unchecked.function(client -> {
            Arrays.stream(client.listFiles(path)).forEach(consumer);
            return null;
        }));
    }
}
