package org.hsweb.web.controller.file;

import org.hsweb.web.logger.annotation.AccessLogger;
import org.hsweb.web.authorize.annotation.Authorize;
import org.hsweb.web.bean.po.resource.Resources;
import org.hsweb.web.message.ResponseMessage;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.webbuilder.utils.common.StringUtils;
import org.hsweb.web.service.config.ConfigService;
import org.hsweb.web.service.resource.FileService;
import org.hsweb.web.service.resource.ResourcesService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 文件管理控制器，用于上传和下载资源文件，使用restful。
 * Created by 浩 on 2015-08-28 0028.
 */
@RestController
@RequestMapping(value = "/file")
@AccessLogger("文件管理")
@Authorize
public class FileController {

    private org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 配置服务类，用于获取文件存放路径等配置信息
     */
    @Resource
    private ConfigService configService;

    /**
     * 资源服务类，每一个上传的文件都对应一个资源。通过存放到数据库的资源信息，可以实现文件秒传。
     * 通过资源id进行下载，使系统更安全
     */
    @Resource
    private ResourcesService resourcesService;

    /**
     * 文件服务类，用于进行文件保存等操作
     */
    @Resource
    private FileService fileService;

    //文件名中不允许出现的字符 \ / : | ? < > "
    private static final Pattern fileNameKeyWordPattern = Pattern.compile("(\\\\)|(/)|(:)(|)|(\\?)|(>)|(<)|(\")");

    private static final Map<String, String> mediaTypeMapper = new HashMap<>();

    static {
        mediaTypeMapper.put(".png", MediaType.IMAGE_PNG_VALUE);
        mediaTypeMapper.put(".jpg", MediaType.IMAGE_JPEG_VALUE);
        mediaTypeMapper.put(".jpeg", MediaType.IMAGE_JPEG_VALUE);
        mediaTypeMapper.put(".gif", MediaType.IMAGE_GIF_VALUE);
        mediaTypeMapper.put(".bmp", MediaType.IMAGE_JPEG_VALUE);
        mediaTypeMapper.put(".json", MediaType.APPLICATION_JSON_VALUE);
        mediaTypeMapper.put(".txt", MediaType.TEXT_PLAIN_VALUE);
        mediaTypeMapper.put(".css", MediaType.TEXT_PLAIN_VALUE);
        mediaTypeMapper.put(".js", "application/javascript");
        mediaTypeMapper.put(".html", MediaType.TEXT_HTML_VALUE);
        mediaTypeMapper.put(".xml", MediaType.TEXT_XML_VALUE);
    }

    /**
     * restful风格的文件下载
     */
    @RequestMapping(value = "/download/{id}/{name:.+}", method = RequestMethod.GET)
    @AccessLogger("下载文件")
    public ResponseMessage restDownLoad(@PathVariable("id") String id,
                                        @PathVariable("name") String name,
                                        HttpServletResponse response, HttpServletRequest request) {
        return downLoad(id, name, response, request);
    }

    /**
     * 下载文件,支持断点下载
     *
     * @param id   要下载资源文件的id
     * @param name 自定义文件名，该文件名不能存在非法字符
     * @return 失败时，会返回失败原因信息{@link ResponseMessage}
     */
    @RequestMapping(value = "/download/{id}", method = RequestMethod.GET)
    @AccessLogger("下载文件")
    public ResponseMessage downLoad(@PathVariable("id") String id,
                                    @RequestParam(value = "name", required = false) String name,
                                    HttpServletResponse response, HttpServletRequest request) {
        try {
            Resources resources = resourcesService.selectByPk(id);
            if (resources == null || resources.getStatus() != 1) {
                response.setStatus(404);
                return new ResponseMessage(false, "资源不存在！", "404");
            } else {
                if (!"file".equals(resources.getType()))
                    return new ResponseMessage(false, "该资源不是文件！", "400");
                String fileBasePath = configService.get("upload", "basePath", "/upload/").trim();
                File file = new File(fileBasePath.concat(resources.getPath().concat("/".concat(resources.getMd5()))));
                if (!file.canRead()) {
                    response.setStatus(404);
                    return new ResponseMessage(false, "资源不存在！", "404");
                }
                //获取contentType，默认application/octet-stream
                String contentType = mediaTypeMapper.get(resources.getSuffix().toLowerCase());
                if (contentType == null)
                    contentType = "application/octet-stream";
                if (StringUtils.isNullOrEmpty(name))//未自定义文件名，则使用上传时的文件名
                    name = resources.getName();
                if (!name.contains("."))//如果未指定文件拓展名，则追加默认的文件拓展名
                    name = name.concat(".").concat(resources.getSuffix());
                //关键字剔除
                name = fileNameKeyWordPattern.matcher(name).replaceAll("");
                int skip = 0;
                long fSize = file.length();
                //尝试判断是否为断点下载
                try {
                    //获取要继续下载的位置
                    String Range = request.getHeader("Range").replaceAll("bytes=", "").replaceAll("-", "");
                    skip = StringUtils.toInt(Range);
                } catch (Exception e) {
                }

                response.setContentLength((int) fSize);//文件大小
                response.setContentType(contentType);
                response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(name, "utf-8"));
                //try with resource
                try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                     BufferedOutputStream stream = new BufferedOutputStream(response.getOutputStream())) {
                    //断点下载
                    if (skip > 0) {
                        inputStream.skip(skip);
                        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                        String contentRange = new StringBuffer("bytes ").append(skip).append("-").append(fSize - 1).append("/").append(fSize).toString();
                        response.setHeader("Content-Range", contentRange);
                    }
                    byte b[] = new byte[2048 * 10];
                    while ((inputStream.read(b)) != -1) {
                        stream.write(b);
                    }
                    stream.flush();
                } catch (Exception e) {
                    logger.debug(String.format("download file error%s", e.getMessage()));
                    throw e;
                }
                return null;
            }
        } catch (Exception e) {
            return new ResponseMessage(false, e);
        }

    }

    /**
     * 上传文件，进行md5一致性校验，不保存重复文件。成功后返回文件信息{uid,md5,name}
     *
     * @param files 文件列表
     * @return 上传结果
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @AccessLogger("上传文件")
    public Object upload(@RequestParam("file") CommonsMultipartFile[] files) {
        if (logger.isInfoEnabled())
            logger.info(String.format("start upload , file number:%s", files.length));
        List<Resources> resourcesList = new LinkedList<>();
        for (int i = 0; i < files.length; i++) {
            CommonsMultipartFile file = files[i];
            if (!file.isEmpty()) {
                if (logger.isInfoEnabled())
                    logger.info(String.format("start write file:%s", file.getOriginalFilename()));
                try {
                    String fileName = files[i].getOriginalFilename();
                    Resources resources = fileService.saveFile(files[i].getFileItem().getInputStream(), fileName);
                    resourcesList.add(resources);
                } catch (Exception e) {
                    return new ResponseMessage(false, e);
                }
            }
        }//响应上传成功的资源信息
        return new ResponseMessage(true, resourcesList)
                .include(Resources.class, "u_id", "name", "md5");
    }
}
