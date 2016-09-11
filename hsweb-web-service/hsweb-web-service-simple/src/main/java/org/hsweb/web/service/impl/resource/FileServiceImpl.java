package org.hsweb.web.service.impl.resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.hsweb.web.bean.po.resource.Resources;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.exception.NotFoundException;
import org.hsweb.web.service.config.ConfigService;
import org.hsweb.web.service.resource.FileService;
import org.hsweb.web.service.resource.ResourcesService;
import org.hsweb.web.core.utils.WebUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hsweb.commons.DateTimeUtils;
import org.hsweb.commons.MD5;

import javax.annotation.Resource;
import java.io.*;
import java.util.Date;


@Service("fileService")
public class FileServiceImpl implements FileService {
    @Resource
    protected ConfigService configService;
    @Resource
    protected ResourcesService resourcesService;

    public String getFileBasePath() {
        return configService.get("upload", "basePath", "./upload").trim();
    }

    @Override
    public InputStream readResources(String resourceId) throws IOException {
        Resources resources = resourcesService.selectByPk(resourceId);
        if (resources == null) throw new NotFoundException("文件不存在");
        return readResources(resources);
    }

    @Override
    public InputStream readResources(Resources resources) throws IOException {
        String fileBasePath = getFileBasePath();
        File file = new File(fileBasePath.concat(resources.getPath().concat("/".concat(resources.getMd5()))));
        if (!file.canRead()) {
            throw new NotFoundException("文件不存在");
        }
        return new FileInputStream(file);
    }

    @Override
    public void writeResources(Resources resources, OutputStream outputStream) throws IOException {
        try (InputStream inputStream = readResources(resources)) {
            byte b[] = new byte[2048 * 10];
            while ((inputStream.read(b)) != -1) {
                outputStream.write(b);
            }
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public Resources saveFile(InputStream is, String fileName) throws IOException {
        //配置中的文件上传根路径
        String fileBasePath = getFileBasePath();
        //文件存储的相对路径，以日期分隔，每天创建一个新的目录
        String filePath = "/file/".concat(DateTimeUtils.format(new Date(), DateTimeUtils.YEAR_MONTH_DAY));
        //文件存储绝对路径
        String absPath = fileBasePath.concat(filePath);
        File path = new File(absPath);
        if (!path.exists()) path.mkdirs(); //创建目录
        String newName = MD5.encode(String.valueOf(System.nanoTime())); //临时文件名 ,纳秒的md5值
        String fileAbsName = absPath.concat("/").concat(newName);
        //try with resource
        long fileLength = 0;
        try (BufferedInputStream in = new BufferedInputStream(is);
             BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(fileAbsName))) {
            byte[] buffer = new byte[2048 * 10];
            int len;
            while ((len = in.read(buffer)) != -1) {
                fileLength+=len;
                os.write(buffer, 0, len);
            }
            os.flush();
        }
        File newFile = new File(fileAbsName);
        //获取文件的md5值
        String md5;
        try (FileInputStream inputStream = new FileInputStream(newFile)) {
            md5 = DigestUtils.md5Hex(inputStream);
        }
        //判断文件是否已经存在
        Resources resources = resourcesService.selectByMd5(md5);
        if (resources != null) {
            newFile.delete();//文件已存在则删除临时文件不做处理
            return resources;
        } else {
            newName = md5;
            newFile.renameTo(new File(absPath.concat("/").concat(newName)));
        }
        resources = new Resources();
        resources.setStatus(1);
        resources.setPath(filePath);
        resources.setMd5(md5);
        resources.setCreateDate(new Date());
        resources.setType("file");
        resources.setSize(fileLength);
        resources.setName(fileName);
        try {
            User user = WebUtil.getLoginUser();
            if (user != null) {
                resources.setCreatorId(user.getId());
            } else {
                resources.setCreatorId("1");
            }
        } catch (Exception e) {
            resources.setCreatorId("1");
        }
        resourcesService.insert(resources);
        return resources;
    }
}
