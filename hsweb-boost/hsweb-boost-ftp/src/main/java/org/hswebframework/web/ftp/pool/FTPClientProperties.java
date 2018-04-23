package org.hswebframework.web.ftp.pool;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

@Getter
@Setter
@ToString
public class FTPClientProperties extends GenericObjectPoolConfig {
    private String host;
    private int port = 22;
    private String username;
    private String password;
    private boolean passiveMode      = true;
    private String  encoding         = "utf-8";
    private int     clientTimeout    = 10 * 1000;
    private int     threadNum        = 20;
    private int     transferFileType = FTPClient.BINARY_FILE_TYPE;
    private boolean renameUploaded   = false;
    private int     retryTimes       = 3;

}