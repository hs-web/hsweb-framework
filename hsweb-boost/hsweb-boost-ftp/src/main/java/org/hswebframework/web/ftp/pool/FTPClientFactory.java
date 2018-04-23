package org.hswebframework.web.ftp.pool;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;


public class FTPClientFactory implements PooledObjectFactory<FTPClient> {
    private static Logger logger = LoggerFactory.getLogger(FTPClientFactory.class);
    private FTPClientProperties config;

    public FTPClientFactory(FTPClientProperties config) {
        this.config = config;
    }

    public PooledObject<FTPClient> makeObject() throws Exception {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setConnectTimeout(config.getClientTimeout());
        ftpClient.connect(config.getHost(), config.getPort());
        int reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftpClient.disconnect();
            logger.warn("FTPServer refused connection");
            return null;
        }
        boolean result = ftpClient.login(config.getUsername(), config.getPassword());
        if (!result) {
            throw new ConnectException("ftp登陆失败:" + config.getUsername() + "/password:" + config.getPassword() + "@" + config.getHost());
        }
        ftpClient.setFileType(config.getTransferFileType());
        ftpClient.setBufferSize(1024);
        ftpClient.setControlEncoding(config.getEncoding());
        if (config.isPassiveMode()) {
            ftpClient.enterLocalPassiveMode();
        }
        return new DefaultPooledObject<>(ftpClient);

    }

    @Override
    public void destroyObject(PooledObject<FTPClient> p) throws Exception {
        try {
            p.getObject().logout();
        } finally {
            p.getObject().disconnect();
        }
    }

    @Override
    public boolean validateObject(PooledObject<FTPClient> p) {
        try {
            p.getObject().sendNoOp();
        } catch (IOException e) {
            logger.warn("validateObject ftp error!", e);
            return false;
        }
        return p.getObject().isConnected() && p.getObject().isAvailable();
    }

    @Override
    public void activateObject(PooledObject<FTPClient> p) throws Exception {
        p.getObject().sendNoOp();
    }

    @Override
    public void passivateObject(PooledObject<FTPClient> p)  {

    }
}