package org.hswebframework.web.service.file.simple;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.hswebframework.utils.time.DateFormatter;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.entity.file.FileInfoEntity;
import org.hswebframework.web.service.file.FileInfoService;
import org.hswebframework.web.service.file.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.security.MessageDigest;
import java.util.Date;

/**
 * 本地文件服务,将文件上传到本地文件系统中
 *
 * @author zhouhao
 * @since 3.0
 */
//@Service("fileService")
public class LocalFileService implements FileService {
    private FileInfoService fileInfoService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * 静态文件存储目录,不能以/结尾
     */
    private String staticFilePath = "./static/upload";

    /**
     * 静态文件访问地址,上传静态文件后,将返回此地址+文件相对地址,以/结尾
     */
    private String staticLocation = "/upload/";

    /**
     * 文件上传目录
     */
    private String filePath = "./upload/file";

    @Value("${hsweb.web.upload.static-file-path:./static/upload}")
    public void setStaticFilePath(String staticFilePath) {
        this.staticFilePath = staticFilePath;
    }

    @Value("${hsweb.web.upload.static-location:/upload/}")
    public void setStaticLocation(String staticLocation) {
        this.staticLocation = staticLocation;
    }

    @Value("${hsweb.web.upload.file-path:./upload/file}")
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getStaticFilePath() {
        return staticFilePath;
    }

    public String getStaticLocation() {
        return staticLocation;
    }

    @Autowired
    public void setFileInfoService(FileInfoService fileInfoService) {
        this.fileInfoService = fileInfoService;
    }

    @Override
    public InputStream readFile(String fileIdOrMd5) {
        FileInfoEntity fileInfo = fileInfoService.selectByIdOrMd5(fileIdOrMd5);
        if (fileInfo == null || !DataStatus.STATUS_ENABLED.equals(fileInfo.getStatus())) {
            throw new NotFoundException("file not found or disabled");
        }
        //配置中的文件上传根路径
        String filePath = getFilePath() + "/" + fileInfo.getLocation();
        File file = new File(filePath);
        if (!file.exists()) {
            throw new NotFoundException("file not found");
        }
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException ignore) {
            //  never happen
            throw new NotFoundException("file not found");
        }
    }

    @Override
    public String saveStaticFile(InputStream fileStream, String fileName) throws IOException {
        //文件后缀
        String suffix = fileName.contains(".") ?
                fileName.substring(fileName.lastIndexOf("."), fileName.length()) : "";

        //以日期划分目录
        String filePath = DateFormatter.toString(new Date(), "yyyyMMdd");

        //创建目录
        new File(getStaticFilePath() + "/" + filePath).mkdirs();

        // 存储的文件名
        String realFileName = System.nanoTime() + suffix;

        String fileAbsName = getStaticFilePath() + "/" + filePath + "/" + realFileName;
        try (FileOutputStream out = new FileOutputStream(fileAbsName)) {
            StreamUtils.copy(fileStream, out);
        }
        //响应上传成功的资源信息
        return getStaticLocation() + filePath + "/" + realFileName;
    }

    @Override
    @SuppressWarnings("all")
    public FileInfoEntity saveFile(InputStream fileStream, String fileName, String type, String creatorId) throws IOException {
        //配置中的文件上传根路径
        String fileBasePath = getFilePath();
        //文件存储的相对路径，以日期分隔，每天创建一个新的目录
        String filePath = DateFormatter.toString(new Date(), "yyyyMMdd");
        //文件存储绝对路径
        String absPath = fileBasePath.concat("/").concat(filePath);
        File path = new File(absPath);
        if (!path.exists()) {
            path.mkdirs(); //创建目录
        }
        String newName = String.valueOf(System.nanoTime()); //临时文件名 ,纳秒的md5值
        String fileAbsName = absPath.concat("/").concat(newName);
        int fileSize;
        MessageDigest digest = DigestUtils.getMd5Digest();
        try (InputStream proxyStream = new InputStream() {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                int l = fileStream.read(b, off, len);
                digest.update(b, off, len);
                return l;
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

            @Override
            public int read() throws IOException {
                return fileStream.read();
            }
        }; FileOutputStream os = new FileOutputStream(fileAbsName)) {
            int remainBytes = fileSize = proxyStream.available();
            byte[] buff = new byte[remainBytes > 1024 * 10 ? 1024 * 10 : remainBytes];
            int bytes;
            logger.info("开始写出文件:{}到:{}, size: {} bytes", fileName, fileAbsName, fileSize);
            while (remainBytes > 0) {
                bytes = proxyStream.read(buff, 0, remainBytes > buff.length ? buff.length : remainBytes);
                os.write(buff, 0, bytes);
                remainBytes -= bytes;
                logger.info("写出文件:{}:{},剩余数据量: {} bytes", fileName, fileAbsName, remainBytes);
            }
            // StreamUtils.copy(in, os);
        }

        String md5 = Hex.encodeHexString(digest.digest());

        File newFile = new File(fileAbsName);
        //获取文件的md5值
        //判断文件是否已经存在
        FileInfoEntity fileInfo = fileInfoService.selectByMd5(md5);
        if (fileInfo != null) {
            logger.info("文件:{}已上传过", fileAbsName);
            if (new File(getFilePath() + "/" + fileInfo.getLocation()).exists()) {
                newFile.delete();//文件已存在则删除临时文件不做处理
            } else {
                newFile.renameTo(new File(absPath.concat("/").concat(md5)));
            }
            return fileInfo;
        } else {
            logger.info("上传文件{}完成:{}->{}", fileName, fileAbsName, absPath.concat("/").concat(md5));
            newFile.renameTo(new File(absPath.concat("/").concat(md5)));
        }
        FileInfoEntity infoEntity = fileInfoService.createEntity();
        infoEntity.setCreateTimeNow();
        infoEntity.setCreatorId(creatorId);
        infoEntity.setLocation(filePath.concat("/").concat(md5));
        infoEntity.setName(fileName);
        infoEntity.setType(type);
        infoEntity.setSize((long) fileSize);
        infoEntity.setMd5(md5);
        infoEntity.setStatus(DataStatus.STATUS_ENABLED);
        fileInfoService.insert(infoEntity);
        return infoEntity;
    }

    @Override
    public void writeFile(String fileId, OutputStream out, long skip) throws IOException {
        try (InputStream inputStream = readFile(fileId)) {
            if (skip > 0) {
                long len = inputStream.skip(skip);
                logger.info("skip write stream {},{}", skip, len);
            }
            StreamUtils.copy(inputStream, out);
        }
    }

}
