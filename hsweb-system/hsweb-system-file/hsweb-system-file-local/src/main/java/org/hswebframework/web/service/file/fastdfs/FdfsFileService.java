package org.hswebframework.web.service.file.fastdfs;

import com.luhuiguo.fastdfs.domain.StorePath;
import com.luhuiguo.fastdfs.service.FastFileStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.entity.file.FileInfoEntity;
import org.hswebframework.web.service.file.FileInfoService;
import org.hswebframework.web.service.file.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.HashSet;

/**
 * @author zhouhao
 * @since 3.0
 */
@Slf4j
public class FdfsFileService implements FileService {
    private FastFileStorageClient fastFileStorageClient;

    private FileInfoService fileInfoService;

    private String staticLocation = "/";

    @Autowired
    public void setFastFileStorageClient(FastFileStorageClient fastFileStorageClient) {
        this.fastFileStorageClient = fastFileStorageClient;
    }

    @Autowired
    public void setFileInfoService(FileInfoService fileInfoService) {
        this.fileInfoService = fileInfoService;
    }

    public void setStaticLocation(String staticLocation) {
        this.staticLocation = staticLocation;
    }

    public String getStaticLocation() {
        return staticLocation;
    }

    @Override
    public InputStream readFile(String fileIdOrMd5) {
        FileInfoEntity entity = fileInfoService.selectByIdOrMd5(fileIdOrMd5);
        StorePath path = StorePath.praseFromUrl(entity.getLocation());
        return fastFileStorageClient.downloadFile(path.getGroup(), path.getPath(), ins -> ins);
    }

    @Override
    public FileInfoEntity saveFile(InputStream fileStream, String fileName, String type, String creatorId) throws IOException {
//        MetaData createIdMeta = new MetaData("creatorId", creatorId);
        MessageDigest digest = DigestUtils.getMd5Digest();
        String suffix = fileName.contains(".") ?
                fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()) : "";

        StorePath path;
        int fileSize;
        try (InputStream tmp = new InputStream() {

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                int r = super.read(b, off, len);
                digest.update(b, off, len);
                return r;
            }

            @Override
            public int read() throws IOException {
                return fileStream.read();
            }

            @Override
            public void close() throws IOException {
                fileStream.close();
                super.close();
            }

            @Override
            public int available() throws IOException {
                return fileStream.available();
            }
        }) {
            path = fastFileStorageClient.uploadFile(tmp, fileSize = tmp.available(), suffix, new HashSet<>());
        }
        String md5 = Hex.encodeHexString(digest.digest());
        FileInfoEntity fileInfo = fileInfoService.createEntity();
        fileInfo.setLocation(path.getFullPath());
        fileInfo.setMd5(md5);
        fileInfo.setStatus(DataStatus.STATUS_ENABLED);
        fileInfo.setSize((long) fileSize);
        fileInfo.setName(fileName);
        fileInfo.setType(type);
        fileInfo.setCreatorId(creatorId);
        fileInfo.setCreateTimeNow();
        fileInfoService.insert(fileInfo);

        return fileInfo;
    }

    @Override
    public String saveStaticFile(InputStream fileStream, String fileName) throws IOException {
        //文件后缀
        String suffix = fileName.contains(".") ?
                fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()) : "";

        StorePath path = fastFileStorageClient.uploadFile(fileStream, fileStream.available(), suffix, new HashSet<>());

        return staticLocation.concat(path.getFullPath());
    }

    @Override
    public void writeFile(String fileId, OutputStream out, long skip) throws IOException {
        try (InputStream inputStream = readFile(fileId)) {
            if (skip > 0) {
                long len = inputStream.skip(skip);
                log.info("skip write {} len:{}", skip, len);
            }
            StreamUtils.copy(inputStream, out);
        }
    }

}
