package org.hsweb.web.service.impl.resource;

/**
 * Created by 浩 on 2015-11-26 0026.
 */

import org.apache.commons.codec.digest.DigestUtils;
import org.hsweb.web.bean.po.resource.Resources;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.service.config.ConfigService;
import org.hsweb.web.service.resource.FileService;
import org.hsweb.web.service.resource.ResourcesService;
import org.hsweb.web.core.utils.WebUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webbuilder.utils.common.DateTimeUtils;
import org.webbuilder.utils.common.MD5;

import javax.annotation.Resource;
import java.io.*;
import java.util.Date;


@Service("fileService")
public class FileServiceImpl implements FileService {
    @Resource
    protected ConfigService configService;
    @Resource
    protected ResourcesService resourcesService;

    @Transactional(rollbackFor = Throwable.class)
    public Resources saveFile(InputStream is, String fileName) throws Exception {
        //配置中的文件上传根路径
        String fileBasePath = configService.get("upload", "basePath", "/upload").trim();
        //文件存储的相对路径，以日期分隔，每天创建一个新的目录
        String filePath = "/file/".concat(DateTimeUtils.format(new Date(), DateTimeUtils.YEAR_MONTH_DAY));
        //文件存储绝对路径
        String absPath = fileBasePath.concat(filePath);
        File path = new File(absPath);
        if (!path.exists()) path.mkdirs(); //创建目录
        String newName = MD5.encode(String.valueOf(System.nanoTime())); //临时文件名 ,纳秒的md5值
        String fileAbsName = absPath.concat("/").concat(newName);
        //try with resource
        try (BufferedInputStream in = new BufferedInputStream(is);
             //写出文件
             BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(fileAbsName))) {
            byte[] buffer = new byte[2048 * 10];
            int len;
            while ((len = in.read(buffer)) != -1) {
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
        resources.setCreate_date(new Date());
        resources.setType("file");
        resources.setName(fileName);
        try {
            User user = WebUtil.getLoginUser();
            if (user != null) {
                resources.setCreator_id(user.getU_id());
            } else {
                resources.setCreator_id("-1");
            }
        } catch (Exception e) {
            resources.setCreator_id("-1");
        }

        resourcesService.insert(resources);
        return resources;
    }
}
