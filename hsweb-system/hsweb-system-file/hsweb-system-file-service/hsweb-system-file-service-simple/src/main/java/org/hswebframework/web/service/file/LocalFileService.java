package org.hswebframework.web.service.file;

import org.apache.commons.codec.digest.DigestUtils;
import org.hswebframework.utils.time.DateFormatter;

import java.io.*;
import java.util.Date;

/**
 * Created by zhouhao on 2017/7/8.
 */
public class LocalFileService implements FileService {

    private String fileBasePath=".";


    public String getFileBasePath() {
        return fileBasePath;
    }

    @Override
    public InputStream readFile(String fileId) {

        return null;
    }

    @Override
    public FileInfo saveFile(InputStream fileStream, String fileName,String creatorId) throws IOException {
        //配置中的文件上传根路径
        String fileBasePath = getFileBasePath();
        //文件存储的相对路径，以日期分隔，每天创建一个新的目录
        String filePath = "/file/".concat(DateFormatter.toString(new Date(),"yyyy-MM-dd"));
        //文件存储绝对路径
        String absPath = fileBasePath.concat(filePath);
        File path = new File(absPath);
        if (!path.exists()) path.mkdirs(); //创建目录
        String newName = String.valueOf(System.nanoTime()); //临时文件名 ,纳秒的md5值
        String fileAbsName = absPath.concat("/").concat(newName);
        //try with resource
        long fileLength = 0;
        try (BufferedInputStream in = new BufferedInputStream(fileStream);
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
      //  Resources resources = resourcesService.selectByMd5(md5);
       // if (resources != null) {
         //   newFile.delete();//文件已存在则删除临时文件不做处理
           // return resources;
        //} else {
          //  newName = md5;
            //newFile.renameTo(new File(absPath.concat("/").concat(newName)));
        //}
        return null;
    }

    @Override
    public void writeFile(String fileId, OutputStream out) {

    }

    @Override
    public String md5(String fileId) {
        return null;
    }
}
